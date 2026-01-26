package com.jsh.erp.datasource.entities;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 盘点明细表实体类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("jsh_depot_check_item")
public class DepotCheckItem {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 盘点主表ID */
    private Long headerId;

    /** 商品ID */
    private Long materialId;

    /** 账面数量 */
    private Integer originNumber;

    /** 盘点数量 */
    private Integer actualNumber;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 账面金额 */
    private BigDecimal originAmount;

    /** 盘点金额 */
    private BigDecimal actualAmount;

    /** 备注 */
    private String remark;

    /** 租户ID */
    private Long tenantId;

    /** 删除标记，0未删除，1删除 */
    @TableLogic
    private Integer deleteFlag;
}