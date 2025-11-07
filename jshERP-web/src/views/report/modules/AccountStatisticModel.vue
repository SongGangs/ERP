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
            <a-col :md="8" :sm="24">
              <a-form-item label="统计日期" :labelCol="labelCol" :wrapperCol="wrapperCol">
                <a-range-picker
                  style="width:100%"
                  v-model="queryParam.createTimeRange"
                  format="YYYY-MM-DD"
                  :placeholder="['开始时间', '结束时间']"
                  @change="onDateChange"
                  @ok="onDateOk"
                />
              </a-form-item>
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
                <td :rowspan="incomeData.length" class="sub-category-cell">收入单</td>
                <td class="item-cell">{{ incomeData[0] ? incomeData[0].itemName : '' }}</td>
                <td class="amount-cell">{{ incomeData[0] ? incomeData[0].amount : '' }}</td>
              </tr>
              <tr v-for="(item, index) in incomeData.slice(1)" :key="'profit-income-' + index">
                <td class="item-cell">{{ item.itemName }}</td>
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
                  <td :rowspan="paidOutData.length" class="sub-category-cell">支出单(已付款)</td>
                  <td class="item-cell">{{ paidOutData[0].itemName }}</td>
                  <td class="amount-cell">{{ paidOutData[0].amount }}</td>
                </tr>
                <tr v-for="(item, index) in paidOutData.slice(1)" :key="'paidOut-' + index">
                  <td class="item-cell">{{ item.itemName }}</td>
                  <td class="amount-cell">{{ item.amount }}</td>
                </tr>
              </template>

              <!-- 其它入库(已付款) -->
              <template v-if="paidOtherInStoreData.length > 0">
                <tr>
                  <td :rowspan="paidOtherInStoreData.length" class="sub-category-cell">其它入库(已付款)</td>
                  <td class="item-cell">{{ paidOtherInStoreData[0].itemName }}</td>
                  <td class="amount-cell">{{ paidOtherInStoreData[0].amount }}</td>
                </tr>
                <tr v-for="(item, index) in paidOtherInStoreData.slice(1)" :key="'paidOtherInStore-' + index">
                  <td class="item-cell">{{ item.itemName }}</td>
                  <td class="amount-cell">{{ item.amount }}</td>
                </tr>
              </template>

              <!-- 其它入库(待付款) -->
              <template v-if="unPaidOtherInStoreData.length > 0">
                <tr>
                  <td :rowspan="unPaidOtherInStoreData.length" class="sub-category-cell">其它入库(已付款)</td>
                  <td class="item-cell">{{ unPaidOtherInStoreData[0].itemName }}</td>
                  <td class="amount-cell">{{ unPaidOtherInStoreData[0].amount }}</td>
                </tr>
                <tr v-for="(item, index) in unPaidOtherInStoreData.slice(1)" :key="'unPaidOtherInStore-' + index">
                  <td class="item-cell">{{ item.itemName }}</td>
                  <td class="amount-cell">{{ item.amount }}</td>
                </tr>
              </template>

              <!-- 采购 -->
              <template v-if="purchaseData.length > 0">
                <tr>
                  <td :rowspan="purchaseData.length" class="sub-category-cell">采购</td>
                  <td class="item-cell">{{ purchaseData[0].itemName }}</td>
                  <td class="amount-cell">{{ purchaseData[0].amount }}</td>
                </tr>
                <tr v-for="(item, index) in purchaseData.slice(1)" :key="'purchase-' + index">
                  <td class="item-cell">{{ item.itemName }}</td>
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
          <span class="balance-formula">收入单-支出单(已付款)-付款单-采购订单</span>
        </div>

        <!-- 利润账单明细 -->
        <div class="statistic-section-title" style="margin-top: 30px">利润账单明细</div>
        <a-row :gutter="16">
          <a-col :span="12">
            <div class="expense-title">收入</div>
            <table class="statistic-table">
              <tbody>
              <tr>
                <td rowspan="50" class="sub-category-cell">收入单</td>
                <td class="item-cell">{{ incomeData[0] ? incomeData[0].itemName : '' }}</td>
                <td class="amount-cell">{{ incomeData[0] ? incomeData[0].amount : '' }}</td>
              </tr>
              <tr v-for="(item, index) in incomeData.slice(1)" :key="'income-' + index">
                <td class="item-cell">{{ item.itemName }}</td>
                <td class="amount-cell">{{ item.amount }}</td>
              </tr>
              </tbody>
            </table>
          </a-col>

          <a-col :span="12">
            <table class="statistic-table">
              <tbody>
              <!-- 支出单(已付款和未付款) -->
              <template v-if="outData.length > 0">
                <tr>
                  <td :rowspan="outData.length" class="sub-category-cell">支出单(已付款和未付款)</td>
                  <td class="item-cell">{{ outData[0].itemName }}</td>
                  <td class="amount-cell">{{ outData[0].amount }}</td>
                </tr>
                <tr v-for="(item, index) in outData.slice(1)" :key="'out-' + index">
                  <td class="item-cell">{{ item.itemName }}</td>
                  <td class="amount-cell">{{ item.amount }}</td>
                </tr>
              </template>

              <!-- 支出(出库) -->
              <template v-if="outStoreData.length > 0">
                <tr>
                  <td :rowspan="outStoreData.length" class="sub-category-cell">支出(出库)</td>
                  <td class="item-cell">{{ outStoreData[0].itemName }}</td>
                  <td class="amount-cell">{{ outStoreData[0].amount }}</td>
                </tr>
                <tr v-for="(item, index) in outStoreData.slice(1)" :key="'outStore-' + index">
                  <td class="item-cell">{{ item.itemName }}</td>
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
          <span class="balance-formula">收入单-支出单-出库</span>
        </div>
      </div>
    </a-modal>
  </div>
</template>
<script>
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import { mixinDevice } from '@/utils/mixin'
import JEllipsis from '@/components/jeecg/JEllipsis'
import { getFirstDayOfCurrentMonth, getFormatDate } from '@/utils/util'
import { getAction } from '@/api/manage'
import moment from 'moment/moment'

export default {
  name: 'AccountStatisticModel',
  mixins: [JeecgListMixin, mixinDevice],
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
      // 查询条件
      queryParam: {
        accountId: '',
        beginDate: getFirstDayOfCurrentMonth(),
        endDate: getFormatDate(),
        createTimeRange: [moment(getFirstDayOfCurrentMonth()), moment(getFormatDate())]
      },
      tabKey: '1',
      pageName: 'accountStatisticModel',
      // 统计数据
      statisticData: null,
      // 现金流水账数据
      incomeData: [],
      outData: [],
      paidOutData: [],
      purchaseData: [],
      outStoreData: [],
      paidOtherInStoreData: [],
      unPaidOtherInStoreData: [],
      // 现金结余
      cashBalance: 0,
      // 利润
      profit: 0,
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
    },
    onDateOk(value) {
      console.log(value)
    },
    searchReset() {
      this.queryParam = {
        accountId: this.currentAccountId,
        beginDate: getFirstDayOfCurrentMonth(),
        endDate: getFormatDate(),
        createTimeRange: [moment(getFirstDayOfCurrentMonth()), moment(getFormatDate())]
      }
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
        this.paidOutData.length + this.paidOtherInStoreData.length +
        this.unPaidOtherInStoreData.length + this.purchaseData.length
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
        // 其它入库(已付款)
        else if (expenseIndex < this.paidOutData.length + this.paidOtherInStoreData.length) {
          const idx = expenseIndex - this.paidOutData.length
          expenseItem = this.paidOtherInStoreData[idx]
          expenseCategory = idx === 0 ? '其它入库(已付款)' : ''
          expenseIndex++
        }
        // 其它入库(待付款)
        else if (expenseIndex < this.paidOutData.length + this.paidOtherInStoreData.length + this.unPaidOtherInStoreData.length) {
          const idx = expenseIndex - this.paidOutData.length - this.paidOtherInStoreData.length
          expenseItem = this.unPaidOtherInStoreData[idx]
          expenseCategory = idx === 0 ? '其它入库(待付款)' : ''
          expenseIndex++
        }
        // 采购
        else if (expenseIndex < this.paidOutData.length + this.paidOtherInStoreData.length +
          this.unPaidOtherInStoreData.length + this.purchaseData.length) {
          const idx = expenseIndex - this.paidOutData.length - this.paidOtherInStoreData.length - this.unPaidOtherInStoreData.length
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

      // ========== 利润账单明细 ==========
      list.push(['利润账单明细'])
      list.push([]) // 空行

      // 表头
      list.push(['收入', '', '', '', '', '', '支出', '', ''])

      // 计算最大行数
      const profitMaxRows = Math.max(
        this.incomeData.length,
        this.outData.length + this.outStoreData.length
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

        // 支出单(已付款和未付款)
        if (profitExpenseIndex < this.outData.length) {
          profitExpenseItem = this.outData[profitExpenseIndex]
          profitExpenseCategory = profitExpenseIndex === 0 ? '支出单(已付款和未付款)' : ''
          profitExpenseIndex++
        }
        // 支出(出库)
        else if (profitExpenseIndex < this.outData.length + this.outStoreData.length) {
          const idx = profitExpenseIndex - this.outData.length
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

      // 当月利润
      list.push([])
      list.push(['当月利润: ', this.profit + '元'])

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
          amount: parseFloat(item.amount || 0).toFixed(2)
        }))
      }

      // 处理现金流水账 - 其他人(已付款)
      this.outData = []
      if (this.statisticData.out && this.statisticData.out.length > 0) {
        this.outData = this.statisticData.out.map(item => ({
          itemName: item.itemName,
          amount: parseFloat(item.amount || 0).toFixed(2)
        }))
      }

      // 处理现金流水账 - 支出单(已付款)
      this.paidOutData = []
      if (this.statisticData.paidOut && this.statisticData.paidOut.length > 0) {
        this.paidOutData = this.statisticData.paidOut.map(item => ({
          itemName: item.itemName,
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
          amount: parseFloat(item.amount || 0).toFixed(2)
        }))
      }

      // 处理利润账单 - 支出单(已付款和未付款)
      this.paidOtherInStoreData = []
      if (this.statisticData.paidOtherInStore && this.statisticData.paidOtherInStore.length > 0) {
        this.paidOtherInStoreData = this.statisticData.paidOtherInStore.map(item => ({
          itemName: item.itemName,
          amount: parseFloat(item.amount || 0).toFixed(2)
        }))
      }

      this.unPaidOtherInStoreData = []
      if (this.statisticData.paidOtherInStore && this.statisticData.unPaidOtherInStore.length > 0) {
        this.unPaidOtherInStoreData = this.statisticData.unPaidOtherInStore.map(item => ({
          itemName: item.itemName,
          amount: parseFloat(item.amount || 0).toFixed(2)
        }))
      }

      // 设置结余和利润
      this.cashBalance = parseFloat(this.statisticData.cashBalance || 0).toFixed(2)
      this.profit = parseFloat(this.statisticData.profit || 0).toFixed(2)
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
</style>