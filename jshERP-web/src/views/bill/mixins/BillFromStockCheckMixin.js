/**
 * 从盘点单生成出入库单的公用 Mixin
 * 用于 OtherInList.vue 和 OtherOutList.vue
 */
import { getMaterialByBarCode } from '@/api/api'
import { getMpListShort } from '@/utils/util'
import Vue from 'vue'

export const BillFromStockCheckMixin = {
  mounted() {
    if (this.$route.query.from === 'stock_check' && this.$route.query.action === 'add') {
      this.handleStockCheckBillData()
    }
  },

  methods: {
    /**
     * 处理从盘点单传递过来的数据
     */
    handleStockCheckBillData() {
      const storageKey = this.getStockCheckStorageKey()
      const billData = sessionStorage.getItem(storageKey)

      if (!billData) return

      try {
        const data = JSON.parse(billData)
        sessionStorage.removeItem(storageKey)
        this.fetchMaterialDetailsAndFillForm(data)
      } catch (e) {
        console.error('解析盘点数据失败:', e)
        this.$message.error('加载盘点数据失败')
      }
    },

    /**
     * 获取 sessionStorage 的 key
     * 子类需要实现此方法
     */
    getStockCheckStorageKey() {
      throw new Error('子类必须实现 getStockCheckStorageKey 方法')
    },

    /**
     * 批量查询商品详细信息并填充表单
     */
    async fetchMaterialDetailsAndFillForm(billData) {
      const rows = billData.rows

      if (billData._needFetchMaterial !== true) {
        this.fillBillForm(billData, rows)
        return
      }

      const mpList = getMpListShort(Vue.ls.get('materialPropertyList'))
      const barCodes = rows.map(row => row.barCode).join(',')
      const depotId = billData.depotId

      try {
        const materialInfoList = await this.fetchMaterialByBarCode(barCodes, depotId, mpList)

        if (materialInfoList && materialInfoList.length > 0) {
          const materialMap = new Map(
            materialInfoList.map(material => [material.mBarCode, material])
          )

          const enrichedRows = rows.map(row => {
            const materialInfo = materialMap.get(row.barCode)
            return materialInfo
              ? this.parseInfoToObj(materialInfo, row, depotId)
              : row
          })

          this.fillBillForm(billData, enrichedRows)
        } else {
          this.fillBillForm(billData, rows)
        }
      } catch (error) {
        console.error('批量查询商品信息失败:', error)
        this.$message.error('查询商品信息失败，请重试')
        this.fillBillForm(billData, rows)
      }
    },

    /**
     * 通过条码查询商品信息（支持批量查询）
     */
    async fetchMaterialByBarCode(barCodes, depotId, mpList) {
      try {
        const res = await getMaterialByBarCode({
          barCode: barCodes,
          depotId: depotId,
          mpList: mpList,
          prefixNo: this.prefixNo
        })

        return res && res.code === 200 && res.data && res.data.length > 0 ? res.data : null
      } catch (error) {
        console.error('查询商品失败:', barCodes, error)
        return null
      }
    },

    /**
     * 填充单据表单
     */
    fillBillForm(billData, enrichedRows) {
      this.$nextTick(() => {
        setTimeout(() => {
          this.$refs.modalForm.action = 'add'
          this.$refs.modalForm.priceLimit = this.priceLimit
          this.$refs.modalForm.pdNumber = billData.linkNumber
          this.$refs.modalForm.isCanCheck = this.btnEnableList.indexOf(2) !== -1
          this.$refs.modalForm.add()

          this.$nextTick(() => {
            this.setFormFields(billData)

            if (enrichedRows.length > 0) {
              this.$refs.modalForm.materialTable.dataSource = enrichedRows
              enrichedRows.forEach((row) => {
                this.$refs.modalForm.changeColumnShow(row)
              })
            }
          })
        }, 800)
      })
    },

    /**
     * 设置表单字段
     */
    setFormFields(billData) {
      const fields = {}

      if (billData.operTime) fields.operTime = billData.operTime
      if (billData.remark) fields.remark = billData.remark
      if (billData.linkNumber) fields.linkNumber = billData.linkNumber

      if (Object.keys(fields).length > 0) {
        this.$refs.modalForm.form.setFieldsValue(fields)
      }
    },

    /**
     * 转为商品对象
     */
    parseInfoToObj(mInfo, row, depotId) {
      const billPrice = this.prefixNo !== 'QTRK' ? mInfo.billPrice : 0
      return {
        barCode: mInfo.mBarCode,
        depotId: depotId,
        name: mInfo.name,
        standard: mInfo.standard,
        model: mInfo.model,
        categoryName: mInfo.categoryName,
        enableSerialNumber: mInfo.enableSerialNumber,
        enableBatchNumber: mInfo.enableBatchNumber,
        otherField1: mInfo.otherField1,
        otherField2: mInfo.otherField2,
        otherField3: mInfo.otherField3,
        unit: mInfo.commodityUnit,
        sku: mInfo.sku,
        batchStock: 0,
        batchNumber: '',
        productionDate: '',
        expiryNum: mInfo.expiryNum,
        expirationDate: '',
        operNumber: row.operNumber,
        unitPrice: billPrice,
        allPrice: row.operNumber * billPrice,
        taxRate: 0,
        taxMoney: 0,
        taxLastMoney: row.operNumber * billPrice,
        stock: mInfo.stock,
        snList: '',
        remark: row.remark
      }
    }
  }
}