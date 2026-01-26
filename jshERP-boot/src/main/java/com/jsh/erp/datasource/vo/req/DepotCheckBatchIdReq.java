package com.jsh.erp.datasource.vo.req;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 盘点单ID请求
 */
@Data
public class DepotCheckBatchIdReq {

    /** 盘点单ID */
    @NotEmpty(message = "盘点单ID不能为空")
    private List<Long> headIds;
}
