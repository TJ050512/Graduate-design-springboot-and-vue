import request from '@/utils/request'

// 分页查询用水记录列表
export function getUsagePage(params) {
  return request({
    url: '/waterUsage/page',
    method: 'get',
    params
  })
}

// 根据ID查询用水记录
export function getUsageById(id) {
  return request({
    url: `/waterUsage/${id}`,
    method: 'get'
  })
}

// 添加用水记录
export function addUsage(data) {
  return request({
    url: '/waterUsage',
    method: 'post',
    data
  })
}

// 更新用水记录
export function updateUsage(data) {
  return request({
    url: '/waterUsage',
    method: 'put',
    data
  })
}

// 确认用水记录
export function confirmUsage(id) {
  return request({
    url: `/waterUsage/confirm/${id}`,
    method: 'put'
  })
}

// 删除用水记录
export function deleteUsage(id) {
  return request({
    url: `/waterUsage/${id}`,
    method: 'delete'
  })
}

