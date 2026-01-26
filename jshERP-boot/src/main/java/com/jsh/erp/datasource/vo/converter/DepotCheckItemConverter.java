package com.jsh.erp.datasource.vo.converter;

import com.jsh.erp.datasource.entities.DepotCheckItem;
import com.jsh.erp.datasource.entities.MaterialVo4Unit;
import com.jsh.erp.datasource.vo.resp.DepotCheckItemDetailResp;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 盘点明细转换器
 */
public class DepotCheckItemConverter {

    /**
     * 转换为详情响应对象
     */
    public static DepotCheckItemDetailResp toDetailResp(DepotCheckItem entity, MaterialVo4Unit material) {
        if (entity == null) {
            return null;
        }

        DepotCheckItemDetailResp resp = new DepotCheckItemDetailResp();
        resp.setId(entity.getId());
        resp.setMaterialId(entity.getMaterialId());
        resp.setOriginNumber(entity.getOriginNumber());
        resp.setActualNumber(entity.getActualNumber());
        resp.setUnitPrice(entity.getUnitPrice());
        resp.setOriginAmount(entity.getOriginAmount());
        resp.setActualAmount(entity.getActualAmount());
        resp.setRemark(entity.getRemark());

        if (Objects.nonNull(entity.getActualNumber())){
            resp.setDiffAmount(entity.getActualAmount().subtract(entity.getOriginAmount()));
            int diffNumber = entity.getActualNumber() - entity.getOriginNumber();
            resp.setDiffNumber(diffNumber);
        }else {
            resp.setDiffAmount(BigDecimal.ZERO);
        }

        // 设置商品信息
        if (material != null) {
            resp.setMBarCode(material.getmBarCode());
            resp.setName(material.getName());
            resp.setStandard(material.getStandard());
            resp.setUnit(material.getUnit());
            resp.setCategoryName(material.getCategoryName());
            resp.setSku(material.getSku());
        }

        return resp;
    }
}