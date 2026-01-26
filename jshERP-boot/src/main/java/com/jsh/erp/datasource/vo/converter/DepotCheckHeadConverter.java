package com.jsh.erp.datasource.vo.converter;

import com.jsh.erp.datasource.entities.DepotCheckHead;
import com.jsh.erp.datasource.vo.enums.DepotCheckStatusEnum;
import com.jsh.erp.datasource.vo.resp.DepotCheckHeadListResp;
import com.jsh.erp.datasource.vo.resp.DepotCheckHeadDetailResp;

/**
 * 盘点单转换器
 */
public class DepotCheckHeadConverter {

    /**
     * 转换为列表响应对象
     */
    public static DepotCheckHeadListResp toListResp(DepotCheckHead entity, String depotName, String userName) {
        if (entity == null) {
            return null;
        }

        DepotCheckHeadListResp resp = new DepotCheckHeadListResp();
        resp.setId(entity.getId());
        resp.setCheckNumber(entity.getCheckNumber());
        resp.setDepotId(entity.getDepotId());
        resp.setDepotName(depotName);
        resp.setOperatorName(userName);
        resp.setCheckDate(entity.getCheckDate());
        resp.setShouldCheckNumber(entity.getShouldCheckNumber());
        resp.setCheckedNumber(entity.getCheckedNumber());
        resp.setUncheckNumber(entity.getShouldCheckNumber() - entity.getCheckedNumber());
        resp.setOriginNumber(entity.getOriginNumber());
        resp.setActualNumber(entity.getActualNumber());
        resp.setOriginAmount(entity.getOriginAmount());
        resp.setActualAmount(entity.getActualAmount());
        resp.setDiffAmount(entity.getActualAmount().subtract(entity.getOriginAmount()));
        resp.setLossNumber(entity.getLossNumber());
        resp.setLossAmount(entity.getLossAmount());
        resp.setProfitNumber(entity.getProfitNumber());
        resp.setProfitAmount(entity.getProfitAmount());
        resp.setStatus(entity.getStatus());
        resp.setStatusName(DepotCheckStatusEnum.getDescByType(entity.getStatus()));
        resp.setRemark(entity.getRemark());
        return resp;
    }

    /**
     * 转换为详情响应对象
     */
    public static DepotCheckHeadDetailResp toDetailResp(DepotCheckHead entity, String depotName) {
        if (entity == null) {
            return null;
        }

        DepotCheckHeadDetailResp resp = new DepotCheckHeadDetailResp();
        resp.setId(entity.getId());
        resp.setCheckNumber(entity.getCheckNumber());
        resp.setDepotId(entity.getDepotId());
        resp.setDepotName(depotName);
        resp.setCheckDate(entity.getCheckDate());
        resp.setShouldCheckNumber(entity.getShouldCheckNumber());
        resp.setCheckedNumber(entity.getCheckedNumber());
        resp.setUncheckNumber(entity.getShouldCheckNumber() - entity.getCheckedNumber());
        resp.setOriginAmount(entity.getOriginAmount());
        resp.setActualAmount(entity.getActualAmount());
        resp.setDiffAmount(entity.getActualAmount().subtract(entity.getOriginAmount()));
        resp.setLossNumber(entity.getLossNumber());
        resp.setLossAmount(entity.getLossAmount());
        resp.setProfitNumber(entity.getProfitNumber());
        resp.setProfitAmount(entity.getProfitAmount());
        resp.setRkNumber(entity.getRkNumber());
        resp.setCkNumber(entity.getCkNumber());
        resp.setStatus(entity.getStatus());
        resp.setRemark(entity.getRemark());
        resp.setFileNames(entity.getFileName());
        return resp;
    }
}