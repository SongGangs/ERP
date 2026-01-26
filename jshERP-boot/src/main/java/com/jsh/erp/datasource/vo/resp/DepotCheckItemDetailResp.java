package com.jsh.erp.datasource.vo.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 盘点明细详情响应
 */
@Data
public class DepotCheckItemDetailResp {

    private Long id;

    /** 商品ID */
    private Long materialId;

    /** 商品条码 */
    private String mBarCode;

    /** 商品名称 */
    private String name;

    /** 规格型号 */
    private String standard;

    /** 单位 */
    private String unit;

    /** 类别 */
    private String categoryName;

    /** 多属性 */
    private String sku;

    /** 账面数量 */
    private Integer originNumber;

    /** 盘点数量 */
    private Integer actualNumber;

    /** 差异数量 */
    private Integer diffNumber;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 账面金额 */
    private BigDecimal originAmount;

    /** 盘点金额 */
    private BigDecimal actualAmount;

    /** 差异金额 */
    private BigDecimal diffAmount;

    /** 备注 */
    private String remark;
}