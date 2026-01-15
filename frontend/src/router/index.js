import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/Index.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页概览', icon: 'HomeFilled' }
      },
      // ========== 管理员专属 ==========
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/Index.vue'),
        meta: { title: '用户管理', icon: 'User', roles: [1] }
      },
      {
        path: 'payment',
        name: 'Payment',
        component: () => import('@/views/payment/Index.vue'),
        meta: { title: '缴费管理', icon: 'Money', roles: [1] }
      },
      {
        path: 'notice',
        name: 'Notice',
        component: () => import('@/views/notice/Index.vue'),
        meta: { title: '公告管理', icon: 'Bell', roles: [1] }
      },
      // ========== 管理员 + 抄表员 ==========
      {
        path: 'waterMeter',
        name: 'WaterMeter',
        component: () => import('@/views/waterMeter/Index.vue'),
        meta: { title: '水表管理', icon: 'Monitor', roles: [1, 3] }
      },
      {
        path: 'waterUsage',
        name: 'WaterUsage',
        component: () => import('@/views/waterUsage/Index.vue'),
        meta: { title: '用水记录', icon: 'Document', roles: [1, 3] }
      },
      // ========== 普通用户专属 ==========
      {
        path: 'myUsage',
        name: 'MyUsage',
        component: () => import('@/views/myUsage/Index.vue'),
        meta: { title: '我的用水', icon: 'Document', roles: [2] }
      },
      {
        path: 'myPayment',
        name: 'MyPayment',
        component: () => import('@/views/myPayment/Index.vue'),
        meta: { title: '我的缴费', icon: 'Money', roles: [2] }
      },
      // ========== 普通用户 + 抄表员 ==========
      {
        path: 'noticeList',
        name: 'NoticeList',
        component: () => import('@/views/noticeList/Index.vue'),
        meta: { title: '系统公告', icon: 'Bell', roles: [2, 3] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  NProgress.start()
  const userStore = useUserStore()
  
  // 未登录，跳转到登录页
  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
    return
  }
  
  // 已登录，访问登录页则跳转到首页
  if (to.path === '/login' && userStore.token) {
    next('/')
    return
  }
  
  // 检查角色权限
  if (to.meta.roles && userStore.userInfo) {
    const userType = userStore.userInfo.userType
    if (!to.meta.roles.includes(userType)) {
      // 无权限，跳转到首页
      next('/dashboard')
      return
    }
  }
  
  next()
})

router.afterEach((to) => {
  NProgress.done()
  document.title = to.meta.title ? `${to.meta.title} - 水务管理系统` : '水务管理系统'
})

export default router

