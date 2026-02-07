import request from '@/utils/request'

// 分页查询缴费记录列表
export function getPaymentPage(params) {
  return request({
    url: '/payment/page',
    method: 'get',
    params
  })
}

// 根据ID查询缴费记录
export function getPaymentById(id) {
  return request({
    url: `/payment/${id}`,
    method: 'get'
  })
}

// 创建缴费记录
export function createPayment(data) {
  return request({
    url: '/payment',
    method: 'post',
    data
  })
}

// 支付
export function pay(id, paymentMethod) {
  return request({
    url: `/payment/pay/${id}`,
    method: 'put',
    params: paymentMethod ? { paymentMethod } : {}
  })
}

// 获取用户缴费统计
export function getPaymentStatistics(userId) {
  return request({
    url: '/payment/statistics',
    method: 'get',
    params: { userId }
  })
}

// 发送单个催缴提醒
export function sendReminder(paymentId) {
  return request({
    url: `/payment/remind/${paymentId}`,
    method: 'post'
  })
}

// 批量发送催缴提醒
export function batchSendReminder(paymentIds) {
  return request({
    url: '/payment/remind/batch',
    method: 'post',
    data: paymentIds
  })
}

// 获取当前用户的未缴费提醒（用户登录时调用）
export function getUnpaidReminders() {
  return request({
    url: '/payment/unpaid/reminders',
    method: 'get'
  })
}

