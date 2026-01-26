<template>
  <j-modal
    :title="title"
    :width="width"
    :visible="visible"
    :confirmLoading="confirmLoading"
    :keyboard="false"
    :forceRender="true"
    v-bind:prefixNo="prefixNo"
    fullscreen
    switchFullscreen
    @cancel="handleCancel"
    :id="prefixNo"
    style="top:20px;height: 95%;">
    <template slot="footer">
      <a-button @click="handleCancel">取消</a-button>
      <!-- 盘点按钮：始终显示 -->
      <a-button :loading="confirmLoading" @click="handleSubmitOnly" type="primary">盘点</a-button>
      <!-- 有审核权限：显示"盘点并审核" -->
      <a-button v-if="checkFlag && isCanCheck" :loading="confirmLoading" @click="handleCheckAndSubmit" type="primary">盘点并审核</a-button>
      <a-button :loading="confirmLoading" @click="handleSave">保存(Ctrl+S)</a-button>
      <!--发起多级审核-->
      <a-button v-if="!checkFlag" @click="handleWorkflow()" type="primary">提交流程</a-button>
    </template>
    <a-spin :spinning="confirmLoading">
      <a-form :form="form">
        <a-row class="form-row" :gutter="24">
          <a-col :lg="6" :md="12" :sm="24">
            <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="盘点编号">
              <a-input placeholder="请输入盘点编号" v-decorator.trim="[ 'number', validatorRules.number ]" disabled />
            </a-form-item>
          </a-col>
          <a-col :lg="6" :md="12" :sm="24">
            <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="盘点日期">
              <j-date v-decorator="['operTime', validatorRules.operTime]" :show-time="false" date-format="YYYY-MM-DD" :disabledDate="disabledDate" :disabled="dateDisabled"/>
            </a-form-item>
          </a-col>
          <a-col :lg="6" :md="12" :sm="24">
            <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="盘点仓库">
              <a-select
                placeholder="请选择仓库"
                v-decorator="[ 'depotId']"
                :disabled="depotDisabled">
                <a-select-option v-for="(depot,index) in depotList" :key="index" :value="depot.id">
                  {{ depot.depotName }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :lg="6" :md="12" :sm="24">
            <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="">
              <a-button type="primary" @click="handleBatchAddGoods" :disabled="!rowCanEdit">
                批量添加待盘点商品
              </a-button>
            </a-form-item>
          </a-col>
        </a-row>
        <!-- 盘点结果统计 -->
        <a-row class="form-row" :gutter="24" style="margin-top: 16px; padding: 12px; background-color: #fafafa; border-radius: 4px;">
          <a-col :span="24" style="display: flex; justify-content: space-around; align-items: center;">
            <div @click="filterByCheckResult('total')"
                 :style="{textAlign: 'center', padding: '8px 20px', borderRight: '1px solid #e8e8e8', cursor: 'pointer', backgroundColor: checkFilter === 'total' ? '#e6f7ff' : 'transparent', borderRadius: '4px', transition: 'all 0.3s'}">
              <div style="font-size: 24px; font-weight: bold; color: #1890ff;">{{ checkStatistics.total }}</div>
              <div style="color: #666; margin-top: 4px;">应盘</div>
            </div>
            <div @click="filterByCheckResult('checked')"
                 :style="{textAlign: 'center', padding: '8px 20px', borderRight: '1px solid #e8e8e8', cursor: 'pointer', backgroundColor: checkFilter === 'checked' ? '#e6f7ff' : 'transparent', borderRadius: '4px', transition: 'all 0.3s'}">
              <div style="font-size: 24px; font-weight: bold; color: #52c41a;">{{ checkStatistics.checked }}</div>
              <div style="color: #666; margin-top: 4px;">已盘</div>
            </div>
            <div @click="filterByCheckResult('unchecked')"
                 :style="{textAlign: 'center', padding: '8px 20px', borderRight: '1px solid #e8e8e8', cursor: 'pointer', backgroundColor: checkFilter === 'unchecked' ? '#f5f5f5' : 'transparent', borderRadius: '4px', transition: 'all 0.3s'}">
              <div style="font-size: 24px; font-weight: bold; color: #999;">{{ checkStatistics.unchecked }}</div>
              <div style="color: #666; margin-top: 4px;">未盘</div>
            </div>
            <div @click="filterByCheckResult('surplus')"
                 :style="{textAlign: 'center', padding: '8px 20px', borderRight: '1px solid #e8e8e8', cursor: 'pointer', backgroundColor: checkFilter === 'surplus' ? '#fff1f0' : 'transparent', borderRadius: '4px', transition: 'all 0.3s'}">
              <div style="font-size: 24px; font-weight: bold; color: #ff4d4f;">{{ checkStatistics.surplus }}</div>
              <div style="color: #666; margin-top: 4px;">🟢 盘盈</div>
            </div>
            <div @click="filterByCheckResult('loss')"
                 :style="{textAlign: 'center', padding: '8px 20px', cursor: 'pointer', backgroundColor: checkFilter === 'loss' ? '#f6ffed' : 'transparent', borderRadius: '4px', transition: 'all 0.3s'}">
              <div style="font-size: 24px; font-weight: bold; color: #faad14;">{{ checkStatistics.loss }}</div>
              <div style="color: #666; margin-top: 4px;">🔴 盘亏</div>
            </div>
          </a-col>
        </a-row>
        <j-editable-table id="billModal"
          :ref="refKeys[0]"
          :loading="materialTable.loading"
          :columns="materialTable.columns"
          :dataSource="materialTable.dataSource"
          :minWidth="minWidth"
          :maxHeight="300"
          :rowNumber="false"
          :rowSelection="rowCanEdit"
          :actionButton="rowCanEdit"
          :dragSortAndNumber="rowCanEdit"
          @valueChange="onValueChange"
          @added="onAdded"
          @deleted="onDeleted">
          <template #buttonAfter>
            <a-row v-if="rowCanEdit" :gutter="24" style="float:left;" data-step="4" data-title="扫码录入" data-intro="此功能支持扫码枪扫描商品条码进行录入">
              <a-col v-if="scanStatus" :md="6" :sm="24">
                <a-button @click="scanEnter">扫码录入</a-button>
              </a-col>
              <a-col v-if="!scanStatus" :md="16" :sm="24" style="padding: 0 8px 0 12px">
                <a-input placeholder="请扫描商品条码并回车" v-model="scanBarCode" @pressEnter="scanPressEnter" ref="scanBarCode"/>
              </a-col>
              <a-col v-if="!scanStatus" :md="6" :sm="24" style="padding: 0px 12px 0 0">
                <a-button @click="stopScan">收起扫码</a-button>
              </a-col>
            </a-row>
            <a-row v-if="rowCanEdit" :gutter="24" style="float:left;padding-bottom: 5px;padding-left:20px;">
              <a-button icon="import" @click="onImport(prefixNo)">导入明细</a-button>
            </a-row>
          </template>
          <template #depotBatchSet>
            <a-icon type="down" @click="handleBatchSetDepot" />
          </template>
          <template #depotAdd>
            <a-divider v-if="quickBtn.depot" style="margin: 4px 0;" />
            <div v-if="quickBtn.depot" class="dropdown-btn" @click="addDepot"><a-icon type="plus" /> 新增</div>
            <div class="dropdown-btn" @mousedown="e => e.preventDefault()" @click="initDepot"><a-icon type="reload" /> 刷新</div>
          </template>
        </j-editable-table>
        <a-row class="form-row" :gutter="24">
          <a-col :lg="24" :md="24" :sm="24">
            <a-form-item :labelCol="labelCol" :wrapperCol="{xs: { span: 24 },sm: { span: 24 }}" label="">
              <a-textarea :rows="1" placeholder="请输入备注" v-decorator="[ 'remark' ]" style="margin-top:8px;"/>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row class="form-row" :gutter="24">
          <a-col :lg="6" :md="12" :sm="24">
            <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="附件">
              <j-upload v-model="fileList" bizPath="bill"></j-upload>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-spin>
    <depot-modal ref="depotModalForm" @ok="depotModalFormOk"></depot-modal>
    <batch-set-depot ref="batchSetDepotModalForm" @ok="batchSetDepotModalFormOk"></batch-set-depot>
    <import-item-modal ref="importItemModalForm" @ok="importItemModalFormOk"></import-item-modal>
    <workflow-iframe ref="modalWorkflow" @ok="workflowModalFormOk"></workflow-iframe>
  </j-modal>
</template>
<script>
  import pick from 'lodash.pick'
  import DepotModal from '../../system/modules/DepotModal'
  import BatchSetDepot from '../dialog/BatchSetDepot'
  import ImportItemModal from '../dialog/ImportItemModal'
  import WorkflowIframe from '@/components/tools/WorkflowIframe'
  import { FormTypes } from '@/utils/JEditableTableUtil'
  import { JEditableTableMixin } from '@/mixins/JEditableTableMixin'
  import { BillModalMixin } from '../mixins/BillModalMixin'
  import { getMpListShort, formatDate } from '@/utils/util'
  import JUpload from '@/components/jeecg/JUpload'
  import JDate from '@/components/jeecg/JDate'
  import Vue from 'vue'
  import { getAction } from '@/api/manage'
  import { saveCheck, submitAndApproveCheck, submitOnlyCheck, getCheckDetail, DepotCheckStatus } from '@/api/depotCheck'
  export default {
    name: "StockCheckModal",
    mixins: [JEditableTableMixin, BillModalMixin],
    components: {
      DepotModal,
      BatchSetDepot,
      ImportItemModal,
      WorkflowIframe,
      JUpload,
      JDate,
      VNodes: {
        functional: true,
        render: (h, ctx) => ctx.props.vnodes,
      }
    },
    data () {
      return {
        title:"操作",
        width: '1600px',
        moreStatus: false,
        // 新增时子表默认添加几行空数据
        addDefaultRowNum: 0,
        visible: false,
        operTimeStr: '',
        prefixNo: 'PDFP',
        fileList:[],
        rowCanEdit: true,
        model: {},
        depotList: [],
        selectedDepotId: undefined, // 从列表页传入的仓库ID
        selectedCheckDate: undefined, // 从列表页传入的盘点日期
        depotDisabled: false, // 仓库字段是否禁用
        dateDisabled: false, // 盘点日期字段是否禁用
        labelCol: {
          xs: { span: 24 },
          sm: { span: 8 },
        },
        wrapperCol: {
          xs: { span: 24 },
          sm: { span: 16 },
        },
        refKeys: ['materialDataTable', ],
        activeKey: 'materialDataTable',
        materialTable: {
          loading: false,
          dataSource: [],
          columns: [
            { title: '条码', key: 'barCode', width: '12%', type: FormTypes.popupJsh, kind: 'material', multi: true,
              validateRules: [{ required: true, message: '${title}不能为空' }]
            },
            { title: '名称', key: 'name', width: '12%', type: FormTypes.normal },
            { title: '规格', key: 'standard', width: '10%', type: FormTypes.normal },
            { title: '类别', key: 'categoryName', width: '8%', type: FormTypes.normal },
            { title: '单位', key: 'unit', width: '6%', type: FormTypes.normal },
            { title: '多属性', key: 'sku', width: '8%', type: FormTypes.normal },
            { title: '账面数量', key: 'stock', width: '7%', type: FormTypes.normal },
            { title: '盘点数量', key: 'operNumber', width: '7%', type: FormTypes.inputNumber, statistics: true,
              validateRules: [
                {
                  pattern: /^(0|[1-9]\d*)(\.\d+)?$/,
                  message: '${title}必须大于等于0'
                }]
            },
            { title: '盈亏数量', key: 'diffNumber', width: '7%', type: FormTypes.normal, statistics: true },
            { title: '盘点结果', key: 'checkResult', width: '6%', type: FormTypes.normal },
            { title: '单价', key: 'unitPrice', width: '6%', type: FormTypes.normal},
            { title: '盈亏金额', key: 'allPrice', width: '7%', type: FormTypes.normal, statistics: true },
            { title: '备注', key: 'remark', width: '6%', type: FormTypes.input },
            { title: '商品标识', key: 'meId', width: '0%', type: FormTypes.hidden },
          ]
        },
        confirmLoading: false,
        validatorRules:{
          operTime:{
            rules: [
              { required: true, message: '请输入盘点日期!' }
            ]
          },
          number:{
            rules: [
              { required: true, message: '请输入盘点编号!' }
            ]
          }
        },
        url: {
          save: '/depotCheck/save',
          submitAndApprove: '/depotCheck/submitAndApprove',
          detailList: '/depotCheck/detail'
        },
        checkStatistics: {
          total: 0,      // 应盘
          checked: 0,    // 已盘
          unchecked: 0,  // 未盘
          surplus: 0,    // 盘盈
          loss: 0        // 盘亏
        },
        checkFilter: 'total', // 当前过滤条件: null(全部), 'total'(应盘), 'checked'(已盘), 'unchecked'(未盘), 'surplus'(盘盈), 'loss'(盘亏) - 默认选中应盘
        allDataSource: []  // 保存所有数据,用于过滤
      }
    },
    computed: {},
    watch: {
      'materialTable.dataSource': {
        handler(newVal) {
          this.updateCheckStatistics()
        },
        deep: true
      }
    },
    created () {
    },
    methods: {
      //重写addInit和copyAddInit方法，使用年月日格式
      addInit(amountNum) {
        getAction('/sequence/buildNumber').then((res) => {
          if (res && res.code === 200) {
            this.model.defaultNumber = amountNum + res.data.defaultNumber
            this.form.setFieldsValue({'number': amountNum + res.data.defaultNumber})
          }
        })
        const today = formatDate(new Date())
        this.$nextTick(() => {
          this.form.setFieldsValue({'operTime': today})
        })
      },
      copyAddInit(amountNum) {
        getAction('/sequence/buildNumber').then((res) => {
          if (res && res.code === 200) {
            this.form.setFieldsValue({'number': amountNum + res.data.defaultNumber})
          }
        })
        const today = formatDate(new Date())
        this.$nextTick(() => {
          this.form.setFieldsValue({'operTime': today})
        })
      },
      //禁用未来日期
      disabledDate(current) {
        // 不能选择超过今天的日期
        return current && current > this.$moment().endOf('day')
      },
      //根据盘点结果过滤数据
      filterByCheckResult(filterType) {
        // 如果点击当前已选中的过滤条件,则取消过滤
        if (this.checkFilter === filterType) {
          this.checkFilter = null
          this.materialTable.dataSource = [...this.allDataSource]
          return
        }

        this.checkFilter = filterType

        const filterMap = {
          'total': () => true,
          'checked': (item) => item.checkResult && (item.checkResult.includes('盘盈') || item.checkResult.includes('盘亏') || item.checkResult.includes('盘平')),
          'unchecked': (item) => item.checkResult && item.checkResult.includes('未盘'),
          'surplus': (item) => item.checkResult && item.checkResult.includes('盘盈'),
          'loss': (item) => item.checkResult && item.checkResult.includes('盘亏')
        }

        const filter = filterMap[filterType] || (() => true)
        this.materialTable.dataSource = this.allDataSource.filter(filter)
      },
      //更新盘点统计
      updateCheckStatistics() {
        const dataToCount = this.allDataSource.length > 0 ? this.allDataSource : this.materialTable.dataSource

        const statistics = {
          total: 0,
          checked: 0,
          unchecked: 0,
          surplus: 0,
          loss: 0
        }

        dataToCount.forEach(item => {
          statistics.total++

          if (item.checkResult && item.checkResult.includes('未盘')) {
            statistics.unchecked++
          } else if (item.checkResult && item.checkResult.includes('盘盈')) {
            statistics.surplus++
            statistics.checked++
          } else if (item.checkResult && item.checkResult.includes('盘亏')) {
            statistics.loss++
            statistics.checked++
          } else if (item.checkResult && item.checkResult.includes('盘平')) {
            statistics.checked++
          }
        })

        this.checkStatistics = statistics
      },
      //调用完edit()方法之后会自动调用此方法
      editAfter() {
        this.currentSelectDepotId = ''
        this.changeColumnHide()

        if (this.action === 'add') {
          this.billStatus = '0'
          this.rowCanEdit = true
          this.addInit(this.prefixNo)
          this.fileList = []

          // 如果从列表页传入了仓库ID和盘点日期,自动设置并禁用字段
          this.depotDisabled = !!this.selectedDepotId
          this.dateDisabled = !!this.selectedCheckDate

          this.$nextTick(() => {
            if(this.selectedDepotId) {
              this.form.setFieldsValue({'depotId': this.selectedDepotId})
              this.currentSelectDepotId = this.selectedDepotId
            }
            if(this.selectedCheckDate) {
              this.form.setFieldsValue({'operTime': this.selectedCheckDate})
            }
          })
        } else {
          // 编辑时：根据状态判断是否可以编辑
          const canEdit = this.model.status === 1 || this.model.status === 2
          this.billStatus = canEdit ? '0' : '1'
          this.rowCanEdit = canEdit
          this.depotDisabled = true
          this.dateDisabled = true
          this.model.operTime = this.model.checkDate

          this.$nextTick(() => {
            this.form.setFieldsValue({
              operTime: this.model.checkDate,
              number: this.model.checkNumber,
              depotId: this.model.depotId,
              remark: this.model.remark
            })
          })

          // 加载子表数据
          this.loadCheckDetail()
        }

        // 复制新增单据-初始化单号和日期
        if(this.action === 'copyAdd') {
          this.model.id = ''
          this.model.tenantId = ''
          this.copyAddInit(this.prefixNo)
          this.depotDisabled = true
        }

        this.initSystemConfig()
        this.initDepot()
        this.initDepotList()
        this.initPlatform()
        this.initQuickBtn()
        this.handleChangeOtherField()
      },
      // 加载盘点明细
      loadCheckDetail() {
        getCheckDetail(this.model.id).then(res => {
          if (res && res.code === 200) {
            const data = res.data.data
            const items = data.items || []
            this.fileList = data.fileNames

            const mappedItems = items.map(item => ({
              id: item.id,
              barCode: item.mBarCode,
              name: item.name,
              standard: item.standard,
              categoryName: item.categoryName,
              unit: item.unit,
              sku: item.sku || '',
              stock: item.originNumber,
              operNumber: item.actualNumber,
              diffNumber: item.diffNumber,
              checkResult: this.getCheckResult(item),
              unitPrice: item.unitPrice,
              allPrice: item.diffAmount,
              remark: item.remark,
              meId: item.materialId,
              depotId: this.model.depotId
            }))

            this.materialTable.dataSource = mappedItems
            this.allDataSource = [...mappedItems]
            this.updateCheckStatistics()
          }
        }).catch(err => {
          console.error('加载盘点明细异常:', err)
        })
      },
      // 计算盘点结果
      getCheckResult(item) {
        if (item.actualNumber === null || item.actualNumber === undefined) {
          return '⚪ 未盘'
        }
        if (item.diffNumber > 0) return '🟢 盘盈'
        if (item.diffNumber < 0) return '🔴 盘亏'
        return '🔵 盘平'
      },
      //初始化仓库列表
      initDepotList() {
        getAction('/depot/findDepotByCurrentUser').then((res) => {
          if(res.code === 200){
            this.depotList = res.data
          }
        })
      },
      //批量添加待盘点商品
      handleBatchAddGoods() {
        let that = this
        let depotId = this.form.getFieldValue('depotId')
        if(!depotId) {
          this.$message.warning('请先选择仓库!')
          return
        }

        // 检查是否已有数据
        this.$refs.materialDataTable.getValues((error, values) => {
          if(error) {
            console.error('获取表格数据失败:', error)
            return
          }

          // 如果表格已有数据,显示确认对话框
          if(values && values.length > 0) {
            that.$confirm({
              title: '提示',
              content: '确定要批量添加盘点商品吗(重复的商品将不添加)?',
              onOk() {
                that.doBatchAddGoods(depotId, values)
              }
            })
          } else {
            // 表格为空,直接添加
            that.doBatchAddGoods(depotId, [])
          }
        })
      },
      //执行批量添加商品
      doBatchAddGoods(depotId, existingValues) {
        this.confirmLoading = true
        //根据仓库获取所有商品的库存信息
        getAction('/material/listWithStock', { depotId: depotId }).then((res) => {
          if (res && res.code === 200) {
            let stockList = res.data
            if(!stockList || stockList.length === 0) {
              this.$message.warning('该仓库没有库存商品!')
              this.confirmLoading = false
              return
            }

            // 获取已存在的商品条码列表,用于去重
            let existingBarCodes = new Set()
            if(existingValues && existingValues.length > 0) {
              existingValues.forEach(item => {
                if(item.barCode) {
                  existingBarCodes.add(item.barCode)
                }
              })
            }

            let newDetailArr = []
            let skippedCount = 0
            for(let i=0; i<stockList.length; i++) {
              let item = stockList[i]
              // 检查是否重复
              if(existingBarCodes.has(item.mBarCode)) {
                skippedCount++
                continue
              }

              this.changeColumnShow(item)
              let mObj = {
                depotId: depotId + '',
                barCode: item.mBarCode,
                meId: item.meId,
                name: item.name,
                standard: item.standard,
                model: item.model,
                categoryName: item.categoryName,
                unit: item.unitName,
                sku: item.sku,
                stock: item.currentStock || 0,
                checkResult: '⚪ 未盘',
                unitPrice: item.purchaseDecimal || 0,
                allPrice: 0,
                remark: ''
              }
              newDetailArr.push(mObj)
            }

            // 合并已有数据和新数据
            let finalArr = existingValues.concat(newDetailArr)
            this.materialTable.dataSource = finalArr
            this.allDataSource = [...finalArr]

            if(newDetailArr.length > 0) {
              let msg = '已添加' + newDetailArr.length + '个商品'
              if(skippedCount > 0) {
                msg += ',跳过' + skippedCount + '个重复商品'
              }
              this.$message.success(msg)
            } else {
              this.$message.warning('没有新商品可添加!')
            }
            // 更新统计
            this.$nextTick(() => {
              this.updateCheckStatistics()
            })
          } else {
            this.$message.error(res.data || '获取商品库存失败!')
          }
        }).catch((error) => {
          console.error('批量添加商品失败:', error)
          this.$message.error('批量添加商品失败,请检查网络或联系管理员!')
        }).finally(() => {
          this.confirmLoading = false
        })
      },
      //提交单据时整理成formData
      classifyIntoFormData(allValues) {
        // 新接口使用不同的保存流程
        // 这个方法现在主要用于验证,实际保存在 handleOk 中处理
        let billMain = Object.assign(this.model, allValues.formValue)
        let detailArr = allValues.tablesValue[0].values

        return {
          billMain: billMain,
          detailArr: detailArr
        }
      },
      // 保存/盘点/盘点并审核的通用提交方法
      async submitCheck(mode) {
        // mode: 'save' | 'submit' | 'submitAndApprove'
        await this.syncDataBeforeSave()

        // 校验列表数据不能为空
        if (!this.allDataSource || this.allDataSource.length === 0) {
          this.$message.error('盘点明细不能为空，请添加商品后再提交')
          return
        }

        // 如果是提交或审核模式，需要校验盘点数量必填
        if (mode !== 'save') {
          const emptyRows = this.allDataSource
            .map((item, index) => ({ item, index }))
            .filter(({ item }) => item.operNumber === undefined || item.operNumber === null || item.operNumber === '')
            .map(({ index }) => index + 1)

          if (emptyRows.length > 0) {
            const action = mode === 'submitAndApprove' ? '提交审核' : '盘点'
            this.$message.error(`第 ${emptyRows.join(', ')} 行的盘点数量不能为空,请填写后再${action}`)
            return
          }
        }

        this.form.validateFields(async (err, formValues) => {
          if (err) return

          this.confirmLoading = true
          try {
            const requestData = {
              depotId: formValues.depotId,
              checkNumber: formValues.number,
              checkDate: formValues.operTime,
              remark: formValues.remark || '',
              items: this.allDataSource.map(item => ({
                materialId: item.meId,
                actualNumber: item.operNumber,
                remark: item.remark || ''
              }))
            }

            if (this.fileList && this.fileList.length > 0) {
              requestData.fileNames = this.fileList
            }
            if (this.model.id) {
              requestData.id = this.model.id
            }

            const apiMap = {
              'save': saveCheck,
              'submit': submitOnlyCheck,
              'submitAndApprove': submitAndApproveCheck
            }
            const messageMap = {
              'save': '暂存成功',
              'submit': '盘点提交成功，等待审核',
              'submitAndApprove': '盘点并提交审核成功'
            }

            const res = await apiMap[mode](requestData)
            if (res.code === 200) {
              this.$message.success(messageMap[mode])
              if (!this.model.id && res.data.id) {
                this.model.id = res.data.id
              }
              this.$emit('ok')
              this.close()
            } else {
              this.$message.error(res.data || '操作失败')
            }
          } catch (error) {
            console.error('操作失败:', error)
            this.$message.error('操作失败: ' + (error.message || '未知错误'))
          } finally {
            this.confirmLoading = false
          }
        })
      },
      // 保存方法(暂存,不校验盘点数量必填)
      async handleSave() {
        await this.submitCheck('save')
      },
      // 盘点并审核
      async handleCheckAndSubmit() {
        await this.submitCheck('submitAndApprove')
      },
      // 盘点(只提交不审核)
      async handleSubmitOnly() {
        await this.submitCheck('submit')
      },
      //单元值改变一个字符就触发一次
      onValueChange(event) {
        const { row, column, value, target } = event

        switch(column.key) {
          case "barCode":
            this.handleBarCodeChange(row, value, target)
            break
          case "operNumber":
            this.handleOperNumberChange(row, value, target)
            break
          case "unitPrice":
            this.handleUnitPriceChange(row, value, target)
            break
          case "allPrice":
            this.handleAllPriceChange(row, value, target)
            break
        }
      },
      // 处理条码变化
      handleBarCodeChange(row, value, target) {
        if (value === "") {
          target.setValues([{rowKey: row.id, values: {name: "", standard: null, categoryName: null, unit:"",
              stock: 0, operNumber: 0, diffNumber: 0, unitPrice: 0, allPrice: 0}}])
          target.recalcAllStatisticsColumns()
          return
        }

        const depotIdSelected = row.depotId || this.form.getFieldValue('depotId')
        if(!depotIdSelected) {
          this.$message.warning('请先选择仓库!')
          return
        }

        const param = {
          barCode: value,
          depotId: depotIdSelected,
          mpList: getMpListShort(Vue.ls.get('materialPropertyList')),
          prefixNo: this.prefixNo
        }

        getAction('/material/getMaterialByBarCode', param).then((res) => {
          if (res && res.code === 200) {
            const mList = res.data
            if (value.indexOf(',') > -1) {
              // 多个条码
              this.$refs.materialDataTable.getValues((error, values) => {
                values.pop()
                const mArr = values
                for (let i = 0; i < mList.length; i++) {
                  const mInfo = mList[i]
                  this.changeColumnShow(mInfo)
                  const mObj = this.parseInfoToObj(mInfo)
                  mObj.depotId = depotIdSelected + ''
                  mObj.stock = mInfo.stock
                  mObj.checkResult = '⚪ 未盘'
                  mArr.push(mObj)
                }
                this.materialTable.dataSource = mArr
                target.$forceUpdate()
              })
            } else {
              // 单个条码
              const mInfo = mList[0]
              this.changeColumnShow(mInfo)
              const mInfoEx = this.parseInfoToObj(mInfo)
              mInfoEx.depotId = depotIdSelected + ''
              mInfoEx.stock = mInfo.stock
              mInfoEx.checkResult = '⚪ 未盘'
              target.setValues([{ rowKey: row.id, values: mInfoEx }])
              target.recalcAllStatisticsColumns()
              target.autoSelectBySpecialKey('operNumber', row.orderNum)
              target.$forceUpdate()
            }
          }
        })
      },
      // 处理盘点数量变化
      handleOperNumberChange(row, value, target) {
        if (value === null || value === undefined || value === '') {
          target.setValues([{
            rowKey: row.id,
            values: {
              operNumber: null,
              diffNumber: null,
              allPrice: 0,
              checkResult: '⚪ 未盘'
            }
          }])
          target.recalcAllStatisticsColumns()
          this.syncAndUpdateStats(target)
        } else {
          const operNumber = value - 0
          const stock = row.stock - 0
          const diffNumber = (operNumber - stock).toFixed(2) - 0
          const unitPrice = row.unitPrice - 0
          const allPrice = (unitPrice * diffNumber).toFixed(2) - 0

          let checkResult = '🔵 盘平'
          if (diffNumber > 0) checkResult = '🟢 盘盈'
          else if (diffNumber < 0) checkResult = '🔴 盘亏'

          target.setValues([{rowKey: row.id, values: {diffNumber, allPrice, checkResult}}])
          target.recalcAllStatisticsColumns()
          this.syncAndUpdateStats(target)
        }
      },
      // 处理单价变化
      handleUnitPriceChange(row, value, target) {
        const operNumber = row.operNumber - 0
        const stock = row.stock - 0
        const diffNumber = (operNumber - stock).toFixed(2) - 0
        const unitPrice = value - 0
        const allPrice = (unitPrice * diffNumber).toFixed(2) - 0
        target.setValues([{rowKey: row.id, values: {allPrice}}])
        target.recalcAllStatisticsColumns()
      },
      // 处理金额变化
      handleAllPriceChange(row, value, target) {
        const operNumber = row.operNumber - 0
        const stock = row.stock - 0
        const diffNumber = (operNumber - stock).toFixed(2) - 0
        const allPrice = value - 0
        const unitPrice = diffNumber !== 0 ? (allPrice / diffNumber).toFixed(4) - 0 : 0
        target.setValues([{rowKey: row.id, values: {unitPrice}}])
        target.recalcAllStatisticsColumns()
      },
      // 同步数据并更新统计
      syncAndUpdateStats(target) {
        this.$nextTick(() => {
          this.$refs.materialDataTable.getValues((error, values) => {
            if (!error) {
              this.materialTable.dataSource = values
              if (this.checkFilter && this.checkFilter !== 'total') {
                const currentValuesMap = new Map(values.map(item => [item.id, item]))
                this.allDataSource = this.allDataSource.map(item =>
                  currentValuesMap.has(item.id) ? currentValuesMap.get(item.id) : item
                )
              } else {
                this.allDataSource = [...values]
              }
              this.updateCheckStatistics()
            }
          })
        })
      },
      //转为商品对象
      parseInfoToObj(mInfo) {
        return {
          barCode: mInfo.mBarCode,
          meId: mInfo.meId,
          name: mInfo.name,
          standard: mInfo.standard,
          model: mInfo.model,
          categoryName: mInfo.categoryName,
          enableBatchNumber: mInfo.enableBatchNumber,
          unit: mInfo.commodityUnit,
          sku: mInfo.sku,
          stock: mInfo.stock,
          checkResult: '⚪ 未盘',
          unitPrice: mInfo.purchaseDecimal || 0,
          allPrice: 0
        }
      },
      // 保存前同步表格数据到 allDataSource
      async syncDataBeforeSave() {
        return new Promise((resolve, reject) => {
          this.$refs.materialDataTable.getValues((error, values) => {
            if (error) {
              console.error('获取表格数据失败:', error)
              reject(error)
              return
            }

            // 如果当前有过滤条件，需要合并数据
            if (this.checkFilter && this.checkFilter !== 'total') {
              // 创建一个 Map 用于快速查找
              const currentValuesMap = new Map(values.map(item => [item.id, item]))

              // 更新 allDataSource 中对应的项
              this.allDataSource = this.allDataSource.map(item => {
                // 如果这个项在当前显示的数据中（即符合过滤条件），使用最新的值
                if (currentValuesMap.has(item.id)) {
                  return currentValuesMap.get(item.id)
                }
                // 否则保留原值
                return item
              })
            } else {
              // 没有过滤条件，直接使用表格的全部数据
              this.allDataSource = [...values]
            }

            resolve()
          })
        })
      },
      //添加新行时自动填充表头选择的仓库
      onAdded(event) {
        const { row, target } = event
        // 获取表头选择的仓库
        let headerDepotId = this.form.getFieldValue('depotId')
        // 初始化新行的默认值,盘点数量为空
        let defaultValues = {
          stock: 0,
          checkResult: '⚪ 未盘',
          unitPrice: 0,
          allPrice: 0
        }
        if(headerDepotId) {
          // 自动填充到新行的仓库字段
          defaultValues.depotId = headerDepotId + ''
        }
        target.setValues([{rowKey: row.id, values: defaultValues}])
        // 同步更新 allDataSource 和统计
        this.$nextTick(() => {
          this.allDataSource = [...this.materialTable.dataSource]
          this.updateCheckStatistics()
        })
        // 自动下滑到最后一行
        let that = this
        setTimeout(function(){
          that.$refs.materialDataTable.resetScrollTop((target.rows.length+1)*that.$refs.materialDataTable.rowHeight)
        },1000)
      },
      //删除行时更新统计
      onDeleted(event) {
        // 获取删除后的最新数据（这是当前显示的数据，可能是过滤后的）
        this.$nextTick(() => {
          this.$refs.materialDataTable.getValues((error, values) => {
            if (!error) {
              // 更新当前显示的数据源
              this.materialTable.dataSource = values

              // 如果当前有过滤条件，需要同步更新 allDataSource
              if (this.checkFilter && this.checkFilter !== 'total') {
                // 获取当前显示数据的所有 id
                const currentIds = new Set(values.map(item => item.id))
                // 从 allDataSource 中移除不在当前显示数据中的项
                // 注意：我们需要保留其他过滤条件下的数据
                // 所以我们需要找出被删除的项，然后从 allDataSource 中移除
                const displayedIds = new Set(this.materialTable.dataSource.map(item => item.id))
                this.allDataSource = this.allDataSource.filter(item => {
                  // 如果这个项在当前过滤条件下应该显示，则检查它是否在 values 中
                  const shouldDisplay = this.shouldItemDisplay(item, this.checkFilter)
                  if (shouldDisplay) {
                    return currentIds.has(item.id)
                  }
                  // 如果不在当前过滤条件下，保留它
                  return true
                })
              } else {
                // 没有过滤条件，直接同步
                this.allDataSource = [...values]
              }

              // 更新统计
              this.updateCheckStatistics()
            }
          })
        })
      },
      // 判断某个项是否应该在指定过滤条件下显示
      shouldItemDisplay(item, filterType) {
        if (!filterType || filterType === 'total') {
          return true
        }

        switch(filterType) {
          case 'checked':
            return item.checkResult && (
              item.checkResult.includes('盘盈') ||
              item.checkResult.includes('盘亏') ||
              item.checkResult.includes('盘平')
            )
          case 'unchecked':
            return item.checkResult && item.checkResult.includes('未盘')
          case 'surplus':
            return item.checkResult && item.checkResult.includes('盘盈')
          case 'loss':
            return item.checkResult && item.checkResult.includes('盘亏')
          default:
            return true
        }
      },
      //关闭弹窗时重置数据
      close() {
        // 重置附件列表
        this.fileList = []
        // 重置过滤条件
        this.checkFilter = 'total'
        // 重置统计数据
        this.checkStatistics = {
          total: 0,
          checked: 0,
          unchecked: 0,
          surplus: 0,
          loss: 0
        }
        // 重置所有数据源
        this.allDataSource = []
        // 重置仓库禁用状态
        this.depotDisabled = false
        // 重置日期禁用状态
        this.dateDisabled = false
        // 重置选中的仓库ID
        this.selectedDepotId = undefined
        // 重置选中的盘点日期
        this.selectedCheckDate = undefined
        // 调用父类的 close 方法
        this.$options.mixins.forEach(mixin => {
          if (mixin.methods && mixin.methods.close) {
            mixin.methods.close.call(this)
          }
        })
      }
    }
  }
</script>
<style scoped>

</style>