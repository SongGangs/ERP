package com.jsh.erp.datasource.vo.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 盘点单详情响应
 */
@Data
public class DepotCheckHeadDetailResp {

    private Long id;

    /** 盘点编号 */
    private String checkNumber;

    /** 盘点仓库ID */
    private Long depotId;

    /** 盘点仓库名称 */
    private String depotName;

    /** 盘点日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date checkDate;

    /** 应盘商品数(种类) */
    private Integer shouldCheckNumber;

    /** 已盘商品数(种类) */
    private Integer checkedNumber;

    /** 未盘商品数(种类) */
    private Integer uncheckNumber;

    /** 账面总金额 */
    private BigDecimal originAmount;

    /** 盘点总金额 */
    private BigDecimal actualAmount;

    /** 差异总金额 */
    private BigDecimal diffAmount;

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

    /** 操作人 */
    private String operatorName;

    /** 文件 */
    private String fileNames;

    /** 明细列表 */
    private List<DepotCheckItemDetailResp> items;
}