package com.jsh.erp.datasource.vo.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 盘点明细保存请求
 */
@Data
public class DepotCheckItemSaveReq {

    /** 商品ID */
    @NotNull(message = "商品ID不能为空")
    private Long materialId;

    /** 盘点数量 */
    private Integer actualNumber;

    /** 备注 */
    private String remark;
}