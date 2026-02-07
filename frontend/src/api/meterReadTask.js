import request from '@/utils/request'

// 获取需要抄表的水表列表（管理员用）
export function getNeedReadMeters() {
  return request({
    url: '/meterReadTask/needRead',
    method: 'get'
  })
}

// 通知抄表员抄表（指定水表）
export function notifyMeterRead(meterIds) {
  return request({
    url: '/meterReadTask/notify',
    method: 'post',
    data: meterIds
  })
}

// 批量通知所有需要抄表的水表
export function notifyAllMeterRead() {
  return request({
    url: '/meterReadTask/notifyAll',
    method: 'post'
  })
}

// 获取抄表员的待处理任务列表
export function getPendingTasks() {
  return request({
    url: '/meterReadTask/pending',
    method: 'get'
  })
}

// 获取抄表员的待处理任务数量
export function getPendingTaskCount() {
  return request({
    url: '/meterReadTask/pending/count',
    method: 'get'
  })
}

// 完成抄表任务
export function completeTask(taskId, usageId) {
  return request({
    url: `/meterReadTask/complete/${taskId}`,
    method: 'put',
    params: usageId ? { usageId } : {}
  })
}
