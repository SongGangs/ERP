package com.jsh.erp.datasource.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Depot {
    private Long id;

    private String name;

    private String address;

    private BigDecimal warehousing;

    private BigDecimal truckage;

    private Integer type;

    private String sort;

    private String remark;

    private Long principal;

    private Boolean enabled;

    private Long tenantId;

    private String deleteFlag;

    private Boolean isDefault;

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public void setWarehousing(BigDecimal warehousing) {
        this.warehousing = warehousing;
    }

    public void setTruckage(BigDecimal truckage) {
        this.truckage = truckage;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setSort(String sort) {
        this.sort = sort == null ? null : sort.trim();
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public void setPrincipal(Long principal) {
        this.principal = principal;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public void setDeleteFlag(String deleteFlag) {
        this.deleteFlag = deleteFlag == null ? null : deleteFlag.trim();
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}