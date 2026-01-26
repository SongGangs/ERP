package com.jsh.erp.datasource.vo.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 盘点单保存请求
 */
@Data
public class DepotCheckSaveReq {

    /** 盘点单ID（更新时传入） */
    private Long id;

    /** 仓库ID */
    @NotNull(message = "仓库ID不能为空")
    private Long depotId;

    /** 盘点编码 */
    @NotBlank(message = "盘点编码不能为空")
    private String checkNumber;

    /** 盘点日期 */
    @NotNull(message = "盘点日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date checkDate;

    /** 备注 */
    private String remark;

    /** 文件 */
    private String fileNames;

    /** 明细列表 */
    @NotEmpty(message = "盘点明细不能为空")
    @Valid
    private List<DepotCheckItemSaveReq> items;
}