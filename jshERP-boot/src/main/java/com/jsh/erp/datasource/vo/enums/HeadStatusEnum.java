package com.jsh.erp.datasource.vo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HeadStatusEnum implements BaseEnum {

    /**
     * 未审核
     */
    UNAUDITED(0, "未审核"),

    /**
     * 已审核
     */
    AUDITED(1, "已审核"),

    /**
     * 完成采购/销售
     */
    COMPLETED(2, "完成采购"),

    /**
     * 部分采购/销售
     */
    PARTIAL(3, "部分采购"),

    /**
     * 审核中
     */
    AUDITING(9, "审核中");

    private final Integer type;
    private final String desc;
}
