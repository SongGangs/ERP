package com.jsh.erp.datasource.vo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 盘点单关联出入库单请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepotCheckLinkBillReq {

    /** 盘点单编码 */
    @NotBlank(message = "盘点单编码不能为空")
    private String checkNumber;

    /** 盘盈入库单号（前端生成后传入） */
    private String rkNumber;

    /** 盘亏出库单号（前端生成后传入） */
    private String ckNumber;
}