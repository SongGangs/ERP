package com.jsh.erp.datasource.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatisticResp {

    private Long accountId;

    private String accountName;

    private List<AccountStatisticItemResp> income;

    private List<AccountStatisticItemResp> out;

    private List<AccountStatisticItemResp> paidOut;

    private List<AccountStatisticItemResp> purchase;

    private List<AccountStatisticItemResp> outStore;

    private List<AccountStatisticItemResp> paidOtherInStore;

    private List<AccountStatisticItemResp> unPaidOtherInStore;

    /**
     * 现金流水结余 = 收入单-支出单(已付款)-付款单-采购订单
     */
    private BigDecimal cashBalance;

    /**
     * 利润 = 收入单-支出单-出库
     */
    private BigDecimal profit;
}
