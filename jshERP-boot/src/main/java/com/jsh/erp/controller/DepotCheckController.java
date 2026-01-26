package com.jsh.erp.controller;

import com.jsh.erp.base.BaseController;
import com.jsh.erp.base.TableDataInfo;
import com.jsh.erp.datasource.entities.DepotCheckHead;
import com.jsh.erp.datasource.vo.req.*;
import com.jsh.erp.datasource.vo.resp.*;
import com.jsh.erp.service.DepotCheckService;
import com.jsh.erp.utils.BaseResponseInfo;
import com.jsh.erp.utils.ErpInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jsh.erp.utils.ResponseJsonUtil.returnJson;

/**
 * 库存盘点Controller
 */
@RestController
@RequestMapping(value = "/depotCheck")
@Api(tags = {"库存盘点管理"})
public class DepotCheckController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(DepotCheckController.class);

    @Resource
    private DepotCheckService depotCheckService;

    /**
     * 构建盘点单保存响应
     */
    private BaseResponseInfo buildSaveResponse(DepotCheckHead head) {
        BaseResponseInfo res = new BaseResponseInfo();
        DepotCheckSaveResp resp = new DepotCheckSaveResp();
        resp.setId(head.getId());
        resp.setCheckNumber(head.getCheckNumber());
        resp.setStatus(head.getStatus());
        res.code = 200;
        res.data = resp;
        return res;
    }

    /**
     * 构建操作成功响应
     */
    private BaseResponseInfo buildSuccessResponse(String message) {
        BaseResponseInfo res = new BaseResponseInfo();
        res.code = 200;
        res.data = message;
        return res;
    }

    /**
     * 构建操作失败响应
     */
    private BaseResponseInfo buildErrorResponse(Exception e) {
        BaseResponseInfo res = new BaseResponseInfo();
        res.code = 500;
        res.data = e.getMessage();
        return res;
    }

    /**
     * 盘点单列表查询
     */
    @GetMapping(value = "/list")
    @ApiOperation(value = "获取盘点单列表")
    public TableDataInfo getList(DepotCheckQueryReq req) {
        startPage();
        List<DepotCheckHeadListResp> list = depotCheckService.selectList(req);
        return getDataTable(list);
    }

    /**
     * 保存盘点单（创建或更新）- 暂存模式
     * 同时保存主表和明细数据
     * 盘点数量字段可以为空，作为草稿保存
     */
    @PostMapping(value = "/save")
    @ApiOperation(value = "保存盘点单(暂存)")
    public BaseResponseInfo save(@RequestBody @Valid DepotCheckSaveReq req,
                                 HttpServletRequest request) {
        try {
            DepotCheckHead head = depotCheckService.saveCheck(req, request);
            return buildSaveResponse(head);
        } catch (Exception e) {
            logger.error("保存盘点单失败，盘点单号: {}, 错误: {}", req.getCheckNumber(), e.getMessage(), e);
            return buildErrorResponse(e);
        }
    }

    /**
     * 提交并审核盘点单 - 严格校验模式
     * 同时保存主表和明细数据，并自动审核通过
     * 盘点数量字段必填，校验所有商品都已盘点
     */
    @PostMapping(value = "/submitAndApprove")
    @ApiOperation(value = "提交并审核盘点单")
    public BaseResponseInfo submitAndApprove(@RequestBody @Valid DepotCheckSaveReq req,
                                             HttpServletRequest request) {
        try {
            DepotCheckHead head = depotCheckService.submitAndApproveCheck(req, request);
            return buildSaveResponse(head);
        } catch (Exception e) {
            logger.error("提交并审核盘点单失败，盘点单号: {}, 错误: {}", req.getCheckNumber(), e.getMessage(), e);
            return buildErrorResponse(e);
        }
    }

    /**
     * 只提交盘点（不审核）- 严格校验模式
     * 保存主表和明细数据并提交，但不自动审核
     * 盘点数量字段必填，校验所有商品都已盘点
     * 提交后状态变为"已盘点（未审核）"，需要后续手动审核
     */
    @PostMapping(value = "/submitOnly")
    @ApiOperation(value = "只提交盘点（不审核）")
    public BaseResponseInfo submitOnly(@RequestBody @Valid DepotCheckSaveReq req,
                                       HttpServletRequest request) {
        try {
            DepotCheckHead head = depotCheckService.submitCheckOnly(req, request);
            return buildSaveResponse(head);
        } catch (Exception e) {
            logger.error("提交盘点单失败，盘点单号: {}, 错误: {}", req.getCheckNumber(), e.getMessage(), e);
            return buildErrorResponse(e);
        }
    }

    /**
     * 盘点单详情
     */
    @PostMapping(value = "/detail")
    @ApiOperation(value = "获取盘点单详情")
    public String getDetail(@RequestBody @Valid DepotCheckIdReq req) {
        Map<String, Object> objectMap = new HashMap<>();
        try {
            DepotCheckHeadDetailResp detail = depotCheckService.getDetail(req.getId());
            objectMap.put("data", detail);
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } catch (Exception e) {
            objectMap.put("message", e.getMessage());
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    /**
     * 提交盘点（完成盘点）
     */
    @PostMapping(value = "/submit")
    @ApiOperation(value = "完成盘点")
    public BaseResponseInfo submit(@RequestBody @Valid DepotCheckIdReq req) {
        try {
            depotCheckService.submitCheck(req.getId());
            return buildSuccessResponse("提交成功");
        } catch (Exception e) {
            return buildErrorResponse(e);
        }
    }

    /**
     * 审核盘点单
     */
    @PostMapping(value = "/approve")
    @ApiOperation(value = "审核盘点单")
    public BaseResponseInfo approve(@RequestBody @Valid DepotCheckIdReq req) {
        try {
            depotCheckService.approveCheck(req.getId());
            return buildSuccessResponse("审核成功");
        } catch (Exception e) {
            logger.error("审核盘点单失败，ID: {}, 错误: {}", req.getId(), e.getMessage(), e);
            return buildErrorResponse(e);
        }
    }

    /**
     * 批量审核盘点单
     */
    @PostMapping(value = "/batchApprove")
    @ApiOperation(value = "批量审核盘点单")
    public BaseResponseInfo batchApprove(@RequestBody @Valid DepotCheckBatchIdReq req) {
        try {
            depotCheckService.batchApprove(req.getHeadIds());
            return buildSuccessResponse("批量审核成功");
        } catch (Exception e) {
            logger.error("批量审核盘点单失败，错误: {}", e.getMessage(), e);
            return buildErrorResponse(e);
        }
    }

    /**
     * 批量反审核盘点单
     */
    @PostMapping(value = "/batchUnApprove")
    @ApiOperation(value = "批量反审核盘点单")
    public BaseResponseInfo batchUnApprove(@RequestBody @Valid DepotCheckBatchIdReq req) {
        try {
            depotCheckService.batchUnApprove(req.getHeadIds());
            return buildSuccessResponse("批量反审核成功");
        } catch (Exception e) {
            logger.error("批量反审核盘点单失败，错误: {}", e.getMessage(), e);
            return buildErrorResponse(e);
        }
    }

    /**
     * 删除盘点单
     */
    @PostMapping(value = "/batchDelete")
    @ApiOperation(value = "删除盘点单")
    public BaseResponseInfo batchDelete(@RequestBody @Valid DepotCheckBatchIdReq req) {
        try {
            depotCheckService.batchDelete(req.getHeadIds());
            return buildSuccessResponse("删除成功");
        } catch (Exception e) {
            logger.error("删除盘点单失败，错误: {}", e.getMessage(), e);
            return buildErrorResponse(e);
        }
    }
}