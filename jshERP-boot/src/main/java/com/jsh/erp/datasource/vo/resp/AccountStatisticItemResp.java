package com.jsh.erp.datasource.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatisticItemResp {

    private String itemName;

    private BigDecimal amount;


    public static List<AccountStatisticItemResp> from(Map<String, BigDecimal> map) {
        return map.entrySet()
                .stream()
                .map(entry -> AccountStatisticItemResp.builder()
                        .itemName(entry.getKey()).amount(entry.getValue()).build())
                .collect(Collectors.toList());
    }
}
