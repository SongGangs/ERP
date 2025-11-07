package com.jsh.erp.datasource.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Date;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHead {
    private Long id;

    private String type;

    private Long organId;

    private Long handsPersonId;

    private Long creator;

    private BigDecimal changeAmount;

    private BigDecimal discountMoney;

    private BigDecimal totalPrice;

    private Long accountId;

    private String billNo;

    private Date billTime;

    private String remark;

    private String fileName;

    private String status;

    private String source;

    private Long tenantId;

    private String deleteFlag;

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo == null ? null : billNo.trim();
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public void setSource(String source) {
        this.source = source == null ? null : source.trim();
    }

    public void setDeleteFlag(String deleteFlag) {
        this.deleteFlag = deleteFlag == null ? null : deleteFlag.trim();
    }
}