package com.jsh.erp.datasource.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepotItemVo4WithInfoEx extends DepotItem{

    private Long MId;

    private String MName;

    private String MModel;

    private String MaterialUnit;

    private String MColor;

    private String MStandard;

    private String MMfrs;

    private String MOtherField1;

    private String MOtherField2;

    private String MOtherField3;

    private String enableSerialNumber;

    private String enableBatchNumber;

    private String DepotName;

    private String AnotherDepotName;

    private Long UnitId;

    private String unitName;

    private Integer ratio;

    private String otherUnit;

    private BigDecimal presetPriceOne;

    private String priceStrategy;

    private BigDecimal purchaseDecimal;

    private BigDecimal currentUnitPrice;

    private String barCode;

    private BigDecimal weight;

    private String position;

    private String imgName;

    private String brand;

    private Integer expiryNum;
}