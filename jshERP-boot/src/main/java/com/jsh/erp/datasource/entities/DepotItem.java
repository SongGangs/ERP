package com.jsh.erp.datasource.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepotItem {
    private Long id;

    private Long headerId;

    private Long materialId;

    private Long materialExtendId;

    private String materialUnit;

    private String sku;

    private BigDecimal operNumber;

    private BigDecimal basicNumber;

    private BigDecimal unitPrice;

    private BigDecimal purchaseUnitPrice;

    private BigDecimal taxUnitPrice;

    private BigDecimal allPrice;

    private String remark;

    private Long depotId;

    private Long anotherDepotId;

    private BigDecimal taxRate;

    private BigDecimal taxMoney;

    private BigDecimal taxLastMoney;

    private String materialType;

    private String snList;

    private String batchNumber;

    private Date productionDate;

    private Date expirationDate;

    private Long linkId;

    private Long tenantId;

    private String deleteFlag;

    public void setMaterialUnit(String materialUnit) {
        this.materialUnit = materialUnit == null ? null : materialUnit.trim();
    }

    public void setSku(String sku) {
        this.sku = sku == null ? null : sku.trim();
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType == null ? null : materialType.trim();
    }

    public void setSnList(String snList) {
        this.snList = snList == null ? null : snList.trim();
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber == null ? null : batchNumber.trim();
    }

    public void setDeleteFlag(String deleteFlag) {
        this.deleteFlag = deleteFlag == null ? null : deleteFlag.trim();
    }
}