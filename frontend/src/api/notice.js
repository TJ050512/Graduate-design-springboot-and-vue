import request from '@/utils/request'

// 分页查询公告列表
export function getNoticePage(params) {
  return request({
    url: '/notice/page',
    method: 'get',
    params
  })
}

// 根据ID查询公告
export function getNoticeById(id) {
  return request({
    url: `/notice/${id}`,
    method: 'get'
  })
}

// 添加公告
export function addNotice(data) {
  return request({
    url: '/notice',
    method: 'post',
    data
  })
}

// 更新公告
export function updateNotice(data) {
  return request({
    url: '/notice',
    method: 'put',
    data
  })
}

// 发布公告
export function publishNotice(id) {
  return request({
    url: `/notice/publish/${id}`,
    method: 'put'
  })
}

// 删除公告
export function deleteNotice(id) {
  return request({
    url: `/notice/${id}`,
    method: 'delete'
  })
}

