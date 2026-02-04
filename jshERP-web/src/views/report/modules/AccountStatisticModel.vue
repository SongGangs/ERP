<template>
  <div ref="container">
    <a-modal
      :title="title"
      :width="width"
      :visible="visible"
      :getContainer="() => $refs.container"
      :maskStyle="{'top':'93px','left':'154px'}"
      :wrapClassName="wrapClassNameInfo()"
      :mask="isDesktop()"
      :maskClosable="false"
      @cancel="handleCancel"
      cancelText="关闭"
      style="top:20px;height: 95%;">
      <template slot="footer">
        <a-button key="back" @click="handleCancel">取消</a-button>
      </template>
      <!-- 查询区域 -->
      <div class="table-page-search-wrapper">
        <!-- 搜索区域 -->
        <a-form layout="inline" @keyup.enter.native="searchQuery">
          <a-row :gutter="24">
            <a-col :md="12" :sm="24">
              <a-form-item label="统计日期" :labelCol="labelCol" :wrapperCol="wrapperCol">
                <a-range-picker
                  style="width:100%"
                  v-model="queryParam.createTimeRange"
                  format="YYYY-MM-DD"
                  :placeholder="['开始时间', '结束时间']"
                  :allowClear="false"
                  @change="onDateChange"
                />
              </a-form-item>
              <!-- 月份快速选择按钮 -->
              <div class="month-quick-select">
                <div class="quick-select-buttons">
                  <a-button size="small" @click="selectMonth(0)" :type="selectedQuickMonth === 0 ? 'primary' : 'default'">本月</a-button>
                  <a-button size="small" @click="selectMonth(-1)" :type="selectedQuickMonth === -1 ? 'primary' : 'default'">上月</a-button>
                  <a-button size="small" @click="selectMonth(-2)" :type="selectedQuickMonth === -2 ? 'primary' : 'default'">上上月</a-button>
                  <a-button size="small" @click="selectMonth(-3)" :type="selectedQuickMonth === -3 ? 'primary' : 'default'">三月前</a-button>
                </div>
              </div>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-button type="primary" @click="searchQuery">查询</a-button>
              <a-button style="margin-left: 8px" @click="searchReset">重置</a-button>
              <a-button style="margin-left: 8px" @click="exportExcel" icon="download">导出</a-button>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <!-- 统计表格区域 -->
      <div class="statistic-container">
        <!-- 账户信息标题 -->
        <div class="account-header">
          <span>账户: {{ statisticData ? statisticData.accountName : '' }}</span>
          <span style="margin-left: 50px">日期: {{ queryParam.beginDate }} 至 {{ queryParam.endDate }}</span>
        </div>

        <!-- 现金流水账明细 -->
        <div class="statistic-section-title">现金流水账明细</div>
        <a-row :gutter="16">
          <a-col :span="12">
            <div class="expense-title">收入</div>
            <table class="statistic-table">
              <tbody>
              <tr>
                <td :rowspan="incomeData.length" class="sub-category-cell">
                  <a @click="jumpToItemInList(undefined)" class="link-text">收入单</a>
                </td>
                <td class="item-cell">
                  <a @click="jumpToItemInList(incomeData[0] ? incomeData[0].itemCode : undefined)" class="link-text">{{ incomeData[0] ? incomeData[0].itemName : '' }}</a></td>
                <td class="amount-cell">{{ incomeData[0] ? incomeData[0].amount : '' }}</td>
                <td :rowspan="incomeData.length" class="total-amount-cell" style="background: #e6f7ff; font-weight: bold;">{{ incomeTotalAmount }}</td>
              </tr>
              <tr v-for="(item, index) in incomeData.slice(1)" :key="'profit-income-' + index">
                <td class="item-cell">
                  <a @click="jumpToItemInList(item.itemCode)" class="link-text">{{ item.itemName }}</a></td>
                <td class="amount-cell">{{ item.amount }}</td>
              </tr>
              </tbody>
            </table>
          </a-col>

          <a-col :span="12">
            <div class="expense-title">支出</div>
            <table class="statistic-table">
              <tbody>
              <!-- 支出单(已付款) -->
              <template v-if="paidOutData.length > 0">
                <tr>
                  <td :rowspan="paidOutData.length" class="sub-category-cell">
                    <a @click="jumpToItemOutList('1', undefined)" class="link-text">支出单(已付款)</a>
                    <span class="percentage-text">{{ calculatePercentage(paidOutTotalAmount) }}</span>
                  </td>
                  <td class="item-cell">
                    <a @click="jumpToItemOutList('1', paidOutData[0].itemCode)"
                       class="link-text">{{ paidOutData[0].itemName }}</a>
                    <span class="percentage-text">{{ calculatePercentage(paidOutData[0].amount) }}</span>
                  </td>
                  <td class="amount-cell">{{ paidOutData[0].amount }}</td>
                  <td :rowspan="paidOutData.length" class="total-amount-cell" style="background: #fff1f0; font-weight: bold;">{{ paidOutTotalAmount }}</td>
                </tr>
                <tr v-for="(item, index) in paidOutData.slice(1)" :key="'paidOut-' + index">
                  <td class="item-cell">
                    <a @click="jumpToItemOutList('1', item.itemCode)" class="link-text">{{ item.itemName }}</a>
                    <span class="percentage-text">{{ calculatePercentage(item.amount) }}</span>
                  </td>
                  <td class="amount-cell">{{ item.amount }}</td>
                </tr>
              </template>

              <!-- 付款单(已支付) -->
              <template v-if="paidPaymentData.length > 0">
                <tr>
                  <td :rowspan="paidPaymentData.length" class="sub-category-cell">
                    <a @click="jumpToOtherInList(undefined)" class="link-text">付款单(已支付)</a>
                    <span class="percentage-text">{{ calculatePercentage(paidPaymentTotalAmount) }}</span>
                  </td>
                  <td class="item-cell">
                    {{ paidPaymentData[0].itemName }}
                    <span class="percentage-text">{{ calculatePercentage(paidPaymentData[0].amount) }}</span>
                  </td>
                  <td class="amount-cell">{{ paidPaymentData[0].amount }}</td>
                  <td :rowspan="paidPaymentData.length" class="total-amount-cell" style="background: #fff1f0; font-weight: bold;">{{ paidPaymentTotalAmount }}</td>
                </tr>
                <tr v-for="(item, index) in paidPaymentData.slice(1)" :key="'paidPayment-' + index">
                  <td class="item-cell">
                    {{ item.itemName }}
                    <span class="percentage-text">{{ calculatePercentage(item.amount) }}</span>
                  </td>
                  <td class="amount-cell">{{ item.amount }}</td>
                </tr>
              </template>

              <!-- 付款单(待支付) -->
              <template v-if="unPaidPaymentData.length > 0">
                <tr>
                  <td :rowspan="unPaidPaymentData.length" class="sub-category-cell">
                    <a @click="jumpToOtherInList(undefined)" class="link-text">付款单(待支付)</a>
                    <span class="percentage-text">{{ calculatePercentage(unPaidPaymentTotalAmount) }}</span>
                  </td>
                  <td class="item-cell">
                    {{ unPaidPaymentData[0].itemName }}
                    <span class="percentage-text">{{ calculatePercentage(unPaidPaymentData[0].amount) }}</span>
                  </td>
                  <td class="amount-cell">{{ unPaidPaymentData[0].amount }}</td>
                  <td :rowspan="unPaidPaymentData.length" class="total-amount-cell" style="background: #fff1f0; font-weight: bold;">{{ unPaidPaymentTotalAmount }}</td>
                </tr>
                <tr v-for="(item, index) in unPaidPaymentData.slice(1)" :key="'unPaidPayment-' + index">
                  <td class="item-cell">
                    {{ item.itemName }}
                    <span class="percentage-text">{{ calculatePercentage(item.amount) }}</span>
                  </td>
                  <td class="amount-cell">{{ item.amount }}</td>
                </tr>
              </template>

              <!-- 采购 -->
              <template v-if="purchaseData.length > 0">
                <tr>
                  <td :rowspan="purchaseData.length" class="sub-category-cell">
                    <a @click="jumpToPurchaseInList" class="link-text">采购</a>
                    <span class="percentage-text">{{ calculatePercentage(purchaseTotalAmount) }}</span>
                  </td>
                  <td class="item-cell">
                    {{ purchaseData[0].itemName }}
                    <span class="percentage-text">{{ calculatePercentage(purchaseData[0].amount) }}</span>
                  </td>
                  <td class="amount-cell">{{ purchaseData[0].amount }}</td>
                  <td :rowspan="purchaseData.length" class="total-amount-cell" style="background: #fff1f0; font-weight: bold;">{{ purchaseTotalAmount }}</td>
                </tr>
                <tr v-for="(item, index) in purchaseData.slice(1)" :key="'purchase-' + index">
                  <td class="item-cell">
                    {{ item.itemName }}
                    <span class="percentage-text">{{ calculatePercentage(item.amount) }}</span>
                  </td>
                  <td class="amount-cell">{{ item.amount }}</td>
                </tr>
              </template>
              </tbody>
            </table>
          </a-col>
        </a-row>

        <!-- 现金流水结余 -->
        <div class="balance-box">
          <span class="balance-label">现金流水结余: <span class="profit-amount">{{ cashBalance }}</span>元</span>
          <span class="balance-formula">公式：收入单-支出单(已付款)-付款单-采购订单</span>
        </div>

        <!-- 现金流水结余 -->
        <div class="balance-box">
          <span class="balance-label">当前打款: <span class="profit-amount">{{ paymentAmount }}</span>元</span>
          <span class="balance-formula">公式：{{ paymentFormula }}</span>
        </div>

        <!-- 利润账单明细 -->
        <div class="statistic-section-title" style="margin-top: 30px">利润账单明细</div>
        <a-row :gutter="16">
          <a-col :span="12">
            <div class="expense-title">收入</div>
            <table class="statistic-table">
              <tbody>
              <tr>
                <td :rowspan="incomeData.length" class="sub-category-cell">
                  <a @click="jumpToItemInList(undefined)" class="link-text">收入单</a>
                </td>
                <td class="item-cell">
                  <a @click="jumpToItemInList(incomeData[0] ? incomeData[0].itemCode : undefined)" class="link-text">{{ incomeData[0] ? incomeData[0].itemName : '' }}</a></td>
                <td class="amount-cell">{{ incomeData[0] ? incomeData[0].amount : '' }}</td>
                <td :rowspan="incomeData.length" class="total-amount-cell" style="background: #e6f7ff; font-weight: bold;">{{ incomeTotalAmount }}</td>
              </tr>
              <tr v-for="(item, index) in incomeData.slice(1)" :key="'income-' + index">
                <td class="item-cell">
                  <a @click="jumpToItemInList(item.itemCode)" class="link-text">{{ item.itemName }}</a></td>
                <td class="amount-cell">{{ item.amount }}</td>
              </tr>
              </tbody>
            </table>
          </a-col>

          <a-col :span="12">
            <div class="expense-title">支出</div>
            <table class="statistic-table">
              <tbody>
              <!-- 支出单(已付款) -->
              <template v-if="paidOutData.length > 0">
                <tr>
                  <td :rowspan="paidOutData.length" class="sub-category-cell">
                    <a @click="jumpToItemOutList('1', undefined)" class="link-text">支出单(已付款)</a>
                    <span class="percentage-text">{{ calculatePercentage(paidOutTotalAmount) }}</span>
                  </td>
                  <td class="item-cell">
                    <a @click="jumpToItemOutList('1', paidOutData[0].itemCode)" class="link-text">{{ paidOutData[0].itemName }}</a>
                    <span class="percentage-text">{{ calculatePercentage(paidOutData[0].amount) }}</span>
                  </td>
                  <td class="amount-cell">{{ paidOutData[0].amount }}</td>
                  <td :rowspan="paidOutData.length" class="total-amount-cell" style="background: #fff1f0; font-weight: bold;">{{ paidOutTotalAmount }}</td>
                </tr>
                <tr v-for="(item, index) in paidOutData.slice(1)" :key="'profit-paidOut-' + index">
                  <td class="item-cell">
                    <a @click="jumpToItemOutList('1', item.itemCode)" class="link-text">{{ item.itemName }}</a>
                    <span class="percentage-text">{{ calculatePercentage(item.amount) }}</span>
                  </td>
                  <td class="amount-cell">{{ item.amount }}</td>
                </tr>
              </template>

              <!-- 支出单(待付款) -->
              <template v-if="unPaidOutData.length > 0">
                <tr>
                  <td :rowspan="unPaidOutData.length" class="sub-category-cell">
                    <a @click="jumpToItemOutList('0', undefined)" class="link-text">支出单(待付款)</a>
                    <span class="percentage-text">{{ calculatePercentage(unPaidOutTotalAmount) }}</span>
                  </td>
                  <td class="item-cell">
                    <a @click="jumpToItemOutList('0', unPaidOutData[0].itemCode)" class="link-text">{{ unPaidOutData[0].itemName }}</a>
                    <span class="percentage-text">{{ calculatePercentage(unPaidOutData[0].amount) }}</span>
                  </td>
                  <td class="amount-cell">{{ unPaidOutData[0].amount }}</td>
                  <td :rowspan="unPaidOutData.length" class="total-amount-cell" style="background: #fff1f0; font-weight: bold;">{{ unPaidOutTotalAmount }}</td>
                </tr>
                <tr v-for="(item, index) in unPaidOutData.slice(1)" :key="'profit-unPaidOut-' + index">
                  <td class="item-cell">
                    <a @click="jumpToItemOutList('0', item.itemCode)" class="link-text">{{ item.itemName }}</a>
                    <span class="percentage-text">{{ calculatePercentage(item.amount) }}</span>
                  </td>
                  <td class="amount-cell">{{ item.amount }}</td>
                </tr>
              </template>

              <!-- 支出(出库) -->
              <template v-if="outStoreData.length > 0">
                <tr>
                  <td :rowspan="outStoreData.length" class="sub-category-cell">
                    <a @click="jumpToOtherOutList(undefined)" class="link-text">支出(出库)</a>
                    <span class="percentage-text">{{ calculatePercentage(outStoreTotalAmount) }}</span>
                  </td>
                  <td class="item-cell">
                    <a @click="jumpToOtherOutList(outStoreData[0].itemCode)" class="link-text">{{outStoreData[0].itemName}}</a>
                    <span class="percentage-text">{{ calculatePercentage(outStoreData[0].amount) }}</span>
                  </td>
                  <td class="amount-cell">{{ outStoreData[0].amount }}</td>
                  <td :rowspan="outStoreData.length" class="total-amount-cell" style="background: #fff1f0; font-weight: bold;">{{ outStoreTotalAmount }}</td>
                </tr>
                <tr v-for="(item, index) in outStoreData.slice(1)" :key="'outStore-' + index">
                  <td class="item-cell">
                    <a @click="jumpToOtherOutList(item.itemCode)" class="link-text">{{item.itemName}}</a>
                    <span class="percentage-text">{{ calculatePercentage(item.amount) }}</span>
                  </td>
                  <td class="amount-cell">{{ item.amount }}</td>
                </tr>
              </template>
              </tbody>
            </table>
          </a-col>
        </a-row>

        <!-- 利润 -->
        <div class="balance-box">
          <span class="balance-label">当前利润: <span class="profit-amount">{{ profit }}</span>元</span>
          <span class="balance-formula">公式：收入单-支出单-出库</span>
        </div>
      </div>
    </a-modal>
  </div>
</template>
<script>
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import { mixinDevice } from '@/utils/mixin'
import { TabLinkMixin } from '@/mixins/TabLinkMixin'
import JEllipsis from '@/components/jeecg/JEllipsis'
import { getFirstDayOfCurrentMonth, getFormatDate, getLastDayOfCurrentMonth } from '@/utils/util'
import { getAction } from '@/api/manage'
import moment from 'moment/moment'

export default {
  name: 'AccountStatisticModel',
  mixins: [JeecgListMixin, mixinDevice, TabLinkMixin],
  components: {
    JEllipsis
  },
  data() {
    return {
      title: '操作',
      width: '1200px',
      visible: false,
      disableMixinCreated: true,
      toFromType: '',
      currentAccountId: '',
      selectedQuickMonth: 0, // 当前选中的快速月份按钮
      // 查询条件
      queryParam: {
        accountId: '',
        beginDate: getFirstDayOfCurrentMonth(),
        endDate: getLastDayOfCurrentMonth(),
        createTimeRange: [moment(getFirstDayOfCurrentMonth()), moment(getLastDayOfCurrentMonth())]
      },
      tabKey: '1',
      pageName: 'accountStatisticModel',
      // 统计数据
      statisticData: null,
      // 现金流水账数据
      incomeData: [],
      paidOutData: [],
      unPaidOutData: [],
      purchaseData: [],
      outStoreData: [],
      paidPaymentData: [],
      unPaidPaymentData: [],
      companyOutData: [],
      // 现金结余
      cashBalance: 0,
      // 打款
      paymentAmount: 0,
      // 打款公式
      paymentFormula: '',
      // 利润
      profit: 0,
      // 各类别总金额
      incomeTotalAmount: 0,
      paidOutTotalAmount: 0,
      unPaidOutTotalAmount: 0,
      paidPaymentTotalAmount: 0,
      unPaidPaymentTotalAmount: 0,
      purchaseTotalAmount: 0,
      outStoreTotalAmount: 0,
      labelCol: {
        xs: { span: 1 },
        sm: { span: 2 }
      },
      wrapperCol: {
        xs: { span: 10 },
        sm: { span: 16 }
      },
      url: {
        list: '/account/statistic'
      }
    }
  },
  created() {
    this.initColumnsSetting()
  },
  methods: {
    loadData() {
      if (!this.url.list) {
        this.$message.error('请设置url.list属性!')
        return
      }
      let params = this.getQueryParams()
      this.loading = true
      getAction(this.url.list, params).then((res) => {
        if (res && res.code === 200) {
          // 处理统计数据
          this.statisticData = res.data
          this.processStatisticData()
        } else if (res.code === 500) {
          this.$message.warning(res.data)
        } else {
          this.$message.warning(res.data)
        }
        this.loading = false
      })
    },
    getQueryParams() {
      let param = Object.assign({}, this.queryParam, this.isorter)
      param.accountId = this.currentAccountId
      return param
    },
    show(record) {
      this.model = Object.assign({}, record)
      this.currentAccountId = record.id
      this.queryParam.accountId = record.id
      this.visible = true
      this.loadData()
    },
    close() {
      this.$emit('close')
      this.visible = false
    },
    handleCancel() {
      this.close()
    },
    onDateChange: function(value, dateString) {
      this.queryParam.beginDate = dateString[0]
      this.queryParam.endDate = dateString[1]
      // 手动选择日期时，清除快速选择的高亮
      this.selectedQuickMonth = null
    },
    selectMonth(monthOffset) {
      // monthOffset: 0=本月, -1=上月, -2=上上月, -3=三月前
      this.selectedQuickMonth = monthOffset
      const targetMonth = moment().add(monthOffset, 'months')
      const startDate = targetMonth.clone().startOf('month')
      const endDate = targetMonth.clone().endOf('month')

      this.queryParam.beginDate = startDate.format('YYYY-MM-DD')
      this.queryParam.endDate = endDate.format('YYYY-MM-DD')
      this.queryParam.createTimeRange = [startDate, endDate]

      // 自动触发查询
      this.loadData()
    },
    searchReset() {
      this.queryParam = {
        accountId: this.currentAccountId,
        beginDate: getFirstDayOfCurrentMonth(),
        endDate: getLastDayOfCurrentMonth(),
        createTimeRange: [moment(getFirstDayOfCurrentMonth()), moment(getLastDayOfCurrentMonth())]
      }
      this.selectedQuickMonth = 0
      this.loadData()
    },
    exportExcel() {
      if (!this.statisticData) {
        this.$message.warning('暂无数据可导出')
        return
      }

      let list = []

      // 添加账户信息标题
      list.push(['日期: ', this.queryParam.beginDate + ' 至 ' + this.queryParam.endDate])
      list.push([]) // 空行

      // ========== 现金流水账明细 ==========
      list.push(['现金流水账明细'])
      list.push([]) // 空行

      // 表头: 左侧收入,右侧支出
      list.push(['收入', '', '', '', '', '', '支出', '', ''])

      // 计算最大行数
      const cashFlowMaxRows = Math.max(
        this.incomeData.length,
        this.paidOutData.length + this.paidPaymentData.length +
        this.unPaidPaymentData.length + this.purchaseData.length
      )

      // 构建现金流水账数据
      let incomeIndex = 0
      let expenseIndex = 0

      for (let i = 0; i < cashFlowMaxRows; i++) {
        let row = []

        // 左侧收入数据
        if (i < this.incomeData.length) {
          if (i === 0) {
            row.push('收入单')
          } else {
            row.push('')
          }
          row.push(this.incomeData[i].itemName)
          row.push(this.incomeData[i].amount)
        } else {
          row.push('', '', '')
        }

        // 右侧支出数据
        let expenseItem = null
        let expenseCategory = ''

        // 支出单(已付款)
        if (expenseIndex < this.paidOutData.length) {
          expenseItem = this.paidOutData[expenseIndex]
          expenseCategory = expenseIndex === 0 ? '支出单(已付款)' : ''
          expenseIndex++
        }
        // 付款单(已支付)
        else if (expenseIndex < this.paidOutData.length + this.paidPaymentData.length) {
          const idx = expenseIndex - this.paidOutData.length
          expenseItem = this.paidPaymentData[idx]
          expenseCategory = idx === 0 ? '付款单(已支付)' : ''
          expenseIndex++
        }
        // 付款单(待支付)
        else if (expenseIndex < this.paidOutData.length + this.paidPaymentData.length + this.unPaidPaymentData.length) {
          const idx = expenseIndex - this.paidOutData.length - this.paidPaymentData.length
          expenseItem = this.unPaidPaymentData[idx]
          expenseCategory = idx === 0 ? '付款单(待支付)' : ''
          expenseIndex++
        }
        // 采购
        else if (expenseIndex < this.paidOutData.length + this.paidPaymentData.length +
          this.unPaidPaymentData.length + this.purchaseData.length) {
          const idx = expenseIndex - this.paidOutData.length - this.paidPaymentData.length - this.unPaidPaymentData.length
          expenseItem = this.purchaseData[idx]
          expenseCategory = idx === 0 ? '采购' : ''
          expenseIndex++
        }

        if (expenseItem) {
          row.push('', '', '', expenseCategory, expenseItem.itemName, expenseItem.amount)
        } else {
          row.push('', '', '', '', '', '')
        }

        list.push(row)
      }

      // 现金流水结余
      list.push([])
      list.push(['现金流水结余: ', this.cashBalance + '元'])
      list.push([]) // 空行
      list.push([]) // 空行

      // 当前打款
      list.push([])
      list.push(['当前打款: ', this.paymentAmount + '元'])
      list.push([]) // 空行
      list.push([]) // 空行

      // ========== 利润账单明细 ==========
      list.push(['利润账单明细'])
      list.push([]) // 空行

      // 表头
      list.push(['收入', '', '', '', '', '', '支出', '', ''])

      // 计算最大行数
      const profitMaxRows = Math.max(
        this.incomeData.length,
        this.paidOutData.length + this.unPaidOutData.length + this.outStoreData.length
      )

      // 构建利润账单数据
      let profitExpenseIndex = 0

      for (let i = 0; i < profitMaxRows; i++) {
        let row = []

        // 左侧收入数据(与现金流水账相同)
        if (i < this.incomeData.length) {
          if (i === 0) {
            row.push('收入单')
          } else {
            row.push('')
          }
          row.push(this.incomeData[i].itemName)
          row.push(this.incomeData[i].amount)
        } else {
          row.push('', '', '')
        }

        // 右侧支出数据
        let profitExpenseItem = null
        let profitExpenseCategory = ''

        // 支出单(已付款)
        if (profitExpenseIndex < this.paidOutData.length) {
          profitExpenseItem = this.paidOutData[profitExpenseIndex]
          profitExpenseCategory = profitExpenseIndex === 0 ? '支出单(已付款)' : ''
          profitExpenseIndex++
        }
        // 支出单(待付款)
        else if (profitExpenseIndex < this.paidOutData.length + this.unPaidOutData.length) {
          const idx = profitExpenseIndex - this.paidOutData.length
          profitExpenseItem = this.unPaidOutData[idx]
          profitExpenseCategory = idx === 0 ? '支出单(待付款)' : ''
          profitExpenseIndex++
        }
        // 支出(出库)
        else if (profitExpenseIndex < this.paidOutData.length + this.unPaidOutData.length + this.outStoreData.length) {
          const idx = profitExpenseIndex - this.paidOutData.length - this.unPaidOutData.length
          profitExpenseItem = this.outStoreData[idx]
          profitExpenseCategory = idx === 0 ? '支出(出库)' : ''
          profitExpenseIndex++
        }

        if (profitExpenseItem) {
          row.push('', '', '', profitExpenseCategory, profitExpenseItem.itemName, profitExpenseItem.amount)
        } else {
          row.push('', '', '', '', '', '')
        }

        list.push(row)
      }

      // 当前利润
      list.push([])
      list.push(['当前利润: ', this.profit + '元'])

      let head = '收入分类,收入项目,收入金额,,,,支出分类,支出项目,支出金额'
      let tip = this.statisticData.accountName
      let fileName = `账户流水统计_${this.statisticData.accountName}_${this.queryParam.beginDate}_${this.queryParam.endDate}`
      this.handleExportXlsPost(fileName, tip, head, tip, list)
    },
    processStatisticData() {
      if (!this.statisticData) return

      // 处理现金流水账 - 收入数据
      this.incomeData = []
      if (this.statisticData.income && this.statisticData.income.length > 0) {
        this.incomeData = this.statisticData.income.map(item => ({
          itemName: item.itemName,
          itemCode: item.itemCode,
          amount: parseFloat(item.amount || 0).toFixed(2)
        }))
      }

      // 处理现金流水账 - 支出单(已付款)
      this.paidOutData = []
      if (this.statisticData.paidOut && this.statisticData.paidOut.length > 0) {
        this.paidOutData = this.statisticData.paidOut.map(item => ({
          itemName: item.itemName,
          itemCode: item.itemCode,
          amount: parseFloat(item.amount || 0).toFixed(2)
        }))
      }

      // 处理现金流水账 - 支出单(待付款)
      this.unPaidOutData = []
      if (this.statisticData.unPaidOut && this.statisticData.unPaidOut.length > 0) {
        this.unPaidOutData = this.statisticData.unPaidOut.map(item => ({
          itemName: item.itemName,
          itemCode: item.itemCode,
          amount: parseFloat(item.amount || 0).toFixed(2)
        }))
      }

      // 处理现金流水账 - 采购
      this.purchaseData = []
      if (this.statisticData.purchase && this.statisticData.purchase.length > 0) {
        this.purchaseData = this.statisticData.purchase.map(item => ({
          itemName: item.itemName,
          amount: parseFloat(item.amount || 0).toFixed(2)
        }))
      }

      // 处理利润账单 - 支出(出库)
      this.outStoreData = []
      if (this.statisticData.outStore && this.statisticData.outStore.length > 0) {
        this.outStoreData = this.statisticData.outStore.map(item => ({
          itemName: item.itemName,
          itemCode: item.itemCode,
          amount: parseFloat(item.amount || 0).toFixed(2)
        }))
      }

      // 处理现金流水账 - 付款单(已支付)
      this.paidPaymentData = []
      if (this.statisticData.paidPayment && this.statisticData.paidPayment.length > 0) {
        this.paidPaymentData = this.statisticData.paidPayment.map(item => ({
          itemName: item.itemName,
          amount: parseFloat(item.amount || 0).toFixed(2)
        }))
      }

      // 处理现金流水账 - 付款单(待支付)
      this.unPaidPaymentData = []
      if (this.statisticData.unPaidPayment && this.statisticData.unPaidPayment.length > 0) {
        this.unPaidPaymentData = this.statisticData.unPaidPayment.map(item => ({
          itemName: item.itemName,
          amount: parseFloat(item.amount || 0).toFixed(2)
        }))
      }

      // 计算各类别总金额
      this.incomeTotalAmount = this.incomeData.reduce((sum, item) => sum + parseFloat(item.amount), 0).toFixed(2)
      this.paidOutTotalAmount = this.paidOutData.reduce((sum, item) => sum + parseFloat(item.amount), 0).toFixed(2)
      this.unPaidOutTotalAmount = this.unPaidOutData.reduce((sum, item) => sum + parseFloat(item.amount), 0).toFixed(2)
      this.paidPaymentTotalAmount = this.paidPaymentData.reduce((sum, item) => sum + parseFloat(item.amount), 0).toFixed(2)
      this.unPaidPaymentTotalAmount = this.unPaidPaymentData.reduce((sum, item) => sum + parseFloat(item.amount), 0).toFixed(2)
      this.purchaseTotalAmount = this.purchaseData.reduce((sum, item) => sum + parseFloat(item.amount), 0).toFixed(2)
      this.outStoreTotalAmount = this.outStoreData.reduce((sum, item) => sum + parseFloat(item.amount), 0).toFixed(2)

      // 设置结余和利润
      this.cashBalance = parseFloat(this.statisticData.cashBalance || 0).toFixed(2)
      this.paymentAmount = parseFloat(this.statisticData.paymentAmount || 0).toFixed(2)
      this.profit = parseFloat(this.statisticData.profit || 0).toFixed(2)

      // 处理打款支出项数据
      this.companyOutData = []
      if (this.statisticData.companyOut && this.statisticData.companyOut.length > 0) {
        this.companyOutData = this.statisticData.companyOut.map(item => ({
          itemName: item.itemName,
          itemCode: item.itemCode,
          amount: parseFloat(item.amount || 0).toFixed(2)
        }))
      }

      // 动态生成打款公式
      this.generatePaymentFormula()
    },
    calculatePercentage(amount) {
      // 计算百分比：当前项除以收入总额
      if (!this.incomeTotalAmount || parseFloat(this.incomeTotalAmount) === 0) {
        return ''
      }
      const percentage = (parseFloat(amount) / parseFloat(this.incomeTotalAmount) * 100).toFixed(2)
      return `${percentage}%`
    },
    generatePaymentFormula() {
      // 基础公式：收入单
      let formula = '收入单'

      // 添加支出项
      if (this.companyOutData && this.companyOutData.length > 0) {
        const outItems = this.companyOutData.map(item => item.itemName).join(' - ')
        formula += '-' + outItems
      }

      this.paymentFormula = formula
    },
    jumpToItemInList(inOutItemId) {
      // 跳转到收入单列表
      this.openInNewTab(
        '/financial/item_in',
        {
          accountId: this.currentAccountId,
          beginDate: this.queryParam.beginDate,
          endDate: this.queryParam.endDate,
          inOutItemId: inOutItemId
        },
        '收入单',
        'ItemInList'
      )
    },
    jumpToItemOutList(status, inOutItemId) {
      // 跳转到支出单列表（已付款）
      this.openInNewTab(
        '/financial/item_out',
        {
          accountId: this.currentAccountId,
          beginDate: this.queryParam.beginDate,
          endDate: this.queryParam.endDate,
          inOutItemId: inOutItemId,
          status: status
        },
        '支出单',
        'ItemOutList'
      )
    },
    jumpToMoneyOutList(status) {
      // 跳转到付款单列表
      this.openInNewTab(
        '/financial/money_out',
        {
          accountId: this.currentAccountId,
          beginDate: this.queryParam.beginDate,
          endDate: this.queryParam.endDate,
          status: status
        },
        '付款单',
        'MoneyOutList'
      )
    },
    jumpToOtherInList(paymentStatus) {
      // 跳转到其它入库列表
      const query = {
        accountId: this.currentAccountId,
        beginDate: this.queryParam.beginDate,
        endDate: this.queryParam.endDate
      }

      // 根据付款状态设置筛选条件
      if (paymentStatus === 'paid') {
        query.status = '1' // 已审核（已付款）
      } else if (paymentStatus === 'unpaid') {
        query.status = '0' // 未审核（未付款）
      }

      this.openInNewTab(
        '/bill/other_in',
        query,
        '其它入库',
        'OtherInList'
      )
    },
    jumpToPurchaseInList() {
      // 跳转到采购入库列表
      this.openInNewTab(
        '/bill/purchase_order',
        {
          accountId: this.currentAccountId,
          beginDate: this.queryParam.beginDate,
          endDate: this.queryParam.endDate
        },
        '采购订单',
        'PurchaseOrderList'
      )
    },
    jumpToOtherOutList(bizType) {
      // 跳转到其它出库入库列表
      this.openInNewTab(
        '/bill/other_out',
        {
          accountId: this.currentAccountId,
          beginDate: this.queryParam.beginDate,
          endDate: this.queryParam.endDate,
          bizType: bizType
        },
        '其它出库',
        'OtherOutList'
      )
    }
  }
}
</script>
<style scoped>
.statistic-container {
  margin-bottom: 20px;
}

.account-header {
  font-size: 14px;
  margin-bottom: 10px;
  padding: 10px;
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
}

.statistic-section-title {
  font-size: 16px;
  font-weight: bold;
  margin: 15px 0 10px 0;
  padding: 5px 10px;
  background: #fafafa;
  border-left: 4px solid #1890ff;
}

.expense-title {
  text-align: center;
  font-weight: bold;
  padding: 5px;
  background: #fafafa;
  border: 1px solid #d9d9d9;
  margin-bottom: -1px;
}

.statistic-table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #d9d9d9;
}

.statistic-table td {
  border: 1px solid #d9d9d9;
  padding: 8px;
  font-size: 13px;
}

.category-cell {
  width: 15%;
  text-align: center;
  font-weight: bold;
  background: #fafafa;
  vertical-align: middle;
}

.total-amount-cell {
  width: 15%;
  text-align: center;
  vertical-align: middle;
  font-size: 14px;
}

.sub-category-cell {
  width: 30%;
  text-align: center;
  background: #fafafa;
  vertical-align: middle;
}

.item-cell {
  width: 35%;
  padding-left: 15px;
}

.amount-cell {
  width: 20%;
  text-align: right;
  padding-right: 15px;
}

.balance-box {
  margin-top: 15px;
  padding: 12px 20px;
  background: #f0f2f5;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.balance-label {
  font-size: 15px;
  font-weight: bold;
}

.balance-formula {
  font-size: 13px;
  color: #666;
}

.profit-amount {
  color: #ff4d4f;
  font-weight: bold;
}

.percentage-text {
  display: block;
  font-size: 11px;
  color: #ff6b35;
  font-weight: 500;
  margin-top: 2px;
  background: #fff3e0;
  padding: 1px 4px;
  border-radius: 2px;
  display: inline-block;
}

.month-quick-select {
  margin-top: 8px;
  margin-left: 80px;
  display: flex;
  align-items: center;
}

.quick-select-buttons {
  display: flex;
  gap: 8px;
}

.link-text {
  color: #1890ff;
  cursor: pointer;
  text-decoration: none;
}

.link-text:hover {
  color: #40a9ff;
  text-decoration: underline;
}
</style>