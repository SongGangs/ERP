package com.jsh.erp.datasource.vo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BizTypeEnum implements BaseEnum {

    /**
     * 原材料消耗
     */
    MATERIAL_CONSUMPTION(1, "原材料消耗"),

    /**
     * 损坏报损
     */
    DAMAGE_LOSS(2, "损坏报损"),

    /**
     * 临期过期报损
     */
    EXPIRING_LOSS(3, "临期过期报损"),
    /**
     * 盘亏报损
     */
    STOCK_LOSS(4, "盘亏报损"),

    /**
     * 盘盈入库
     */
    STOCK_Profit(14, "盘盈入库"),
    ;

    private final Integer type;
    private final String desc;
}
