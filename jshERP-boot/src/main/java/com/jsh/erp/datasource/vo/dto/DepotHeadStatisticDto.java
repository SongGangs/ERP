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
public class DepotHeadStatisticDto {

    private String typeName;

    private String subTypeName;

    private Integer outType;

    private Integer status;

    private BigDecimal amount;
}
