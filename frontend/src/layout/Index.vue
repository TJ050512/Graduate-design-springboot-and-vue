<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h3>水务管理系统</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><component :is="icons.HomeFilled" /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/user">
          <el-icon><component :is="icons.User" /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/waterMeter">
          <el-icon><component :is="icons.Monitor" /></el-icon>
          <span>水表管理</span>
        </el-menu-item>
        <el-menu-item index="/waterUsage">
          <el-icon><component :is="icons.Document" /></el-icon>
          <span>用水记录</span>
        </el-menu-item>
        <el-menu-item index="/payment">
          <el-icon><component :is="icons.Money" /></el-icon>
          <span>缴费管理</span>
        </el-menu-item>
        <el-menu-item index="/notice">
          <el-icon><component :is="icons.Bell" /></el-icon>
          <span>公告管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><component :is="icons.User" /></el-icon>
              {{ userStore.userInfo?.realName || userStore.userInfo?.username || '用户' }}
              <el-icon class="el-icon--right"><component :is="icons.ArrowDown" /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  HomeFilled,
  User,
  Monitor,
  Document,
  Money,
  Bell,
  ArrowDown
} from '@element-plus/icons-vue'

const icons = {
  HomeFilled,
  User,
  Monitor,
  Document,
  Money,
  Bell,
  ArrowDown
}

const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '首页')

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    border-bottom: 1px solid #434a55;
    
    h3 {
      font-size: 18px;
      font-weight: 500;
    }
  }
  
  .sidebar-menu {
    border: none;
    height: calc(100vh - 60px);
    overflow-y: auto;
  }
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: white;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
  
  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      color: #606266;
      
      .el-icon {
        margin-right: 5px;
      }
    }
  }
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>

