import request from '@/utils/request'

// 用户登录
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

// 获取当前用户信息
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 分页查询用户列表
export function getUserPage(params) {
  return request({
    url: '/user/page',
    method: 'get',
    params
  })
}

// 根据ID查询用户
export function getUserById(id) {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

// 添加用户
export function addUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(data) {
  return request({
    url: '/user',
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/user/changePassword',
    method: 'put',
    data
  })
}

// 重置密码
export function resetPassword(id) {
  return request({
    url: `/user/resetPassword/${id}`,
    method: 'put'
  })
}

