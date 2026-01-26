package com.jsh.erp.datasource.entities;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 盘点主表实体类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("jsh_depot_check_head")
public class DepotCheckHead {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 盘点编号 */
    private String checkNumber;

    /** 盘点仓库ID */
    private Long depotId;

    /** 盘点日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date checkDate;

    /** 应盘商品数(种类) */
    private Integer shouldCheckNumber;

    /** 已盘商品数(种类) */
    private Integer checkedNumber;

    /** 账面总数量(种类) */
    private Integer originNumber;

    /** 盘点总数量(种类) */
    private Integer actualNumber;

    /** 账面总金额 */
    private BigDecimal originAmount;

    /** 盘点总金额 */
    private BigDecimal actualAmount;

    /** 盘亏商品数(种类) */
    private Integer lossNumber;

    /** 盘亏总金额 */
    private BigDecimal lossAmount;

    /** 盘盈商品数(种类) */
    private Integer profitNumber;

    /** 盘盈总金额 */
    private BigDecimal profitAmount;

    /** 盘盈入库订单号 */
    private String rkNumber;

    /** 盘亏出库订单号 */
    private String ckNumber;

    /** 状态：1-盘点中，2-已盘点（未审核），3-已盘点（已审核），4-已调整 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 文件 */
    private String fileName;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    /** 创建人 */
    private Long creator;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;

    /** 更新人 */
    private Long updater;

    /** 删除标记，0未删除，1删除 */
    @TableLogic
    private Integer deleteFlag;

    /** 租户ID */
    private Long tenantId;
}