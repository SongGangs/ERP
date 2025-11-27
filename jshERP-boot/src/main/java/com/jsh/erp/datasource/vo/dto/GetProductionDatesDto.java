package com.jsh.erp.datasource.vo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetProductionDatesDto {

    private Long materialId;

    private String productionDate;

}