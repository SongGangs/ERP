<template>
  <a-row :gutter="24">
    <a-col :md="24">
      <a-card :style="cardStyle" :bordered="false">
        <!-- 查询区域 -->
        <div class="table-page-search-wrapper">
          <!-- 搜索区域 -->
          <a-form layout="inline" @keyup.enter.native="searchQuery">
            <a-row :gutter="24">
              <a-col :md="6" :sm="24">
                <a-form-item label="盘点编号" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-input placeholder="请输入盘点编号" v-model="queryParam.checkNumber"></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="6" :sm="24">
                <a-form-item label="仓库名称" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-select placeholder="请选择仓库" showSearch allow-clear optionFilterProp="children" v-model="queryParam.depotId">
                    <a-select-option v-for="(depot,index) in depotList" :key="index" :value="depot.id">
                      {{ depot.depotName }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :md="6" :sm="24">
                <a-form-item label="盘点日期" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-range-picker
                    style="width:100%"
                    v-model="queryParam.createTimeRange"
                    format="YYYY-MM-DD"
                    :placeholder="['开始时间', '结束时间']"
                    :disabledDate="disabledDate"
                    @change="onDateChange"
                    @ok="onDateOk"
                  />
                </a-form-item>
              </a-col>
              <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
                <a-col :md="6" :sm="24">
                  <a-button type="primary" @click="searchQuery">查询</a-button>
                  <a-button style="margin-left: 8px" @click="searchReset">重置</a-button>
                  <a @click="handleToggleSearch" style="margin-left: 8px">
                    {{ toggleSearchStatus ? '收起' : '展开' }}
                    <a-icon :type="toggleSearchStatus ? 'up' : 'down'"/>
                  </a>
                </a-col>
              </span>
            </a-row>
            <template v-if="toggleSearchStatus">
              <a-row :gutter="24">
                <a-col :md="6" :sm="24">
                  <a-form-item label="盘点状态" :labelCol="labelCol" :wrapperCol="wrapperCol">
                    <a-select placeholder="请选择盘点状态" allow-clear v-model="queryParam.status">
                      <a-select-option :value="DepotCheckStatus.CHECKING">{{ DepotCheckStatusName[DepotCheckStatus.CHECKING] }}</a-select-option>
                      <a-select-option :value="DepotCheckStatus.CHECKED_UNAPPROVED">{{ DepotCheckStatusName[DepotCheckStatus.CHECKED_UNAPPROVED] }}</a-select-option>
                      <a-select-option :value="DepotCheckStatus.CHECKED_APPROVED">{{ DepotCheckStatusName[DepotCheckStatus.CHECKED_APPROVED] }}</a-select-option>
                      <a-select-option :value="DepotCheckStatus.ADJUSTED">{{ DepotCheckStatusName[DepotCheckStatus.ADJUSTED] }}</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <a-form-item label="操作人" :labelCol="labelCol" :wrapperCol="wrapperCol">
                    <a-select placeholder="请选择操作人" showSearch allow-clear optionFilterProp="children" v-model="queryParam.operatorId">
                      <a-select-option v-for="(item,index) in userList" :key="index" :value="item.id">
                        {{ item.userName }}
                      </a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
              </a-row>
            </template>
          </a-form>
        </div>
        <!-- 操作按钮区域 -->
        <div class="table-operator"  style="margin-top: 5px">
          <a-button v-if="btnEnableList.indexOf(1)>-1" @click="myHandleAdd" type="primary" icon="plus">新增</a-button>
          <a-button v-if="btnEnableList.indexOf(2)>-1" icon="check" @click="batchApprove" >审核</a-button>
          <a-button v-if="btnEnableList.indexOf(2)>-1" icon="close" @click="batchUnApprove">反审核</a-button>
          <a-button v-if="btnEnableList.indexOf(1)>-1" icon="delete" @click="batchDel">删除</a-button>
          <a-popover trigger="click" placement="right">
            <template slot="content">
              <a-checkbox-group @change="onColChange" v-model="settingDataIndex" :defaultValue="settingDataIndex">
                <a-row style="width: 500px">
                  <template v-for="(item,index) in defColumns">
                    <template>
                      <a-col :span="8">
                        <a-checkbox :value="item.dataIndex">
                          <j-ellipsis :value="item.title" :length="10"></j-ellipsis>
                        </a-checkbox>
                      </a-col>
                    </template>
                  </template>
                </a-row>
                <a-row style="padding-top: 10px;">
                  <a-col>
                    恢复默认列配置:<a-button @click="handleRestDefault" type="link" size="small">恢复默认</a-button>
                  </a-col>
                </a-row>
              </a-checkbox-group>
            </template>
            <a-button icon="setting">列设置</a-button>
          </a-popover>
          <a-tooltip placement="left" title="盘点功能用于定期核对库存,记录账面数量、实际数量和盘盈盘亏情况" slot="action">
            <a-icon v-if="btnEnableList.indexOf(1)>-1" type="question-circle" style="font-size:20px;float:right;" />
          </a-tooltip>
        </div>
        <!-- table区域-begin -->
        <div>
          <a-table
            ref="table"
            size="middle"
            bordered
            rowKey="id"
            :columns="columns"
            :dataSource="dataSource"
            :components="handleDrag(columns)"
            :pagination="ipagination"
            :scroll="scroll"
            :loading="loading"
            :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
            @change="handleTableChange">
            <span slot="action" slot-scope="text, record">
              <a @click="myHandleView(record)">查看</a>
              <!-- 状态为 1(盘点中) 或 2(已盘点未审核) 时可以盘点和编辑 -->
              <template v-if="record.status === DepotCheckStatus.CHECKING || record.status === DepotCheckStatus.CHECKED_UNAPPROVED">
                <a-divider v-if="btnEnableList.indexOf(1)>-1" type="vertical" />
                <a v-if="btnEnableList.indexOf(1)>-1" @click="myHandleEdit(record)">编辑</a>
                <a-divider v-if="btnEnableList.indexOf(1)>-1" type="vertical" />
                <a-popconfirm v-if="btnEnableList.indexOf(1)>-1" title="确定删除吗?" @confirm="() => myHandleDelete(record)">
                  <a>删除</a>
                </a-popconfirm>
              </template>
            </span>
            <template slot="customRenderStatus" slot-scope="status">
              <a-tag v-if="status === DepotCheckStatus.CHECKING" color="blue">{{ DepotCheckStatusName[DepotCheckStatus.CHECKING] }}</a-tag>
              <a-tag v-if="status === DepotCheckStatus.CHECKED_UNAPPROVED" color="orange">{{ DepotCheckStatusName[DepotCheckStatus.CHECKED_UNAPPROVED] }}</a-tag>
              <a-tag v-if="status === DepotCheckStatus.CHECKED_APPROVED" color="green">{{ DepotCheckStatusName[DepotCheckStatus.CHECKED_APPROVED] }}</a-tag>
              <a-tag v-if="status === DepotCheckStatus.ADJUSTED" color="purple">{{ DepotCheckStatusName[DepotCheckStatus.ADJUSTED] }}</a-tag>
            </template>
          </a-table>
        </div>
        <!-- table区域-end -->
        <!-- 表单区域 -->
        <stock-check-modal ref="modalForm" @ok="modalFormOk" @close="modalFormClose"></stock-check-modal>
        <stock-check-view-modal ref="modalView" @close="modalFormClose" @generateInBill="handleGenerateInBill" @generateOutBill="handleGenerateOutBill" @viewInBill="handleViewInBill" @viewOutBill="handleViewOutBill"></stock-check-view-modal>
        <bill-detail ref="modalDetail" @ok="modalFormOk" @close="modalFormClose"></bill-detail>
        <bill-excel-iframe ref="billExcelIframe" @ok="modalFormOk" @close="modalFormClose"></bill-excel-iframe>

        <!-- 选择仓库对话框 -->
        <a-modal
          title="选择盘点仓库和日期"
          :visible="selectDepotVisible"
          :confirmLoading="selectDepotLoading"
          @ok="handleSelectDepotOk"
          @cancel="handleSelectDepotCancel"
          :width="500">
          <a-form-model ref="selectDepotForm" :model="selectDepotForm" :label-col="{span: 6}" :wrapper-col="{span: 16}">
            <a-form-model-item label="选择仓库" prop="depotId" :rules="[{ required: true, message: '请选择仓库' }]">
              <a-select
                v-model="selectDepotForm.depotId"
                placeholder="请选择要盘点的仓库"
                show-search
                option-filter-prop="children">
                <a-select-option v-for="depot in depotList" :key="depot.id" :value="depot.id">
                  {{ depot.depotName }}
                </a-select-option>
              </a-select>
            </a-form-model-item>
            <a-form-model-item label="盘点日期" prop="checkDate" :rules="[{ required: true, message: '请选择盘点日期' }]">
              <a-date-picker
                v-model="selectDepotForm.checkDate"
                placeholder="请选择盘点日期"
                format="YYYY-MM-DD"
                style="width: 100%"
                :disabledDate="disabledDate" />
            </a-form-model-item>
          </a-form-model>
        </a-modal>
      </a-card>
    </a-col>
  </a-row>
</template>
<script>
  import StockCheckModal from './modules/StockCheckModal'
  import StockCheckViewModal from './modules/StockCheckViewModal'
  import BillDetail from './dialog/BillDetail'
  import BillExcelIframe from '@/components/tools/BillExcelIframe'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import { BillListMixin } from './mixins/BillListMixin'
  import JEllipsis from '@/components/jeecg/JEllipsis'
  import JDate from '@/components/jeecg/JDate'
  import { TabParamsMixin } from '@/mixins/TabParamsMixin'
  import { TabLinkMixin } from '@/mixins/TabLinkMixin'
  import { getCheckList, batchDeleteCheck, batchApproveCheck, batchUnApproveCheck, DepotCheckStatus, DepotCheckStatusName } from '@/api/depotCheck'
  import { findBillDetailByNumber } from '@/api/api'
  import moment from 'moment/moment'
  import { getFormatDate, getPrevMonthFormatDate } from '@/utils/util'
  export default {
    name: "StockCheckList",
    mixins: [JeecgListMixin, BillListMixin, TabParamsMixin, TabLinkMixin],
    components: {
      StockCheckModal,
      StockCheckViewModal,
      BillDetail,
      BillExcelIframe,
      JEllipsis,
      JDate,
      VNodes: {
        functional: true,
        render: (h, ctx) => ctx.props.vnodes,
      }
    },
    data () {
      return {
        disableDefaultQuery: true,
        // 状态枚举
        DepotCheckStatus,
        DepotCheckStatusName,
        // 查询条件
        queryParam: {
          checkNumber: "",
          depotId: undefined,
          status: undefined,
          operatorId: undefined,
          materialInfo: "",
          beginTime: undefined,
          endTime: undefined
        },
        prefixNo: 'PDFP',
        depotList: [],
        urlPath: '/bill/stock_check',
        // 选择仓库对话框
        selectDepotVisible: false,
        selectDepotLoading: false,
        selectDepotForm: {
          depotId: undefined,
          checkDate: undefined
        },
        labelCol: {
          span: 5
        },
        wrapperCol: {
          span: 18,
          offset: 1
        },
        // 默认索引
        defDataIndex:['action','checkNumber','status','depotName','checkDate','operatorName','shouldCheckNumber','checkedNumber','uncheckNumber',
          'originNumber','actualNumber','originAmount','actualAmount','diffAmount','lossNumber','lossAmount','profitNumber','profitAmount','remark'],
        // 默认列
        defColumns: [
          {
            title: '操作',
            dataIndex: 'action',
            align:"center", width: 200,
            scopedSlots: { customRender: 'action' },
          },
          { title: '盘点编号', dataIndex: 'checkNumber', width: 160 },
          { title: '盘点状态', dataIndex: 'status', width: 120, align: "center",
            scopedSlots: { customRender: 'customRenderStatus' }
          },
          { title: '盘点仓库', dataIndex: 'depotName', width: 120 },
          { title: '盘点日期', dataIndex: 'checkDate', width: 110 },
          { title: '操作人', dataIndex: 'operatorName', width: 100 },
          { title: '应盘个数', dataIndex: 'shouldCheckNumber', width: 90, align: 'center' },
          { title: '已盘个数', dataIndex: 'checkedNumber', width: 90, align: 'center' },
          { title: '未盘个数', dataIndex: 'uncheckNumber', width: 90, align: 'center' },
          { title: '账面个数', dataIndex: 'originNumber', width: 90, align: 'center' },
          { title: '盘点个数', dataIndex: 'actualNumber', width: 90, align: 'center' },
          { title: '账面金额', dataIndex: 'originAmount', width: 110, align: 'center' },
          { title: '盘点金额', dataIndex: 'actualAmount', width: 110, align: 'center' },
          { title: '差异金额', dataIndex: 'diffAmount', width: 110, align: 'center' },
          { title: '盘亏个数', dataIndex: 'lossNumber', width: 90, align: 'center' },
          { title: '盘亏金额', dataIndex: 'lossAmount', width: 110, align: 'center' },
          { title: '盘盈个数', dataIndex: 'profitNumber', width: 90, align: 'center' },
          { title: '盘盈金额', dataIndex: 'profitAmount', width: 110, align: 'center' },
          { title: '备注', dataIndex: 'remark', width: 200 }
        ],
        url: {
          list: "/depotCheck/list",
          batchDelete: "/depotCheck/delete"
        }
      }
    },
    computed: {
    },
    created() {
      this.initSystemConfig()
      this.getDepotData()
      this.initUser()
      // 初始加载数据
      this.searchQuery()
    },
    methods: {
      //禁用未来日期
      disabledDate(current) {
        // 不能选择超过今天的日期
        return current && current > moment().endOf('day')
      },
      // 应用路由参数（TabParamsMixin 要求实现）
      applyRouteParams(params) {
        // StockCheckList 目前不需要处理 URL 参数
        // 如果将来需要支持从其他页面跳转并带参数，可以在这里实现
        // 例如：
        // if (params.depotId) {
        //   this.queryParam.depotId = parseInt(params.depotId)
        //   this.loadData(1)
        // }
      },
      searchQuery() {
        const params = {
          currentPage: this.ipagination.current,
          pageSize: this.ipagination.pageSize
        }

        if (this.queryParam.checkNumber) {
          params.checkNumber = this.queryParam.checkNumber
        }
        if (this.queryParam.depotId) {
          params.depotId = this.queryParam.depotId
        }
        if (this.queryParam.status) {
          params.status = this.queryParam.status
        }
        if (this.queryParam.operatorId) {
          params.operatorId = this.queryParam.operatorId
        }

        // 处理日期范围
        if (this.queryParam.createTimeRange && this.queryParam.createTimeRange.length === 2) {
          params.beginTime = this.queryParam.createTimeRange[0].format('YYYY-MM-DD')
          params.endTime = this.queryParam.createTimeRange[1].format('YYYY-MM-DD')
        }

        this.loading = true
        getCheckList(params).then(res => {
          if (res.code === 200) {
            this.dataSource = res.data.rows || []
            this.ipagination.total = res.data.total || 0
          } else {
            this.$message.error(res.data.msg || '查询失败')
          }
        }).finally(() => {
          this.loading = false
        })
      },
      searchReset() {
        this.queryParam = {
          checkNumber: "",
          depotId: undefined,
          status: undefined,
          operatorId: undefined,
          materialInfo: "",
          beginTime: undefined,
          endTime: undefined,
          createTimeRange: [moment(getPrevMonthFormatDate(3)), moment(getFormatDate())]
        }
        this.ipagination.current = 1
        this.searchQuery()
      },
      // 查看盘点单
      myHandleView(record) {
        this.$refs.modalView.show(record)
      },
      // 盘点或编辑盘点单（合并重复逻辑）
      myHandleCheck(record) {
        this.openEditModal(record)
      },
      myHandleEdit(record) {
        this.openEditModal(record)
      },
      // 打开编辑模态框的通用方法
      openEditModal(record) {
        if (record.status !== DepotCheckStatus.CHECKING && record.status !== DepotCheckStatus.CHECKED_UNAPPROVED) {
          this.$message.warning('只有盘点中或已盘点（未审核）的单据才能编辑！')
          return
        }
        this.$refs.modalForm.action = "edit"
        this.$refs.modalForm.priceLimit = this.priceLimit
        this.$refs.modalForm.isCanCheck = this.btnEnableList.indexOf(2) !== -1
        this.$refs.modalForm.edit(record)
      },
      // 重写新增方法,先选择仓库
      myHandleAdd() {
        this.selectDepotForm = {
          depotId: undefined,
          checkDate: moment()
        }
        this.selectDepotVisible = true
      },
      // 选择仓库确定
      handleSelectDepotOk() {
        this.$refs.selectDepotForm.validate(valid => {
          if (valid) {
            this.selectDepotVisible = false
            this.$refs.modalForm.action = "add"
            this.$refs.modalForm.priceLimit = this.priceLimit
            this.$refs.modalForm.isCanCheck = this.btnEnableList.indexOf(2) !== -1
            this.$refs.modalForm.selectedDepotId = this.selectDepotForm.depotId
            this.$refs.modalForm.selectedCheckDate = this.selectDepotForm.checkDate.format('YYYY-MM-DD')
            this.handleAdd()
          }
        })
      },
      // 取消选择仓库
      handleSelectDepotCancel() {
        this.selectDepotVisible = false
        this.selectDepotForm = {
          depotId: undefined,
          checkDate: undefined
        }
      },
      myHandleDelete(record) {
        if(record.status !== DepotCheckStatus.CHECKING && record.status !== DepotCheckStatus.CHECKED_UNAPPROVED) {
          this.$message.warning("抱歉,只有盘点中或已盘点（未审核）的单据才能删除!")
          return
        }

        batchDeleteCheck([record.id]).then((res) => {
          if(res.code === 200){
            this.$message.success('删除成功')
            this.searchQuery()
          } else {
            this.$message.warning(res.data || '删除失败');
          }
        })
      },
      // 批量审核/反审核通用方法
      batchOperation(operationName, apiMethod) {
        if (this.selectedRowKeys.length <= 0) {
          this.$message.warning(`请选择要${operationName}的记录!`)
          return
        }

        this.$confirm({
          title: `确认${operationName}`,
          content: `是否批量${operationName}选中的 ${this.selectedRowKeys.length} 条盘点单?`,
          onOk: () => {
            this.loading = true
            apiMethod(this.selectedRowKeys).then(res => {
              if (res && res.code === 200) {
                this.$message.success(`成功${operationName}${this.selectedRowKeys.length}条盘点单`)
                this.selectedRowKeys = []
                this.searchQuery()
              } else {
                this.$message.error(res.data || `批量${operationName}失败`)
              }
            }).finally(() => {
              this.loading = false
            })
          }
        })
      },
      // 批量审核
      batchApprove() {
        this.batchOperation('审核', batchApproveCheck)
      },
      // 批量反审核
      batchUnApprove() {
        this.batchOperation('反审核', batchUnApproveCheck)
      },
      batchDel() {
        if (this.selectedRowKeys.length <= 0) {
          this.$message.warning('请选择一条记录!')
          return
        }

        this.$confirm({
          title: "确认删除",
          content: "是否删除选中数据?",
          onOk: () => {
            this.loading = true
            batchDeleteCheck(this.selectedRowKeys).then(res => {
              if (res && res.code === 200) {
                this.$message.success(`成功删除${this.selectedRowKeys.length}条记录`)
                this.selectedRowKeys = []
                this.searchQuery()
              } else {
                this.$message.error(res.data)
              }
            }).finally(() => {
              this.loading = false
            })
          }
        })
      },
      // 生成出入库单的通用方法
      generateBill(data, billType) {
        const { stockCheckData, items } = data
        const isInBill = billType === '入库'

        const billItems = items.map((item) => ({
          barCode: item.barCode,
          operNumber: Math.abs(item.diffNumber),
          remark: `盘点盘${isInBill ? '盈' : '亏'}自动生成`
        }))

        const billData = {
          type: billType,
          subType: '其它',
          operTime: moment().format('YYYY-MM-DD HH:mm:ss'),
          depotId: stockCheckData.depotId,
          remark: `由盘点单 ${stockCheckData.checkNumber} 自动生成`,
          linkNumber: stockCheckData.checkNumber,
          _needFetchMaterial: true,
          rows: billItems
        }

        const storageKey = isInBill ? 'otherInBillData' : 'otherOutBillData'
        const routePath = isInBill ? '/bill/other_in' : '/bill/other_out'
        const tabTitle = isInBill ? '其它入库' : '其它出库'
        const tabName = isInBill ? 'OtherInList' : 'OtherOutList'

        sessionStorage.setItem(storageKey, JSON.stringify(billData))
        this.openInNewTab(routePath, { from: 'stock_check', action: 'add', t: Date.now() }, tabTitle, tabName)
      },
      // 处理生成其它入库单
      handleGenerateInBill(data) {
        this.generateBill(data, '入库')
      },
      // 处理生成其它出库单
      handleGenerateOutBill(data) {
        this.generateBill(data, '出库')
      },
      // 查看出入库单的通用方法
      viewBill(billNumber, billType, prefixNo) {
        findBillDetailByNumber({ number: billNumber }).then((res) => {
          if (res && res.code === 200) {
            this.$refs.modalDetail.isCanBackCheck = false
            this.$refs.modalDetail.show(res.data, billType, prefixNo, this.priceLimit)
            this.$refs.modalDetail.title = `${billType}单详情`
          } else {
            this.$message.error(`未找到${billType}单: ${billNumber}`)
          }
        }).catch(err => {
          console.error(`查询${billType}单失败:`, err)
          this.$message.error(`查询${billType}单失败`)
        })
      },
      // 查看入库单
      handleViewInBill(data) {
        this.viewBill(data.billNumber, '其它入库', 'QTRK')
      },
      // 查看出库单
      handleViewOutBill(data) {
        this.viewBill(data.billNumber, '其它出库', 'QTCK')
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less'
</style>