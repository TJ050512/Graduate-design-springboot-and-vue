import request from '@/utils/request'

// 分页查询水表列表
export function getMeterPage(params) {
  return request({
    url: '/waterMeter/page',
    method: 'get',
    params
  })
}

// 根据ID查询水表
export function getMeterById(id) {
  return request({
    url: `/waterMeter/${id}`,
    method: 'get'
  })
}

// 添加水表
export function addMeter(data) {
  return request({
    url: '/waterMeter',
    method: 'post',
    data
  })
}

// 更新水表
export function updateMeter(data) {
  return request({
    url: '/waterMeter',
    method: 'put',
    data
  })
}

// 删除水表
export function deleteMeter(id) {
  return request({
    url: `/waterMeter/${id}`,
    method: 'delete'
  })
}

// 查询当前用户的水表列表
export function getMyMeters() {
  return request({
    url: '/waterMeter/my',
    method: 'get'
  })
}

// ========== 水表模拟器 API ==========

// 获取模拟器状态
export function getSimulatorStatus() {
  return request({
    url: '/simulator/status',
    method: 'get'
  })
}

// 启用/禁用模拟器
export function toggleSimulator(enabled) {
  return request({
    url: '/simulator/toggle',
    method: 'put',
    params: { enabled }
  })
}

// 设置模拟速度倍率
export function setSimulatorSpeed(multiplier) {
  return request({
    url: '/simulator/speed',
    method: 'put',
    params: { multiplier }
  })
}

// 立即执行一次模拟
export function triggerSimulation() {
  return request({
    url: '/simulator/trigger',
    method: 'post'
  })
}

