package com.jsh.erp.datasource.vo;


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
public class DepotItemVoBatchNumberList {

    private String id;
    private String barCode;
    private String name;
    private String standard;
    private String model;
    private Long unitId;
    private String commodityUnit;
    private String batchNumber;
    private Date productionDate;
    private String productionDateStr;
    private Date expirationDate;
    private String expirationDateStr;
    private BigDecimal totalNum;
}
