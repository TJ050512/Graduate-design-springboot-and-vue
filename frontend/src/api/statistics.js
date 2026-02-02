import request from '@/utils/request'

/**
 * 获取Dashboard概览统计数据
 */
export function getOverview() {
  return request({
    url: '/statistics/overview',
    method: 'get'
  })
}

/**
 * 获取近6个月用水量趋势数据
 */
export function getUsageTrend() {
  return request({
    url: '/statistics/usage-trend',
    method: 'get'
  })
}

/**
 * 获取用户类型分布数据
 */
export function getUserDistribution() {
  return request({
    url: '/statistics/user-distribution',
    method: 'get'
  })
}

/**
 * 获取水表类型分布数据
 */
export function getMeterDistribution() {
  return request({
    url: '/statistics/meter-distribution',
    method: 'get'
  })
}

/**
 * 获取缴费状态统计
 */
export function getPaymentStatus() {
  return request({
    url: '/statistics/payment-status',
    method: 'get'
  })
}
