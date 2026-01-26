package com.jsh.erp.datasource.vo.req;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 盘点单查询请求
 */
@Data
public class DepotCheckQueryReq {

    /** 盘点编号 */
    private String checkNumber;

    /** 仓库ID */
    private Long depotId;

    /** 状态 */
    private Integer status;

    /** 操作人 */
    private Long operatorId;

    /** 商品信息 */
    private String materialInfo;

    /** 开始日期 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate beginTime;

    /** 结束日期 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endTime;
}