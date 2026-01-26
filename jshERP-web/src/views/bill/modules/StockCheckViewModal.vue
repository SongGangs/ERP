<template>
  <j-modal
    :title="title"
    :width="width"
    :visible="visible"
    :keyboard="false"
    :forceRender="true"
    fullscreen
    switchFullscreen
    @cancel="handleCancel"
    style="top:20px;height: 95%;">
    <template slot="footer">
      <a-button @click="handleCancel">关闭</a-button>
      <!-- 状态为1(盘点中)或2(已盘点未审核)时显示提交盘点按钮 -->
      <a-button v-if="model.status === DepotCheckStatus.CHECKING || model.status === DepotCheckStatus.CHECKED_UNAPPROVED" @click="handleSubmitCheck" type="primary" :loading="submitLoading">提交盘点</a-button>
      <!-- 状态为1(盘点中)或2(已盘点未审核)时显示提交盘点并审核按钮 -->
      <a-button v-if="model.status === DepotCheckStatus.CHECKING || model.status === DepotCheckStatus.CHECKED_UNAPPROVED" @click="handleSubmitAndApprove" type="primary" :loading="submitLoading">提交盘点并审核</a-button>
    </template>
    <a-spin :spinning="loading">
      <a-form :form="form">
        <a-row class="form-row" :gutter="24">
          <a-col :lg="6" :md="12" :sm="24">
            <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="盘点编号">
              <a-input :value="model.checkNumber" disabled />
            </a-form-item>
          </a-col>
          <a-col :lg="6" :md="12" :sm="24">
            <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="盘点日期">
              <a-input :value="model.checkDate" disabled />
            </a-form-item>
          </a-col>
          <a-col :lg="6" :md="12" :sm="24">
            <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="盘点仓库">
              <a-input :value="model.depotName" disabled />
            </a-form-item>
          </a-col>
          <a-col :lg="6" :md="12" :sm="24">
            <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="操作员">
              <a-input :value="model.operatorName" disabled />
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
                 :style="{textAlign: 'center', padding: '8px 20px', borderRight: '1px solid #e8e8e8', cursor: 'pointer', backgroundColor: checkFilter === 'surplus' ? '#fff1f0' : 'transparent', borderRadius: '4px', transition: 'all 0.3s', position: 'relative'}">
              <div style="font-size: 24px; font-weight: bold; color: #ff4d4f;">{{ checkStatistics.surplus }}</div>
              <div style="color: #666; margin-top: 4px;">🟢 盘盈</div>
              <!-- 只有状态为3(已审核)时才显示入库单按钮 -->
              <template v-if="model.status === DepotCheckStatus.CHECKED_APPROVED || model.status === DepotCheckStatus.ADJUSTED">
                <!-- 如果有入库单号,显示查看按钮;否则显示生成按钮 -->
                <a-button v-if="checkStatistics.surplus > 0 && model.rkNumber"
                          type="primary"
                          size="small"
                          icon="eye"
                          @click.stop="handleViewInBill"
                          style="margin-top: 8px;">
                  查看{{model.rkNumber}}
                </a-button>
                <a-button v-else-if="checkStatistics.surplus > 0 && !model.rkNumber"
                          type="primary"
                          size="small"
                          icon="plus"
                          :disabled="inBillGenerated"
                          @click.stop="handleGenerateInBill"
                          style="margin-top: 8px;">
                  生成入库单
                </a-button>
              </template>
            </div>
            <div @click="filterByCheckResult('loss')"
                 :style="{textAlign: 'center', padding: '8px 20px', cursor: 'pointer', backgroundColor: checkFilter === 'loss' ? '#f6ffed' : 'transparent', borderRadius: '4px', transition: 'all 0.3s', position: 'relative'}">
              <div style="font-size: 24px; font-weight: bold; color: #faad14;">{{ checkStatistics.loss }}</div>
              <div style="color: #666; margin-top: 4px;">🔴 盘亏</div>
              <!-- 只有状态为3(已审核)时才显示出库单按钮 -->
              <template v-if="model.status === DepotCheckStatus.CHECKED_APPROVED || model.status === DepotCheckStatus.ADJUSTED">
                <!-- 如果有出库单号,显示查看按钮;否则显示生成按钮 -->
                <a-button v-if="checkStatistics.loss > 0 && model.ckNumber"
                          type="danger"
                          size="small"
                          icon="eye"
                          @click.stop="handleViewOutBill"
                          style="margin-top: 8px;">
                  查看{{model.ckNumber}}
                </a-button>
                <a-button v-else-if="checkStatistics.loss > 0 && !model.ckNumber"
                          type="danger"
                          size="small"
                          icon="minus"
                          :disabled="outBillGenerated"
                          @click.stop="handleGenerateOutBill"
                          style="margin-top: 8px;">
                  生成出库单
                </a-button>
              </template>
            </div>
          </a-col>
        </a-row>

        <!-- 商品明细表格 - 只读展示 -->
        <a-table
          style="margin-top: 16px;"
          :columns="columns"
          :dataSource="displayDataSource"
          :loading="loading"
          :pagination="false"
          :scroll="{ x: 1500, y: 400 }"
          bordered
          size="middle">
          <template slot="footer">
            <div style="text-align: right; padding: 8px 0;">
              <span style="margin-right: 24px; color: #52c41a;">
                🟢 盘盈数量: <strong>{{ surplusNumber }}</strong>
              </span>
              <span style="margin-right: 24px; color: #52c41a;">
                盘盈金额: <strong>{{ surplusAmount }}</strong>
              </span>
              <span style="margin-right: 24px; color: #ff4d4f;">
                🔴 盘亏数量: <strong>{{ lossNumber }}</strong>
              </span>
              <span style="color: #ff4d4f;">
                盘亏金额: <strong>{{ lossAmount }}</strong>
              </span>
            </div>
          </template>
        </a-table>

        <a-row class="form-row" :gutter="24" style="margin-top: 16px;">
          <a-col :lg="24" :md="24" :sm="24">
            <a-form-item :labelCol="labelCol" :wrapperCol="{xs: { span: 24 },sm: { span: 24 }}" label="备注">
              <a-textarea :value="model.remark" :rows="2" disabled />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row class="form-row" :gutter="24" v-if="fileList && fileList.length > 0">
          <a-col :lg="6" :md="12" :sm="24">
            <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" label="附件">
              <j-upload v-model="fileList" bizPath="bill" :disabled="true" :buttonVisible="false"></j-upload>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-spin>
  </j-modal>
</template>

<script>
import JUpload from '@/components/jeecg/JUpload'
import { getCheckDetail, submitOnlyCheck, submitAndApproveCheck, DepotCheckStatus } from '@/api/depotCheck'

export default {
  name: "StockCheckViewModal",
  components: {
    JUpload
  },
  data() {
    return {
      title: "查看盘点单",
      width: '1600px',
      visible: false,
      loading: false,
      submitLoading: false,
      inBillGenerated: false,
      outBillGenerated: false,
      model: {},
      fileList: [],
      prefixNo: 'PDFP',
      DepotCheckStatus, // 状态枚举
      labelCol: {
        xs: { span: 24 },
        sm: { span: 8 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 16 },
      },
      form: this.$form.createForm(this),
      columns: [
        { title: '条码', dataIndex: 'barCode', width: 150, fixed: 'left' },
        { title: '名称', dataIndex: 'name', width: 150 },
        { title: '规格', dataIndex: 'standard', width: 120 },
        { title: '类别', dataIndex: 'categoryName', width: 100 },
        { title: '单位', dataIndex: 'unit', width: 80 },
        { title: '多属性', dataIndex: 'sku', width: 100 },
        { title: '账面数量', dataIndex: 'stock', width: 100, align: 'right' },
        { title: '盘点数量', dataIndex: 'operNumber', width: 100, align: 'right' },
        { title: '盈亏数量', dataIndex: 'diffNumber', width: 100, align: 'right' },
        { title: '盘点结果', dataIndex: 'checkResult', width: 100, align: 'center' },
        { title: '单价', dataIndex: 'unitPrice', width: 100, align: 'right' },
        { title: '盈亏金额', dataIndex: 'allPrice', width: 100, align: 'right' },
        { title: '备注', dataIndex: 'remark', width: 150 }
      ],
      allDataSource: [],
      displayDataSource: [],
      checkStatistics: {
        total: 0,
        checked: 0,
        unchecked: 0,
        surplus: 0,
        loss: 0
      },
      checkFilter: 'total'
    }
  },
  computed: {
    totalDiffNumber() {
      return this.allDataSource.reduce((sum, item) => sum + (item.diffNumber - 0), 0).toFixed(2)
    },
    totalAllPrice() {
      return this.allDataSource.reduce((sum, item) => sum + (item.allPrice - 0), 0).toFixed(2)
    },
    // 盘盈数量合计
    surplusNumber() {
      return this.allDataSource
        .filter(item => item.diffNumber > 0)
        .reduce((sum, item) => sum + (item.diffNumber - 0), 0)
        .toFixed(2)
    },
    // 盘盈金额合计
    surplusAmount() {
      return this.allDataSource
        .filter(item => item.diffNumber > 0)
        .reduce((sum, item) => sum + (item.allPrice - 0), 0)
        .toFixed(2)
    },
    // 盘亏数量合计（取绝对值）
    lossNumber() {
      return Math.abs(
        this.allDataSource
          .filter(item => item.diffNumber < 0)
          .reduce((sum, item) => sum + (item.diffNumber - 0), 0)
      ).toFixed(2)
    },
    // 盘亏金额合计（取绝对值）
    lossAmount() {
      return Math.abs(
        this.allDataSource
          .filter(item => item.diffNumber < 0)
          .reduce((sum, item) => sum + (item.allPrice - 0), 0)
      ).toFixed(2)
    }
  },
  methods: {
    show(record) {
      this.visible = true
      this.loading = true
      this.model = record

      // 重置禁用状态
      this.inBillGenerated = false
      this.outBillGenerated = false

      // 加载盘点明细
      this.loadDetail(record.id)
    },

    loadDetail(headerId) {
      this.loading = true
      getCheckDetail(headerId).then(res => {
        if (res && res.code === 200) {
          const data = res.data.data
          this.model.rkNumber = data.rkNumber
          this.model.ckNumber = data.ckNumber
          this.fileList = data.fileNames || []

          const items = data.items || []

          // 字段映射：接口返回的字段名 -> 页面使用的字段名
          const mappedItems = items.map(item => {
            // 先进行字段映射
            const mappedItem = {
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
              unitPrice: item.unitPrice,
              allPrice: item.diffAmount,
              remark: item.remark,
              materialId: item.materialId,
              // 保留原始字段用于生成出入库单
              model: item.standard,
              color: item.color,
              brand: item.brand,
              mfrs: item.mfrs,
              materialOther: item.materialOther,
              otherField1: item.otherField1,
              otherField2: item.otherField2,
              otherField3: item.otherField3,
              enableSerialNumber: item.enableSerialNumber,
              enableBatchNumber: item.enableBatchNumber,
              expiryNum: item.expiryNum,
              snList: item.snList,
              batchNumber: item.batchNumber,
              productionDate: item.productionDate,
              expirationDate: item.expirationDate
            }

            // 计算盘盈盘亏结果
            if (mappedItem.diffNumber === undefined) {
              mappedItem.checkResult = '⚪ 未盘'
            } else if (mappedItem.diffNumber > 0) {
              mappedItem.checkResult = '🟢 盘盈'
            } else if (mappedItem.diffNumber < 0) {
              mappedItem.checkResult = '🔴 盘亏'
            } else {
              mappedItem.checkResult = '🔵 盘平'
            }

            return mappedItem
          })

          this.allDataSource = mappedItems
          this.displayDataSource = [...mappedItems]
          this.updateCheckStatistics()
        } else {
          this.$message.error(res.data || '加载盘点明细失败')
        }
      }).catch(err => {
        this.$message.error('加载盘点明细失败: ' + err.message)
      }).finally(() => {
        this.loading = false
      })
    },

    filterByCheckResult(filterType) {
      if (this.checkFilter === filterType) {
        this.checkFilter = null
        this.displayDataSource = [...this.allDataSource]
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
      this.displayDataSource = this.allDataSource.filter(filter)
    },

    updateCheckStatistics() {
      const statistics = {
        total: 0,
        checked: 0,
        unchecked: 0,
        surplus: 0,
        loss: 0
      }

      this.allDataSource.forEach(item => {
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

    handleCancel() {
      this.visible = false
      this.model = {}
      this.allDataSource = []
      this.displayDataSource = []
      this.checkFilter = 'total'
      this.$emit('close')
    },

    // 提交盘点的通用方法
    async submitCheck(withApprove = false) {
      const unCheckedItems = this.allDataSource.filter(item =>
        !item.operNumber && item.operNumber !== 0
      )

      if (unCheckedItems.length > 0) {
        this.$message.error(`还有商品未盘点，请完成所有商品的盘点后再提交${withApprove ? '审核' : ''}`)
        return
      }

      this.submitLoading = true
      try {
        const requestData = {
          id: this.model.id,
          depotId: this.model.depotId,
          checkNumber: this.model.checkNumber,
          checkDate: this.model.checkDate,
          remark: this.model.remark || '',
          items: this.allDataSource.map(item => ({
            materialId: item.materialId,
            actualNumber: item.operNumber,
            remark: item.remark || ''
          }))
        }

        const apiMethod = withApprove ? submitAndApproveCheck : submitOnlyCheck
        const successMsg = withApprove ? '盘点提交并审核成功' : '盘点提交成功，等待审核'

        const res = await apiMethod(requestData)
        if (res.code === 200) {
          this.$message.success(successMsg)
          this.$emit('ok')
          this.handleCancel()
        } else {
          this.$message.error(res.data || '提交失败')
        }
      } catch (error) {
        console.error('提交盘点失败:', error)
        this.$message.error('操作失败: ' + (error.message || '未知错误'))
      } finally {
        this.submitLoading = false
      }
    },
    // 提交盘点（不审核）
    async handleSubmitCheck() {
      await this.submitCheck(false)
    },
    // 提交盘点并审核
    async handleSubmitAndApprove() {
      await this.submitCheck(true)
    },

    // 生成出入库单的通用方法
    generateBill(billType) {
      const isInBill = billType === 'in'
      const items = this.allDataSource.filter(item =>
        isInBill ? item.diffNumber > 0 : item.diffNumber < 0
      )

      if (items.length === 0) {
        this.$message.warning(`没有盘${isInBill ? '盈' : '亏'}商品,无法生成${isInBill ? '入' : '出'}库单`)
        return
      }

      if (isInBill) {
        this.inBillGenerated = true
      } else {
        this.outBillGenerated = true
      }

      this.$emit(isInBill ? 'generateInBill' : 'generateOutBill', {
        stockCheckData: this.model,
        items: items
      })
    },
    // 生成其它入库单(盘盈)
    handleGenerateInBill() {
      this.generateBill('in')
    },
    // 生成其它出库单(盘亏)
    handleGenerateOutBill() {
      this.generateBill('out')
    },
    // 查看出入库单的通用方法
    viewBill(billType) {
      const billNumber = billType === 'in' ? this.model.rkNumber : this.model.ckNumber
      if (!billNumber) {
        this.$message.warning(`${billType === 'in' ? '入' : '出'}库单号不存在`)
        return
      }

      this.$emit(billType === 'in' ? 'viewInBill' : 'viewOutBill', {
        billNumber: billNumber
      })
    },
    // 查看入库单
    handleViewInBill() {
      this.viewBill('in')
    },
    // 查看出库单
    handleViewOutBill() {
      this.viewBill('out')
    }
  }
}
</script>

<style scoped>
</style>