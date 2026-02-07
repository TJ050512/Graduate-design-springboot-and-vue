import request from '@/utils/request'

// 分页查询报修工单列表
export function getRepairOrderPage(params) {
  return request({
    url: '/repair/page',
    method: 'get',
    params
  })
}

// 根据ID查询报修工单详情
export function getRepairOrderById(id) {
  return request({
    url: `/repair/${id}`,
    method: 'get'
  })
}

// 创建报修工单
export function createRepairOrder(data) {
  return request({
    url: '/repair',
    method: 'post',
    data
  })
}

// 更新报修工单
export function updateRepairOrder(data) {
  return request({
    url: '/repair',
    method: 'put',
    data
  })
}

// 删除报修工单
export function deleteRepairOrder(id) {
  return request({
    url: `/repair/${id}`,
    method: 'delete'
  })
}

// 开始处理工单
export function handleOrder(id) {
  return request({
    url: `/repair/handle/${id}`,
    method: 'put'
  })
}

// 完成工单
export function completeOrder(id, handleResult) {
  return request({
    url: `/repair/complete/${id}`,
    method: 'put',
    data: { handleResult }
  })
}

// 处理失败，转派工单
export function failOrder(id, failReason) {
  return request({
    url: `/repair/fail/${id}`,
    method: 'put',
    data: { failReason }
  })
}

// 取消工单
export function cancelOrder(id) {
  return request({
    url: `/repair/cancel/${id}`,
    method: 'put'
  })
}

// 用户反馈评价
export function feedbackOrder(id, feedback, rating) {
  return request({
    url: `/repair/feedback/${id}`,
    method: 'put',
    data: { feedback, rating }
  })
}
