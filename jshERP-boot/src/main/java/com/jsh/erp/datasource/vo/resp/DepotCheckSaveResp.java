package com.jsh.erp.datasource.vo.resp;

import lombok.Data;

/**
 * 盘点单保存响应
 */
@Data
public class DepotCheckSaveResp {

    /** 盘点单ID */
    private Long id;

    /** 盘点编号 */
    private String checkNumber;

    /** 状态 */
    private Integer status;
}