package com.jsh.erp.datasource.vo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountItemStatisticDto {

    private Long accountId;

    private BigDecimal debt;

    private BigDecimal finishDebt;
}
