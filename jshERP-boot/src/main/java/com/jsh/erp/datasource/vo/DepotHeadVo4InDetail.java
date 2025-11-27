package com.jsh.erp.datasource.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepotHeadVo4InDetail {

    private String number;

    private String barCode;

    private String mname;

    private String model;

    private String standard;

    private String color;

    private String brand;

    private String mfrs;

    private BigDecimal unitPrice;

    private String sku;

    private String mUnit;

    private String newRemark;

    private BigDecimal operNumber;

    private BigDecimal allPrice;

    private BigDecimal taxRate;

    private BigDecimal taxMoney;

    private BigDecimal taxLastMoney;

    private String sname;

    private String dname;

    private String operTime;

    private String newType;

    private Long tenantId;

    private String productionDate;
}