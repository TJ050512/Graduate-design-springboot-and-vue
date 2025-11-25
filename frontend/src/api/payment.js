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
export function pay(id) {
  return request({
    url: `/payment/pay/${id}`,
    method: 'put'
  })
}

