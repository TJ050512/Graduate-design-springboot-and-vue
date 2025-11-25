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

