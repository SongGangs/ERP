/**
 * 库存盘点 API
 * Base URL: /depotCheck
 * Version: v1.0
 * Updated: 2026-01-20
 */
import { axios } from '@/utils/request'

const api = {
  list: '/depotCheck/list',
  save: '/depotCheck/save',
  submitAndApprove: '/depotCheck/submitAndApprove',
  detail: '/depotCheck/detail',
  submit: '/depotCheck/submit',
  batchDelete: '/depotCheck/batchDelete'
}

/**
 * 盘点单状态枚举
 */
export const DepotCheckStatus = {
  CHECKING: 1,              // 盘点中 - 初始状态，可以编辑、删除
  CHECKED_UNAPPROVED: 2,    // 已盘点（未审核）- 已提交待审核，不可编辑
  CHECKED_APPROVED: 3,      // 已盘点（已审核）- 已审核通过，不可修改
  ADJUSTED: 4               // 已调整 - 已生成出入库单据
}

/**
 * 盘点单状态名称映射
 */
export const DepotCheckStatusName = {
  [DepotCheckStatus.CHECKING]: '盘点中',
  [DepotCheckStatus.CHECKED_UNAPPROVED]: '已盘点（未审核）',
  [DepotCheckStatus.CHECKED_APPROVED]: '已盘点（已审核）',
  [DepotCheckStatus.ADJUSTED]: '已调整'
}

/**
 * 差异状态枚举
 */
export const DiffStatus = {
  PROFIT: '盘盈',  // 实际数量 > 账面数量
  LOSS: '盘亏',    // 实际数量 < 账面数量
  EQUAL: '盘平'    // 实际数量 = 账面数量
}

/**
 * 查询盘点单列表
 * @param {Object} params - 查询参数
 * @param {string} params.checkNumber - 盘点编号（选填）
 * @param {number} params.depotId - 仓库ID（选填）
 * @param {number} params.status - 状态：1-盘点中，2-已盘点（未审核），3-已盘点（已审核），4-已调整（选填）
 * @param {string} params.materialInfo - 商品信息（选填）
 * @param {string} params.startDate - 开始日期时间戳（选填）
 * @param {string} params.endDate - 结束日期时间戳（选填）
 * @param {string} params.remark - 备注关键字（选填）
 * @param {number} params.currentPage - 当前页码
 * @param {number} params.pageSize - 每页数量
 */
export function getCheckList(params) {
  return axios({
    url: api.list,
    method: 'get',
    params: params
  })
}

/**
 * 保存盘点单（暂存模式 - actualNumber可为空）
 * @param {Object} data - 盘点单数据
 * @param {number} data.id - 盘点单ID（更新时必传，创建时不传）
 * @param {number} data.depotId - 仓库ID
 * @param {string} data.checkDate - 盘点日期
 * @param {string} data.remark - 备注
 * @param {Array} data.items - 盘点明细列表
 */
export function saveCheck(data) {
  return axios({
    url: api.save,
    method: 'post',
    data: data
  })
}

/**
 * 提交并审核盘点单（严格模式 - actualNumber必填）
 * @param {Object} data - 盘点单数据
 * @param {number} data.id - 盘点单ID（更新时必传，创建时不传）
 * @param {number} data.depotId - 仓库ID
 * @param {string} data.checkDate - 盘点日期
 * @param {string} data.remark - 备注
 * @param {Array} data.items - 盘点明细列表（actualNumber必填）
 */
export function submitAndApproveCheck(data) {
  return axios({
    url: api.submitAndApprove,
    method: 'post',
    data: data
  })
}

/**
 * 获取盘点单详情
 * @param {number} id - 盘点单ID
 */
export function getCheckDetail(id) {
  return axios({
    url: api.detail,
    method: 'post',
    data: { id }
  })
}

/**
 * 提交盘点
 * @param {number} id - 盘点单ID
 */
export function submitCheck(id) {
  return axios({
    url: api.submit,
    method: 'post',
    data: { id }
  })
}

/**
 * 提交盘点（不审核）
 * @param {Object} data - 盘点单数据
 * @param {number} data.id - 盘点单ID（更新时必传，创建时不传）
 * @param {number} data.depotId - 仓库ID
 * @param {string} data.checkDate - 盘点日期
 * @param {string} data.remark - 备注
 * @param {Array} data.items - 盘点明细列表（actualNumber必填）
 */
export function submitOnlyCheck(data) {
  return axios({
    url: '/depotCheck/submitOnly',
    method: 'post',
    data: data
  })
}

/**
 * 批量审核盘点单
 * @param {Array} headIds - 盘点单ID列表
 */
export function batchApproveCheck(headIds) {
  return axios({
    url: '/depotCheck/batchApprove',
    method: 'post',
    data: { headIds }
  })
}

/**
 * 批量反审核盘点单
 * @param {Array} headIds - 盘点单ID列表
 */
export function batchUnApproveCheck(headIds) {
  return axios({
    url: '/depotCheck/batchUnApprove',
    method: 'post',
    data: { headIds }
  })
}

/**
 * 删除盘点单
 * @param {number} headIds - 盘点单ID列表
 */
export function batchDeleteCheck(headIds) {
  return axios({
    url: api.batchDelete,
    method: 'post',
    data: { headIds }
  })
}

export default {
  getCheckList,
  saveCheck,
  submitAndApproveCheck,
  submitOnlyCheck,
  batchApproveCheck,
  batchUnApproveCheck,
  getCheckDetail,
  submitCheck,
  batchDeleteCheck
}