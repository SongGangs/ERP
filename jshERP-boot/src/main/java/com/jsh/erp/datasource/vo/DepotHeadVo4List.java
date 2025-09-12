package com.jsh.erp.datasource.vo;

import com.jsh.erp.datasource.entities.DepotHead;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepotHeadVo4List extends DepotHead{

    private String projectName;

    private String organName;

    private String userName;

    private String accountName;

    private String allocationProjectName;

    private String materialsList;

    private String salesManStr;

    private String operTimeStr;

    private BigDecimal finishDebt;

    private String depotHeadType;

    private String creatorName;

    private String contacts;

    private String telephone;

    private String address;

    private BigDecimal finishDeposit;

    private BigDecimal needDebt;

    private BigDecimal debt;

    private BigDecimal materialCount;

    /**
     * 是否有付款单或收款单
     */
    private Boolean hasFinancialFlag;
    /**
     * 是否有退款单
     */
    private Boolean hasBackFlag;

    /**
     * 实际欠款
     */
    private BigDecimal realNeedDebt;
}