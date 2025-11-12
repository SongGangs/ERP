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
public class DepotHead {
    private Long id;

    private String type;

    private String subType;

    private String defaultNumber;

    private String number;

    private Date createTime;

    private Date operTime;

    private Long organId;

    private Long depotId;

    private Long creator;

    private Long accountId;

    private BigDecimal changeAmount;

    private BigDecimal backAmount;

    private BigDecimal totalPrice;

    private String payType;

    private String billType;

    private String remark;

    private String fileName;

    private String salesMan;

    private String accountIdList;

    private String accountMoneyList;

    private BigDecimal discount;

    private BigDecimal discountMoney;

    private BigDecimal discountLastMoney;

    private BigDecimal otherMoney;

    private BigDecimal deposit;

    private String status;

    private String purchaseStatus;

    private String source;

    private String linkNumber;

    private String linkApply;

    private Integer bizType;

    private Long tenantId;

    private String deleteFlag;

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public void setSubType(String subType) {
        this.subType = subType == null ? null : subType.trim();
    }

    public void setDefaultNumber(String defaultNumber) {
        this.defaultNumber = defaultNumber == null ? null : defaultNumber.trim();
    }

    public void setNumber(String number) {
        this.number = number == null ? null : number.trim();
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
    }

    public void setPayType(String payType) {
        this.payType = payType == null ? null : payType.trim();
    }

    public void setBillType(String billType) {
        this.billType = billType == null ? null : billType.trim();
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public void setSalesMan(String salesMan) {
        this.salesMan = salesMan == null ? null : salesMan.trim();
    }

    public void setAccountIdList(String accountIdList) {
        this.accountIdList = accountIdList == null ? null : accountIdList.trim();
    }

    public void setAccountMoneyList(String accountMoneyList) {
        this.accountMoneyList = accountMoneyList == null ? null : accountMoneyList.trim();
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public void setPurchaseStatus(String purchaseStatus) {
        this.purchaseStatus = purchaseStatus == null ? null : purchaseStatus.trim();
    }

    public void setSource(String source) {
        this.source = source == null ? null : source.trim();
    }

    public void setLinkNumber(String linkNumber) {
        this.linkNumber = linkNumber == null ? null : linkNumber.trim();
    }

    public void setLinkApply(String linkApply) {
        this.linkApply = linkApply == null ? null : linkApply.trim();
    }

    public void setDeleteFlag(String deleteFlag) {
        this.deleteFlag = deleteFlag == null ? null : deleteFlag.trim();
    }
}