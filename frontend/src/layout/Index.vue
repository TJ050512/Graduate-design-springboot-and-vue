<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <!-- Logo区域 -->
      <div class="logo" :class="{ 'logo-collapse': isCollapse }">
        <div class="logo-icon">
          <svg viewBox="0 0 100 120" fill="none" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="sidebarDropGradient" x1="50%" y1="0%" x2="50%" y2="100%">
                <stop offset="0%" stop-color="#4facfe"/>
                <stop offset="100%" stop-color="#00f2fe"/>
              </linearGradient>
            </defs>
            <path d="M50 8 C50 8, 85 50, 85 75 C85 95, 70 110, 50 110 C30 110, 15 95, 15 75 C15 50, 50 8, 50 8Z" 
                  fill="url(#sidebarDropGradient)"/>
            <g stroke="rgba(255,255,255,0.7)" stroke-width="2" fill="none" stroke-linecap="round">
              <path d="M35 55 L35 75 L45 85"/>
              <path d="M45 45 L45 60 L55 70 L55 90"/>
              <circle cx="35" cy="55" r="3" fill="rgba(255,255,255,0.9)"/>
              <circle cx="45" cy="45" r="3" fill="rgba(255,255,255,0.9)"/>
              <circle cx="55" cy="90" r="3" fill="rgba(255,255,255,0.9)"/>
            </g>
          </svg>
        </div>
        <transition name="fade">
          <div v-if="!isCollapse" class="logo-text">
            <h3>水务管理系统</h3>
            <span>Water Management</span>
          </div>
        </transition>
      </div>
      
      <!-- 导航菜单 -->
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="sidebar-menu"
      >
        <!-- 所有用户都能看到首页 -->
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页概览</template>
        </el-menu-item>
        
        <!-- 仅管理员可见 -->
        <el-menu-item index="/user" v-if="isAdmin">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        
        <!-- 管理员和抄表员可见 -->
        <el-menu-item index="/waterMeter" v-if="isAdmin || isReader">
          <el-icon><Monitor /></el-icon>
          <template #title>水表管理</template>
        </el-menu-item>
        
        <!-- 管理员和抄表员可见：用水记录管理 -->
        <el-menu-item index="/waterUsage" v-if="isAdmin || isReader">
          <el-icon><Document /></el-icon>
          <template #title>用水记录</template>
        </el-menu-item>
        
        <!-- 普通用户可见：我的用水 -->
        <el-menu-item index="/myUsage" v-if="isNormalUser">
          <el-icon><Document /></el-icon>
          <template #title>我的用水</template>
        </el-menu-item>
        
        <!-- 管理员可见：缴费管理 -->
        <el-menu-item index="/payment" v-if="isAdmin">
          <el-icon><Money /></el-icon>
          <template #title>缴费管理</template>
        </el-menu-item>
        
        <!-- 普通用户可见：我的缴费 -->
        <el-menu-item index="/myPayment" v-if="isNormalUser">
          <el-icon><Money /></el-icon>
          <template #title>我的缴费</template>
        </el-menu-item>
        
        <!-- 管理员和维修人员可见：报修工单管理 -->
        <el-menu-item index="/repair" v-if="isAdmin || isRepairman">
          <el-icon><Tools /></el-icon>
          <template #title>报修工单</template>
        </el-menu-item>
        
        <!-- 普通用户可见：我的报修 -->
        <el-menu-item index="/myRepair" v-if="isNormalUser">
          <el-icon><Tools /></el-icon>
          <template #title>我的报修</template>
        </el-menu-item>
        
        <!-- 管理员可见：公告管理 -->
        <el-menu-item index="/notice" v-if="isAdmin">
          <el-icon><Bell /></el-icon>
          <template #title>公告管理</template>
        </el-menu-item>
        
        <!-- 普通用户、抄表员和维修人员可见：公告列表 -->
        <el-menu-item index="/noticeList" v-if="isNormalUser || isReader || isRepairman">
          <el-icon><Bell /></el-icon>
          <template #title>系统公告</template>
        </el-menu-item>
      </el-menu>
      
      <!-- 底部折叠按钮 -->
      <div class="sidebar-footer">
        <div class="collapse-btn" @click="toggleCollapse">
          <el-icon :class="{ 'is-rotate': isCollapse }">
            <Fold />
          </el-icon>
        </div>
      </div>
    </el-aside>
    
    <el-container class="main-container">
      <!-- 头部 -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">
              <el-icon><HomeFilled /></el-icon>
              首页
            </el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle !== '首页概览'">
              {{ currentTitle }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 全屏按钮 -->
          <div class="header-action" @click="toggleFullscreen" title="全屏">
            <el-icon><FullScreen /></el-icon>
          </div>
          
          <!-- 用户信息 -->
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-dropdown">
              <div class="user-avatar">
                <el-icon><User /></el-icon>
              </div>
              <div class="user-info">
                <span class="user-name">{{ userStore.userInfo?.realName || userStore.userInfo?.username || '用户' }}</span>
                <span class="user-role">{{ getUserRoleText() }}</span>
              </div>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 主内容区 -->
      <el-main class="main-content">
        <transition name="page-fade" mode="out-in">
          <router-view />
        </transition>
      </el-main>
      
      <!-- 底部信息 -->
      <el-footer class="footer">
        <span>© 2024 水务管理系统 - Water Management System</span>
      </el-footer>
    </el-container>
    
    <!-- 个人信息对话框 -->
    <el-dialog
      v-model="profileDialogVisible"
      title="个人信息"
      width="500px"
    >
      <div class="profile-content">
        <div class="profile-avatar">
          <el-icon><User /></el-icon>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名">
            {{ userStore.userInfo?.username || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="真实姓名">
            {{ userStore.userInfo?.realName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ userStore.userInfo?.phone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="用户类型">
            <el-tag :type="getUserTypeTag()">{{ getUserRoleText() }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <el-tag :type="userStore.userInfo?.status === 1 ? 'success' : 'danger'">
              {{ userStore.userInfo?.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="profileDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUnpaidReminders } from '@/api/payment'
import { getPendingTaskCount } from '@/api/meterReadTask'
import { ElMessageBox } from 'element-plus'
import {
  HomeFilled,
  User,
  Monitor,
  Document,
  Money,
  Bell,
  ArrowDown,
  Fold,
  FullScreen,
  SwitchButton,
  Tools
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)
const profileDialogVisible = ref(false)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '首页概览')

// 角色判断
const userType = computed(() => userStore.userInfo?.userType)
const isAdmin = computed(() => userType.value === 1)        // 管理员
const isNormalUser = computed(() => userType.value === 2)   // 普通用户
const isReader = computed(() => userType.value === 3)       // 抄表员

// 检查未缴费提醒（仅普通用户）
const checkUnpaidReminders = async () => {
  if (!isNormalUser.value) return
  
  try {
    const res = await getUnpaidReminders()
    if (res.code === 200 && res.data.hasReminder) {
      const { count, totalAmount } = res.data
      
      ElMessageBox.confirm(
        `<div style="text-align: center;">
          <div style="font-size: 48px; margin-bottom: 16px;">💧</div>
          <div style="font-size: 18px; font-weight: bold; margin-bottom: 12px; color: #f56c6c;">您有 ${count} 笔水费待缴纳</div>
          <div style="font-size: 24px; font-weight: bold; color: #f56c6c; margin-bottom: 16px;">¥${totalAmount}</div>
          <div style="color: #909399; font-size: 14px;">为避免影响您的正常用水，请尽快完成缴费</div>
        </div>`,
        '缴费提醒',
        {
          confirmButtonText: '立即缴费',
          cancelButtonText: '稍后处理',
          dangerouslyUseHTMLString: true,
          type: 'warning',
          center: true
        }
      ).then(() => {
        router.push('/myPayment')
      }).catch(() => {})
    }
  } catch (error) {
    console.error('获取未缴费提醒失败', error)
  }
}

// 检查抄表任务提醒（仅抄表员）
const checkMeterReadTasks = async () => {
  if (!isReader.value) return
  
  try {
    const res = await getPendingTaskCount()
    if (res.code === 200 && res.data > 0) {
      const count = res.data
      
      ElMessageBox.confirm(
        `<div style="text-align: center;">
          <div style="font-size: 48px; margin-bottom: 16px;">📋</div>
          <div style="font-size: 18px; font-weight: bold; margin-bottom: 12px; color: #e6a23c;">您有 ${count} 个抄表任务待处理</div>
          <div style="color: #909399; font-size: 14px;">管理员已通知您对相关水表进行抄表，请及时处理</div>
        </div>`,
        '抄表任务提醒',
        {
          confirmButtonText: '查看任务',
          cancelButtonText: '稍后处理',
          dangerouslyUseHTMLString: true,
          type: 'warning',
          center: true
        }
      ).then(() => {
        router.push('/waterUsage')
      }).catch(() => {})
    }
  } catch (error) {
    console.error('获取抄表任务提醒失败', error)
  }
}

// 页面加载时检查提醒
onMounted(() => {
  // 延迟1秒检查，避免页面刚加载就弹窗
  setTimeout(() => {
    checkUnpaidReminders()
    checkMeterReadTasks()
  }, 1000)
})
const isRepairman = computed(() => userType.value === 4)    // 维修人员

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const getUserRoleText = () => {
  const userType = userStore.userInfo?.userType
  switch (userType) {
    case 1: return '管理员'
    case 2: return '普通用户'
    case 3: return '抄表员'
    case 4: return '维修人员'
    default: return '用户'
  }
}

const getUserTypeTag = () => {
  const userType = userStore.userInfo?.userType
  switch (userType) {
    case 1: return 'danger'
    case 2: return 'success'
    case 3: return 'warning'
    case 4: return 'primary'
    default: return 'info'
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
  } else if (command === 'profile') {
    profileDialogVisible.value = true
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  background-color: var(--bg-color);
}

// ==========================================
// 侧边栏样式
// ==========================================
.sidebar {
  background: linear-gradient(180deg, #1e3c72 0%, #2a5298 50%, #1e3c72 100%);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  overflow: hidden;
  position: relative;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  
  // 装饰性波浪背景
  &::before {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 200px;
    background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='rgba(255,255,255,0.03)' d='M0,224L48,213.3C96,203,192,181,288,181.3C384,181,480,203,576,224C672,245,768,267,864,250.7C960,235,1056,181,1152,165.3C1248,149,1344,171,1392,181.3L1440,192L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E") no-repeat bottom;
    background-size: cover;
    pointer-events: none;
  }
}

.logo {
  height: 70px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 1;
  
  &.logo-collapse {
    justify-content: center;
    padding: 0;
  }
  
  .logo-icon {
    width: 36px;
    height: 44px;
    min-width: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    filter: drop-shadow(0 2px 6px rgba(79, 172, 254, 0.5));
    
    svg {
      width: 100%;
      height: 100%;
    }
  }
  
  .logo-text {
    overflow: hidden;
    white-space: nowrap;
    
    h3 {
      font-size: 16px;
      font-weight: 600;
      color: white;
      margin-bottom: 2px;
    }
    
    span {
      font-size: 11px;
      color: rgba(255, 255, 255, 0.6);
      letter-spacing: 0.5px;
    }
  }
}

.sidebar-menu {
  flex: 1;
  border: none;
  background: transparent;
  padding: 12px 0;
  overflow-y: auto;
  position: relative;
  z-index: 1;
  
  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 2px;
  }
  
  :deep(.el-menu-item) {
    height: 48px;
    line-height: 48px;
    margin: 4px 12px;
    border-radius: 8px;
    color: rgba(255, 255, 255, 0.75);
    transition: all 0.3s ease;
    
    .el-icon {
      font-size: 18px;
      margin-right: 10px;
    }
    
    &:hover {
      background: rgba(255, 255, 255, 0.1);
      color: white;
    }
    
    &.is-active {
      background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
      color: white;
      box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 4px;
        height: 24px;
        background: white;
        border-radius: 0 4px 4px 0;
      }
    }
  }
  
  // 折叠状态
  &.el-menu--collapse {
    :deep(.el-menu-item) {
      margin: 4px 8px;
      padding: 0 !important;
      justify-content: center;
      
      .el-icon {
        margin-right: 0;
      }
      
      &.is-active::before {
        display: none;
      }
    }
  }
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 1;
}

.collapse-btn {
  width: 100%;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.75);
  transition: all 0.3s ease;
  
  &:hover {
    background: rgba(255, 255, 255, 0.2);
    color: white;
  }
  
  .el-icon {
    font-size: 18px;
    transition: transform 0.3s ease;
    
    &.is-rotate {
      transform: rotate(180deg);
    }
  }
}

// ==========================================
// 主容器样式
// ==========================================
.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// ==========================================
// 头部样式
// ==========================================
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  background: white;
  border-bottom: 1px solid var(--border-color-light);
  padding: 0 24px;
  box-shadow: var(--shadow-sm);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  
  .el-breadcrumb {
    :deep(.el-breadcrumb__inner) {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-action {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.3s ease;
  
  &:hover {
    background: var(--primary-bg);
    color: var(--primary-color);
  }
  
  .el-icon {
    font-size: 18px;
  }
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    background: var(--bg-color);
  }
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  
  .el-icon {
    font-size: 18px;
  }
}

.user-info {
  display: flex;
  flex-direction: column;
  
  .user-name {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
    line-height: 1.4;
  }
  
  .user-role {
    font-size: 12px;
    color: var(--text-secondary);
    line-height: 1.4;
  }
}

.dropdown-icon {
  color: var(--text-secondary);
  transition: transform 0.3s ease;
}

// ==========================================
// 主内容区样式
// ==========================================
.main-content {
  flex: 1;
  background-color: var(--bg-color);
  padding: 20px;
  overflow-y: auto;
}

// ==========================================
// 底部样式
// ==========================================
.footer {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-top: 1px solid var(--border-color-light);
  color: var(--text-secondary);
  font-size: 12px;
}

// ==========================================
// 过渡动画
// ==========================================
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.page-fade-enter-active {
  animation: fadeIn 0.3s ease;
}

.page-fade-leave-active {
  animation: fadeIn 0.2s ease reverse;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ==========================================
// 个人信息对话框
// ==========================================
.profile-content {
  .profile-avatar {
    width: 80px;
    height: 80px;
    margin: 0 auto 24px;
    border-radius: 50%;
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 36px;
    box-shadow: 0 8px 24px rgba(24, 144, 255, 0.3);
  }
}
</style>
