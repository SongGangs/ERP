package com.jsh.erp.datasource.vo.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 盘点单ID请求
 */
@Data
public class DepotCheckIdReq {

    /** 盘点单ID */
    @NotNull(message = "盘点单ID不能为空")
    private Long id;
}