package com.jsh.erp.datasource.vo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 盘点单状态枚举
 */
@Getter
@AllArgsConstructor
public enum DepotCheckStatusEnum implements BaseEnum {

    /**
     * 盘点中
     */
    CHECKING(1, "盘点中"),

    /**
     * 已盘点（未审核）
     */
    CHECKED_UNAPPROVED(2, "已盘点（未审核）"),

    /**
     * 已盘点（已审核）
     */
    CHECKED_APPROVED(3, "已盘点（已审核）"),

    /**
     * 已调整
     */
    ADJUSTED(4, "已调整");

    private final Integer type;
    private final String desc;

    /**
     * 根据类型获取枚举
     */
    public static DepotCheckStatusEnum getByType(Integer type) {
        if (type == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(e -> e.getType().equals(type))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据类型获取描述
     */
    public static String getDescByType(Integer type) {
        DepotCheckStatusEnum statusEnum = getByType(type);
        return statusEnum != null ? statusEnum.getDesc() : "";
    }
}