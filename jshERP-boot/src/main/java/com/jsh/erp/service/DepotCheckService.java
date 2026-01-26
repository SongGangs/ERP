package com.jsh.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jsh.erp.constants.ExceptionConstants;
import com.jsh.erp.datasource.entities.*;
import com.jsh.erp.datasource.mappers.*;
import com.jsh.erp.datasource.vo.converter.DepotCheckHeadConverter;
import com.jsh.erp.datasource.vo.converter.DepotCheckItemConverter;
import com.jsh.erp.datasource.vo.enums.DepotCheckStatusEnum;
import com.jsh.erp.datasource.vo.req.DepotCheckItemSaveReq;
import com.jsh.erp.datasource.vo.req.DepotCheckQueryReq;
import com.jsh.erp.datasource.vo.req.DepotCheckSaveReq;
import com.jsh.erp.datasource.vo.req.DepotCheckLinkBillReq;
import com.jsh.erp.datasource.vo.resp.DepotCheckHeadDetailResp;
import com.jsh.erp.datasource.vo.resp.DepotCheckHeadListResp;
import com.jsh.erp.datasource.vo.resp.DepotCheckItemDetailResp;
import com.jsh.erp.exception.BizException;
import com.jsh.erp.utils.AssertUtils;
import com.jsh.erp.utils.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 盘点服务类 - 使用 MyBatis-Plus
 */
@Service
public class DepotCheckService {

    private static final Logger logger = LoggerFactory.getLogger(DepotCheckService.class);

    @Resource
    private DepotCheckHeadMapperEx depotCheckHeadMapper;

    @Resource
    private DepotCheckItemMapperEx depotCheckItemMapper;

    @Resource
    private MaterialMapper materialMapper;
    @Resource
    private MaterialMapperEx materialMapperEx;

    @Resource
    private MaterialCurrentStockMapper materialCurrentStockMapper;

    @Resource
    private DepotService depotService;

    @Resource
    private SequenceService sequenceService;

    @Resource
    private UserService userService;

    /**
     * 更新盘点单状态
     */
    private void updateCheckStatus(Long headId, Integer status) {
        logger.debug("更新盘点单状态，ID: {}, 新状态: {}", headId, status);
        LambdaUpdateWrapper<DepotCheckHead> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DepotCheckHead::getId, headId)
                .set(DepotCheckHead::getStatus, status);
        depotCheckHeadMapper.update(null, updateWrapper);
    }

    /**
     * 分页查询盘点单列表
     */
    public List<DepotCheckHeadListResp> selectList(DepotCheckQueryReq req) {
        LambdaQueryWrapper<DepotCheckHead> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtil.isNotEmpty(req.getCheckNumber()), DepotCheckHead::getCheckNumber, req.getCheckNumber())
                .eq(req.getDepotId() != null, DepotCheckHead::getDepotId, req.getDepotId())
                .eq(req.getStatus() != null, DepotCheckHead::getStatus, req.getStatus())
                .eq(Objects.nonNull(req.getOperatorId()), DepotCheckHead::getUpdater, req.getOperatorId())
                .ge(req.getBeginTime() != null, DepotCheckHead::getCheckDate, req.getBeginTime())
                .le(req.getEndTime() != null, DepotCheckHead::getCheckDate, req.getEndTime())
                .orderByDesc(DepotCheckHead::getCheckDate, DepotCheckHead::getId);

        List<DepotCheckHead> list = depotCheckHeadMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        // 批量查询仓库信息
        List<Long> depotIds = list.stream()
                .map(DepotCheckHead::getDepotId)
                .collect(Collectors.toList());
        Map<Long, String> depotNameMap = depotService.getMapByIds(depotIds);

        List<Long> userIds = list.stream().map(DepotCheckHead::getUpdater).collect(Collectors.toList());
        List<User> users = userService.listUserByIds(userIds);
        Map<Long, String> userNameMap = users.stream().collect(Collectors.toMap(User::getId, User::getUsername));

        // 使用 Converter 转换
        return list.stream()
                .map(head -> DepotCheckHeadConverter.toListResp(head, depotNameMap.get(head.getDepotId()), userNameMap.get(head.getUpdater())))
                .collect(Collectors.toList());
    }

    /**
     * 保存盘点单（暂存）
     * 允许盘点数量为空，用于暂存数据
     */
    @Transactional(rollbackFor = Exception.class)
    public DepotCheckHead saveCheck(DepotCheckSaveReq req, HttpServletRequest request) throws Exception {
        return saveCheckInternal(req, request, false);
    }

    /**
     * 提交并审核盘点单
     * 必须校验盘点数量已填写
     */
    @Transactional(rollbackFor = Exception.class)
    public DepotCheckHead submitAndApproveCheck(DepotCheckSaveReq req, HttpServletRequest request) throws Exception {
        DepotCheckHead head = saveCheckInternal(req, request, true);
        updateCheckStatus(head.getId(), DepotCheckStatusEnum.CHECKED_APPROVED.getType());
        head.setStatus(DepotCheckStatusEnum.CHECKED_APPROVED.getType());
        return head;
    }

    /**
     * 只提交盘点（不审核）
     * 保存盘点单数据并提交，但不自动审核，状态变为"已盘点（未审核）"
     * 必须校验盘点数量已填写
     */
    @Transactional(rollbackFor = Exception.class)
    public DepotCheckHead submitCheckOnly(DepotCheckSaveReq req, HttpServletRequest request) throws Exception {
        DepotCheckHead head = saveCheckInternal(req, request, true);
        updateCheckStatus(head.getId(), DepotCheckStatusEnum.CHECKED_UNAPPROVED.getType());
        head.setStatus(DepotCheckStatusEnum.CHECKED_UNAPPROVED.getType());
        return head;
    }

    /**
     * 保存盘点单内部方法
     *
     * @param validateActualNumber 是否校验盘点数量必填
     */
    private DepotCheckHead saveCheckInternal(DepotCheckSaveReq req, HttpServletRequest request,
                                             boolean validateActualNumber) throws Exception {
        // 校验参数
        AssertUtils.assertNotNull(req.getDepotId(), "仓库ID不能为空");
        AssertUtils.assertNotNull(req.getCheckDate(), "盘点日期不能为空");
        AssertUtils.assertNotEmpty(req.getItems(), "盘点明细不能为空");

        // 获取当前用户
        Long userId = userService.getUserId(request);

        DepotCheckHead head;
        boolean isUpdate = req.getId() != null;

        if (isUpdate) {
            logger.info("更新盘点单，ID: {}, 盘点单号: {}", req.getId(), req.getCheckNumber());
            // 更新模式
            head = depotCheckHeadMapper.selectById(req.getId());
            AssertUtils.assertNotNull(head, "盘点单不存在");

            // 只允许状态为 1（盘点中）和 2（已盘点未审核）的单据修改
            Integer status = head.getStatus();
            if (!DepotCheckStatusEnum.CHECKING.getType().equals(status)
                    && !DepotCheckStatusEnum.CHECKED_UNAPPROVED.getType().equals(status)) {
                throw new BizException(ExceptionConstants.DATA_READ_FAIL_CODE,
                        "盘点单状态为【" + DepotCheckStatusEnum.getDescByType(status) + "】，不允许修改");
            }

            // 校验盘点编码不能变更
            if (!Objects.equals(head.getCheckNumber(), req.getCheckNumber())) {
                throw new BizException(ExceptionConstants.DATA_READ_FAIL_CODE, "盘点编码不能变更");
            }

            // 更新基本信息
            DepotCheckHead toUpdateHead = DepotCheckHead.builder()
                    .id(head.getId())
                    .status(DepotCheckStatusEnum.CHECKING.getType())
                    .remark(req.getRemark())
                    .fileName(req.getFileNames())
                    .updater(userId)
                    .build();
            depotCheckHeadMapper.updateById(toUpdateHead);

            // 删除原有明细
            LambdaQueryWrapper<DepotCheckItem> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(DepotCheckItem::getHeaderId, head.getId());
            depotCheckItemMapper.delete(deleteWrapper);
            logger.info("删除原有明细，盘点单ID: {}", head.getId());

        } else {
            logger.info("创建盘点单，盘点单号: {}, 仓库ID: {}", req.getCheckNumber(), req.getDepotId());
            // 创建模式
            // 校验盘点编码是否重复
            LambdaQueryWrapper<DepotCheckHead> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(DepotCheckHead::getCheckNumber, req.getCheckNumber())
                    .last(" limit 1");
            DepotCheckHead existHead = depotCheckHeadMapper.selectOne(checkWrapper);
            if (existHead != null) {
                throw new BizException(ExceptionConstants.DATA_READ_FAIL_CODE,
                        "盘点编码【" + req.getCheckNumber() + "】已存在，请重试");
            }

            head = DepotCheckHead.builder()
                    .checkNumber(req.getCheckNumber())
                    .depotId(req.getDepotId())
                    .checkDate(req.getCheckDate())
                    .status(DepotCheckStatusEnum.CHECKING.getType())
                    .remark(req.getRemark())
                    .fileName(req.getFileNames())
                    .creator(userId)
                    .updater(userId)
                    .deleteFlag(0)
                    .build();

            depotCheckHeadMapper.insert(head);
            logger.info("盘点单创建成功，ID: {}, 单号: {}", head.getId(), head.getCheckNumber());
        }

        // 保存明细并计算统计数据
        saveItemsAndCalculate(head, req.getItems(), validateActualNumber);

        return head;
    }

    /**
     * 保存明细并计算主表统计数据
     *
     * @param validateActualNumber 是否校验盘点数量必填
     */
    private void saveItemsAndCalculate(DepotCheckHead head, List<DepotCheckItemSaveReq> itemReqs,
                                       boolean validateActualNumber) {
        List<DepotCheckItem> items = new ArrayList<>();

        for (DepotCheckItemSaveReq itemReq : itemReqs) {
            AssertUtils.assertNotNull(itemReq.getMaterialId(), "商品ID不能为空");

            // 根据参数决定是否校验盘点数量
            if (validateActualNumber) {
                AssertUtils.assertNotNull(itemReq.getActualNumber(), "盘点数量不能为空");
            }

            // 查询商品信息
            Material material = materialMapper.selectByPrimaryKey(itemReq.getMaterialId());
            AssertUtils.assertNotNull(material, "商品不存在：" + itemReq.getMaterialId());

            // 查询当前库存和单价
            MaterialCurrentStockExample stockExample = new MaterialCurrentStockExample();
            stockExample.createCriteria()
                    .andMaterialIdEqualTo(itemReq.getMaterialId())
                    .andDepotIdEqualTo(head.getDepotId())
                    .andDeleteFlagEqualTo("0");
            List<MaterialCurrentStock> stocks = materialCurrentStockMapper.selectByExample(stockExample);

            BigDecimal originNumber = BigDecimal.ZERO;
            BigDecimal unitPrice = BigDecimal.ZERO;
            if (!stocks.isEmpty()) {
                MaterialCurrentStock stock = stocks.get(0);
                originNumber = stock.getCurrentNumber() != null ? stock.getCurrentNumber() : BigDecimal.ZERO;
                unitPrice = stock.getCurrentUnitPrice() != null ? stock.getCurrentUnitPrice() : BigDecimal.ZERO;
            }

            // 计算金额
            BigDecimal actualAmount = null;
            if (Objects.nonNull(itemReq.getActualNumber())) {
                BigDecimal actualNumber = new BigDecimal(itemReq.getActualNumber());
                actualAmount = actualNumber.multiply(unitPrice);
            }
            BigDecimal originAmount = originNumber.multiply(unitPrice);

            // 创建明细
            DepotCheckItem item = DepotCheckItem.builder()
                    .headerId(head.getId())
                    .materialId(itemReq.getMaterialId())
                    .originNumber(originNumber.intValue())
                    .actualNumber(itemReq.getActualNumber())
                    .unitPrice(unitPrice)
                    .originAmount(originAmount)
                    .actualAmount(actualAmount)
                    .remark(itemReq.getRemark())
                    .deleteFlag(0)
                    .build();
            items.add(item);
        }
        depotCheckItemMapper.insert(items);
        logger.info("盘点明细保存成功，盘点单ID: {}, 明细数量: {}", head.getId(), items.size());

        // 根据明细计算主表统计数据
        calculateHeadSummary(head, items);
    }

    /**
     * 根据明细计算主表统计数据
     */
    private void calculateHeadSummary(DepotCheckHead head, List<DepotCheckItem> items) {
        int shouldCheckNumber = items.size();

        // 过滤出有效的明细（盘点数量不为空）
        List<DepotCheckItem> validItems = items.stream()
                .filter(item -> Objects.nonNull(item.getActualNumber()))
                .collect(Collectors.toList());

        int checkedNumber = validItems.size();
        int originNumber = (int) validItems.stream().filter(item -> item.getOriginNumber() > 0).count();
        int actualNumber = (int) validItems.stream().filter(item -> item.getActualNumber() > 0).count();

        BigDecimal originAmount = validItems.stream()
                .map(DepotCheckItem::getOriginAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal actualAmount = validItems.stream()
                .map(DepotCheckItem::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算盘亏
        List<DepotCheckItem> lossItems = validItems.stream()
                .filter(item -> item.getActualNumber() < item.getOriginNumber())
                .collect(Collectors.toList());
        int lossNumber = lossItems.size();
        BigDecimal lossAmount = lossItems.stream()
                .map(item -> item.getOriginAmount().subtract(item.getActualAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算盘盈
        List<DepotCheckItem> profitItems = validItems.stream()
                .filter(item -> item.getActualNumber() > item.getOriginNumber())
                .collect(Collectors.toList());
        int profitNumber = profitItems.size();
        BigDecimal profitAmount = profitItems.stream()
                .map(item -> item.getActualAmount().subtract(item.getOriginAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 更新主表统计数据
        LambdaUpdateWrapper<DepotCheckHead> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DepotCheckHead::getId, head.getId())
                .set(DepotCheckHead::getShouldCheckNumber, shouldCheckNumber)
                .set(DepotCheckHead::getCheckedNumber, checkedNumber)
                .set(DepotCheckHead::getOriginNumber, originNumber)
                .set(DepotCheckHead::getActualNumber, actualNumber)
                .set(DepotCheckHead::getOriginAmount, originAmount)
                .set(DepotCheckHead::getActualAmount, actualAmount)
                .set(DepotCheckHead::getLossNumber, lossNumber)
                .set(DepotCheckHead::getLossAmount, lossAmount)
                .set(DepotCheckHead::getProfitNumber, profitNumber)
                .set(DepotCheckHead::getProfitAmount, profitAmount);
        depotCheckHeadMapper.update(null, updateWrapper);
    }

    /**
     * 获取盘点单详情
     */
    public DepotCheckHeadDetailResp getDetail(Long id) {
        // 查询主表
        DepotCheckHead head = depotCheckHeadMapper.selectById(id);
        AssertUtils.assertNotNull(head, "盘点单不存在");

        // 获取仓库名称
        String depotName = null;
        if (head.getDepotId() != null) {
            Depot depot = depotService.getDepot(head.getDepotId());
            if (depot != null) {
                depotName = depot.getName();
            }
        }

        // 使用 Converter 转换
        DepotCheckHeadDetailResp resp = DepotCheckHeadConverter.toDetailResp(head, depotName);

        User user = userService.getUser(head.getUpdater());
        if (Objects.nonNull(user)) {
            resp.setOperatorName(user.getUsername());
        }

        // 查询明细列表
        LambdaQueryWrapper<DepotCheckItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(DepotCheckItem::getHeaderId, id)
                .orderByAsc(DepotCheckItem::getId);
        List<DepotCheckItem> items = depotCheckItemMapper.selectList(itemWrapper);

        if (CollectionUtils.isEmpty(items)) {
            return resp;
        }

        // 批量查询商品信息
        List<Long> materialIds = items.stream()
                .map(DepotCheckItem::getMaterialId)
                .collect(Collectors.toList());
        List<MaterialVo4Unit> materialVo4Units = materialMapperEx.selectByConditionMaterial(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, materialIds, null);
        Map<Long, MaterialVo4Unit> materialMap = materialVo4Units.stream()
                .collect(Collectors.toMap(MaterialVo4Unit::getId, Function.identity()));

        // 使用 Converter 转换
        List<DepotCheckItemDetailResp> itemResps = items.stream()
                .map(item -> DepotCheckItemConverter.toDetailResp(item, materialMap.get(item.getMaterialId())))
                .collect(Collectors.toList());

        resp.setItems(itemResps);
        return resp;
    }

    /**
     * 提交盘点（完成盘点）
     * 需要校验所有明细中盘点数量，与提交并审核盘点单逻辑一致
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitCheck(Long id) {
        DepotCheckHead head = depotCheckHeadMapper.selectById(id);
        AssertUtils.assertNotNull(head, "盘点单不存在");
        if (DepotCheckStatusEnum.CHECKING.notType(head.getStatus()) && DepotCheckStatusEnum.CHECKED_UNAPPROVED.notType(head.getStatus())) {
            throw new BizException(ExceptionConstants.DATA_READ_FAIL_CODE, "只有盘点中的单据才能提交");
        }

        // 查询所有明细
        LambdaQueryWrapper<DepotCheckItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(DepotCheckItem::getHeaderId, id);
        List<DepotCheckItem> items = depotCheckItemMapper.selectList(itemWrapper);

        // 校验明细不能为空
        AssertUtils.assertNotEmpty(items, "盘点明细不能为空");

        // 校验所有明细的盘点数量必填（与提交并审核逻辑一致）
        for (DepotCheckItem item : items) {
            AssertUtils.assertNotNull(item.getActualNumber(), "存在未填写盘点数量的商品，请完成所有商品的盘点");
        }

        updateCheckStatus(id, DepotCheckStatusEnum.CHECKED_UNAPPROVED.getType());
    }

    /**
     * 审核盘点单
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveCheck(Long id) {
        DepotCheckHead head = depotCheckHeadMapper.selectById(id);
        AssertUtils.assertNotNull(head, "盘点单不存在");
        AssertUtils.assertEquals(head.getStatus(), DepotCheckStatusEnum.CHECKED_UNAPPROVED.getType(), "只有已盘点（未审核）的单据才能审核");

        updateCheckStatus(id, DepotCheckStatusEnum.CHECKED_APPROVED.getType());
    }

    /**
     * 批量审核盘点单
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchApprove(List<Long> headIds) {
        AssertUtils.assertNotEmpty(headIds, "盘点单ID列表不能为空");

        List<DepotCheckHead> heads = depotCheckHeadMapper.selectByIds(headIds);
        AssertUtils.assertNotEmpty(heads, "盘点单不存在");

        // 校验所有单据状态
        for (DepotCheckHead head : heads) {
            if (!DepotCheckStatusEnum.CHECKED_UNAPPROVED.getType().equals(head.getStatus())) {
                throw new BizException(ExceptionConstants.DATA_READ_FAIL_CODE,
                        "盘点单【" + head.getCheckNumber() + "】状态不是已盘点（未审核），不能审核");
            }
        }

        // 批量更新状态
        LambdaUpdateWrapper<DepotCheckHead> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(DepotCheckHead::getId, headIds)
                .set(DepotCheckHead::getStatus, DepotCheckStatusEnum.CHECKED_APPROVED.getType());
        depotCheckHeadMapper.update(null, updateWrapper);
    }

    /**
     * 批量反审核盘点单
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUnApprove(List<Long> headIds) {
        AssertUtils.assertNotEmpty(headIds, "盘点单ID列表不能为空");

        List<DepotCheckHead> heads = depotCheckHeadMapper.selectByIds(headIds);
        AssertUtils.assertNotEmpty(heads, "盘点单不存在");

        // 校验所有单据状态
        for (DepotCheckHead head : heads) {
            if (!DepotCheckStatusEnum.CHECKED_APPROVED.getType().equals(head.getStatus())) {
                throw new BizException(ExceptionConstants.DATA_READ_FAIL_CODE,
                        "盘点单【" + head.getCheckNumber() + "】状态不是已盘点（已审核），不能反审核");
            }
            if (StringUtils.isNotBlank(head.getRkNumber()) || StringUtils.isNotBlank(head.getCkNumber())) {
                throw new BizException(ExceptionConstants.DATA_READ_FAIL_CODE,
                        "盘点单【" + head.getCheckNumber() + "】已生成出入库单，不能反审核");
            }
        }

        // 批量更新状态
        LambdaUpdateWrapper<DepotCheckHead> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(DepotCheckHead::getId, headIds)
                .set(DepotCheckHead::getStatus, DepotCheckStatusEnum.CHECKED_UNAPPROVED.getType());
        depotCheckHeadMapper.update(null, updateWrapper);
    }

    /**
     * 删除盘点单
     * 只允许状态为 1（盘点中）和 2（已盘点未审核）的单据删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> headIds) {
        List<DepotCheckHead> heads = depotCheckHeadMapper.selectByIds(headIds);
        AssertUtils.assertNotEmpty(heads, "盘点单不存在");

        // 检查是否有不允许删除的单据（状态不是 1 或 2）
        for (DepotCheckHead head : heads) {
            Integer status = head.getStatus();
            if (!DepotCheckStatusEnum.CHECKING.getType().equals(status)
                    && !DepotCheckStatusEnum.CHECKED_UNAPPROVED.getType().equals(status)) {
                throw new BizException(ExceptionConstants.DATA_READ_FAIL_CODE,
                        "盘点单【" + head.getCheckNumber() + "】状态为【" + DepotCheckStatusEnum.getDescByType(status) + "】，不允许删除");
            }
        }

        // 删除明细 - 使用 Lambda 删除
        LambdaQueryWrapper<DepotCheckItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(DepotCheckItem::getHeaderId, headIds);
        depotCheckItemMapper.delete(itemWrapper);

        // 删除主表 - MyBatis-Plus 会自动使用逻辑删除
        depotCheckHeadMapper.deleteByIds(headIds);
    }

    /**
     * 关联出入库单并调整盘点状态
     * 前端根据盈亏数量生成出入库单后，将单号传入
     * 支持多次调用，分别关联出库单和入库单
     * 只有当所有需要的单号都关联完成后，才更新状态为"已调整"
     */
    @Transactional(rollbackFor = Exception.class)
    public void linkBillAndAdjust(DepotCheckLinkBillReq req) {
        AssertUtils.assertNotBlank(req.getCheckNumber(), "盘点单编码不能为空");
        logger.info("关联出入库单，盘点单号: {}, 入库单号: {}, 出库单号: {}",
                req.getCheckNumber(), req.getRkNumber(), req.getCkNumber());

        // 查询盘点单
        LambdaQueryWrapper<DepotCheckHead> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DepotCheckHead::getCheckNumber, req.getCheckNumber())
                .last(" limit 1");
        DepotCheckHead head = depotCheckHeadMapper.selectOne(queryWrapper);
        AssertUtils.assertNotNull(head, "盘点单不存在");
        AssertUtils.assertEquals(head.getStatus(), DepotCheckStatusEnum.CHECKED_APPROVED.getType(),
                "只有已盘点（已审核）的单据才能关联出入库单");

        // 校验：至少需要提供一个单号
        if (StringUtil.isEmpty(req.getRkNumber()) && StringUtil.isEmpty(req.getCkNumber())) {
            throw new BizException(ExceptionConstants.DATA_READ_FAIL_CODE,
                    "盘盈入库单号和盘亏出库单号至少需要提供一个");
        }

        boolean hasProfitItems = head.getProfitNumber() != null && head.getProfitNumber() > 0;
        boolean hasLossItems = head.getLossNumber() != null && head.getLossNumber() > 0;

        // 更新盘点单的出入库单号
        LambdaUpdateWrapper<DepotCheckHead> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DepotCheckHead::getId, head.getId());

        // 处理盘盈入库单号
        if (StringUtil.isNotEmpty(req.getRkNumber())) {
            validateBillLink(hasProfitItems, head.getRkNumber(), "入库单", "盘盈");
            updateWrapper.set(DepotCheckHead::getRkNumber, req.getRkNumber());
        }

        // 处理盘亏出库单号
        if (StringUtil.isNotEmpty(req.getCkNumber())) {
            validateBillLink(hasLossItems, head.getCkNumber(), "出库单", "盘亏");
            updateWrapper.set(DepotCheckHead::getCkNumber, req.getCkNumber());
        }

        // 更新单号
        depotCheckHeadMapper.update(null, updateWrapper);

        // 重新查询盘点单，获取最新的单号信息
        head = depotCheckHeadMapper.selectById(head.getId());

        // 检查是否所有需要的单号都已关联完成
        boolean allLinked = (!hasProfitItems || StringUtil.isNotEmpty(head.getRkNumber()))
                && (!hasLossItems || StringUtil.isNotEmpty(head.getCkNumber()));

        // 只有当所有需要的单号都关联完成后，才更新状态为"已调整"
        if (allLinked) {
            updateCheckStatus(head.getId(), DepotCheckStatusEnum.ADJUSTED.getType());
            logger.info("盘点单已完成调整，盘点单号: {}, 入库单号: {}, 出库单号: {}",
                    head.getCheckNumber(), head.getRkNumber(), head.getCkNumber());
        } else {
            logger.info("盘点单部分关联完成，盘点单号: {}, 等待其他单号关联", head.getCheckNumber());
        }
    }

    /**
     * 校验单据关联
     */
    private void validateBillLink(boolean hasItems, String existingNumber, String billType, String itemType) {
        if (!hasItems) {
            throw new BizException(ExceptionConstants.DATA_READ_FAIL_CODE,
                    "该盘点单没有" + itemType + "商品，不需要关联" + billType);
        }
        if (StringUtil.isNotEmpty(existingNumber)) {
            throw new BizException(ExceptionConstants.DATA_READ_FAIL_CODE,
                    "该盘点单已关联" + billType + "【" + existingNumber + "】，不能重复关联");
        }
    }
}