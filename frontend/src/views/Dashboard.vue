<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>👋 欢迎回来，{{ userName }}！</h2>
          <p>{{ currentDate }}，{{ greeting }}</p>
        </div>
        <div class="welcome-illustration">
          <svg viewBox="0 0 200 120" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="water-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#40a9ff;stop-opacity:0.3" />
                <stop offset="100%" style="stop-color:#1890ff;stop-opacity:0.5" />
              </linearGradient>
            </defs>
            <ellipse cx="100" cy="100" rx="80" ry="15" fill="url(#water-gradient)" opacity="0.5"/>
            <path d="M60,50 Q100,10 140,50 Q140,90 100,100 Q60,90 60,50" fill="url(#water-gradient)" stroke="#1890ff" stroke-width="2"/>
            <circle cx="80" cy="45" r="8" fill="rgba(255,255,255,0.6)"/>
            <circle cx="70" cy="55" r="4" fill="rgba(255,255,255,0.4)"/>
          </svg>
        </div>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="12" :lg="6" v-for="(stat, index) in statistics" :key="index">
        <div class="stat-card" :style="{ animationDelay: `${index * 0.1}s` }">
          <div class="stat-icon" :style="{ background: stat.gradient }">
            <el-icon><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">
              <span class="number">{{ animatedValues[index] }}</span>
              <span class="unit" v-if="stat.unit">{{ stat.unit }}</span>
            </div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
          <div class="stat-trend" :class="stat.trendType" v-if="isAdmin && stat.trend !== 0">
            <el-icon v-if="stat.trend > 0"><Top /></el-icon>
            <el-icon v-else><Bottom /></el-icon>
            <span>{{ Math.abs(stat.trend) }}%</span>
          </div>
          <div class="stat-decoration" :style="{ background: stat.decorationColor }"></div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 快捷操作 -->
    <el-row :gutter="20" class="action-row">
      <el-col :span="24">
        <div class="quick-actions">
          <h3 class="section-title">
            <el-icon><Lightning /></el-icon>
            快捷操作
          </h3>
          <div class="action-grid">
            <div class="action-item" v-for="action in quickActions" :key="action.title" @click="handleAction(action.path)">
              <div class="action-icon" :style="{ background: action.gradient }">
                <el-icon><component :is="action.icon" /></el-icon>
              </div>
              <span class="action-title">{{ action.title }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 内容区域 -->
    <el-row :gutter="20" class="content-row">
      <!-- 最近公告 -->
      <el-col :xs="24" :lg="12">
        <el-card class="content-card notice-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon class="header-icon"><Bell /></el-icon>
                <span>最新公告</span>
              </div>
              <el-button type="primary" link @click="$router.push(isAdmin ? '/notice' : '/noticeList')">
                查看更多 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div class="notice-list">
            <div class="notice-item" v-for="notice in recentNotices" :key="notice.id">
              <div class="notice-tag">
                <el-tag :type="getNoticeType(notice.type)" size="small">{{ getNoticeTypeName(notice.type) }}</el-tag>
              </div>
              <div class="notice-content">
                <h4>{{ notice.title }}</h4>
                <p>{{ notice.date }}</p>
              </div>
            </div>
            <el-empty v-if="recentNotices.length === 0" description="暂无公告" :image-size="80" />
          </div>
        </el-card>
      </el-col>
      
      <!-- 系统信息 -->
      <el-col :xs="24" :lg="12">
        <el-card class="content-card system-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon class="header-icon"><InfoFilled /></el-icon>
                <span>系统信息</span>
              </div>
            </div>
          </template>
          <div class="system-info">
            <div class="info-item" v-for="info in systemInfo" :key="info.label">
              <span class="info-label">{{ info.label }}</span>
              <span class="info-value">{{ info.value }}</span>
            </div>
          </div>
          
          <div class="feature-list">
            <h4>系统功能</h4>
            <div class="feature-grid">
              <div class="feature-item" v-for="feature in features" :key="feature">
                <el-icon><CircleCheck /></el-icon>
                <span>{{ feature }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  User,
  Monitor,
  Document,
  Money,
  Bell,
  ArrowRight,
  Top,
  Bottom,
  InfoFilled,
  CircleCheck,
  Plus,
  Search,
  DataAnalysis,
  Promotion,
  Lightning
} from '@element-plus/icons-vue'
import { getUserPage } from '@/api/user'
import { getMeterPage } from '@/api/waterMeter'
import { getUsagePage } from '@/api/waterUsage'
import { getPaymentPage } from '@/api/payment'
import { getNoticePage } from '@/api/notice'

const router = useRouter()
const userStore = useUserStore()

// 角色判断
const userType = computed(() => userStore.userInfo?.userType)
const isAdmin = computed(() => userType.value === 1)
const isNormalUser = computed(() => userType.value === 2)
const isReader = computed(() => userType.value === 3)

const userName = computed(() => userStore.userInfo?.realName || userStore.userInfo?.username || '用户')

const currentDate = computed(() => {
  const now = new Date()
  const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
  return now.toLocaleDateString('zh-CN', options)
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了，注意休息！'
  if (hour < 9) return '早上好，新的一天开始了！'
  if (hour < 12) return '上午好，工作顺利！'
  if (hour < 14) return '中午好，记得午休！'
  if (hour < 18) return '下午好，继续加油！'
  if (hour < 22) return '晚上好，辛苦了！'
  return '夜深了，早点休息！'
})

const statistics = ref([
  {
    icon: markRaw(User),
    label: '用户总数',
    value: 0,
    unit: '人',
    trend: 0,
    trendType: 'up',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    decorationColor: 'rgba(102, 126, 234, 0.1)'
  },
  {
    icon: markRaw(Monitor),
    label: '水表总数',
    value: 0,
    unit: '台',
    trend: 0,
    trendType: 'up',
    gradient: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
    decorationColor: 'rgba(17, 153, 142, 0.1)'
  },
  {
    icon: markRaw(Document),
    label: '用水记录',
    value: 0,
    unit: '条',
    trend: 0,
    trendType: 'up',
    gradient: 'linear-gradient(135deg, #ee9ca7 0%, #ffdde1 100%)',
    decorationColor: 'rgba(238, 156, 167, 0.1)'
  },
  {
    icon: markRaw(Money),
    label: '缴费记录',
    value: 0,
    unit: '条',
    trend: 0,
    trendType: 'up',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    decorationColor: 'rgba(240, 147, 251, 0.1)'
  }
])

// 数字动画
const animatedValues = ref([0, 0, 0, 0])

// 加载统计数据 - 根据角色加载不同数据
const loadStatistics = async () => {
  try {
    const userId = userStore.userInfo?.userId
    
    if (isAdmin.value) {
      // 管理员：加载全部数据统计
      const [userRes, meterRes, usageRes, paymentRes] = await Promise.all([
        getUserPage({ page: 1, size: 1 }),
        getMeterPage({ page: 1, size: 1 }),
        getUsagePage({ page: 1, size: 1 }),
        getPaymentPage({ page: 1, size: 1 })
      ])
      
      if (userRes.code === 200) statistics.value[0].value = userRes.data.total || 0
      if (meterRes.code === 200) statistics.value[1].value = meterRes.data.total || 0
      if (usageRes.code === 200) statistics.value[2].value = usageRes.data.total || 0
      if (paymentRes.code === 200) statistics.value[3].value = paymentRes.data.total || 0
    } else if (isNormalUser.value) {
      // 普通用户：只加载自己的数据
      const [usageRes, paymentRes] = await Promise.all([
        getUsagePage({ page: 1, size: 1, userId }),
        getPaymentPage({ page: 1, size: 1, userId })
      ])
      
      // 更新为用户专属统计
      statistics.value = [
        { ...statistics.value[2], label: '用水记录', value: usageRes.data?.total || 0 },
        { ...statistics.value[3], label: '缴费记录', value: paymentRes.data?.total || 0 }
      ]
    } else if (isReader.value) {
      // 抄表员：加载水表和用水记录
      const [meterRes, usageRes] = await Promise.all([
        getMeterPage({ page: 1, size: 1 }),
        getUsagePage({ page: 1, size: 1 })
      ])
      
      statistics.value = [
        { ...statistics.value[1], label: '水表总数', value: meterRes.data?.total || 0 },
        { ...statistics.value[2], label: '用水记录', value: usageRes.data?.total || 0 }
      ]
    }
    
    // 更新动画数组长度并启动数字动画
    animatedValues.value = new Array(statistics.value.length).fill(0)
    statistics.value.forEach((stat, index) => {
      animateNumber(index, stat.value)
    })
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

// 加载最新公告
const loadRecentNotices = async () => {
  try {
    const res = await getNoticePage({ page: 1, size: 5, status: 1 })
    if (res.code === 200 && res.data.records) {
      recentNotices.value = res.data.records.map(item => ({
        id: item.noticeId,
        title: item.title,
        type: item.noticeType,
        date: item.createTime?.split(' ')[0] || ''
      }))
    }
  } catch (error) {
    console.error('加载公告失败', error)
  }
}

onMounted(() => {
  loadStatistics()
  loadRecentNotices()
})

const animateNumber = (index, target) => {
  const duration = 1500
  const steps = 60
  const stepValue = target / steps
  let current = 0
  const timer = setInterval(() => {
    current += stepValue
    if (current >= target) {
      animatedValues.value[index] = target
      clearInterval(timer)
    } else {
      animatedValues.value[index] = Math.floor(current)
    }
  }, duration / steps)
}

// 根据角色显示不同的快捷操作
const quickActions = computed(() => {
  // 管理员的快捷操作
  if (isAdmin.value) {
    return [
      { title: '添加用户', icon: markRaw(Plus), path: '/user', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
      { title: '水表管理', icon: markRaw(Monitor), path: '/waterMeter', gradient: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)' },
      { title: '用水记录', icon: markRaw(Search), path: '/waterUsage', gradient: 'linear-gradient(135deg, #ee9ca7 0%, #ffdde1 100%)' },
      { title: '缴费管理', icon: markRaw(Money), path: '/payment', gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
      { title: '发布公告', icon: markRaw(Promotion), path: '/notice', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
      { title: '数据统计', icon: markRaw(DataAnalysis), path: '/dashboard', gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' }
    ]
  }
  // 普通用户的快捷操作
  if (isNormalUser.value) {
    return [
      { title: '我的用水', icon: markRaw(Document), path: '/myUsage', gradient: 'linear-gradient(135deg, #ee9ca7 0%, #ffdde1 100%)' },
      { title: '我的缴费', icon: markRaw(Money), path: '/myPayment', gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
      { title: '系统公告', icon: markRaw(Bell), path: '/noticeList', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' }
    ]
  }
  // 抄表员的快捷操作
  if (isReader.value) {
    return [
      { title: '水表管理', icon: markRaw(Monitor), path: '/waterMeter', gradient: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)' },
      { title: '录入抄表', icon: markRaw(Document), path: '/waterUsage', gradient: 'linear-gradient(135deg, #ee9ca7 0%, #ffdde1 100%)' },
      { title: '系统公告', icon: markRaw(Bell), path: '/noticeList', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' }
    ]
  }
  return []
})

const handleAction = (path) => {
  router.push(path)
}

const recentNotices = ref([])

const getNoticeType = (type) => {
  const types = { 1: 'danger', 2: 'warning', 3: 'info' }
  return types[type] || 'info'
}

const getNoticeTypeName = (type) => {
  const names = { 1: '重要', 2: '通知', 3: '公告' }
  return names[type] || '公告'
}

const systemInfo = ref([
  { label: '系统版本', value: 'v1.0.0' },
  { label: '技术栈', value: 'Vue 3 + Spring Boot' },
  { label: '数据库', value: 'MySQL 8.0' },
  { label: '运行状态', value: '正常运行中' }
])

// 根据角色显示不同的功能列表
const features = computed(() => {
  if (isAdmin.value) {
    return ['用户管理', '水表管理', '用水记录', '缴费管理', '公告管理', '数据统计']
  }
  if (isNormalUser.value) {
    return ['查看用水', '在线缴费', '查看公告', '个人信息']
  }
  if (isReader.value) {
    return ['水表管理', '录入抄表', '查看公告', '个人信息']
  }
  return ['用户管理', '水表管理', '用水记录', '缴费管理', '公告管理', '数据统计']
})
</script>

<style scoped lang="scss">
.dashboard {
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ==========================================
// 欢迎区域
// ==========================================
.welcome-section {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  border-radius: 16px;
  padding: 28px 32px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -20%;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
    pointer-events: none;
  }
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.welcome-text {
  h2 {
    font-size: 24px;
    color: white;
    margin-bottom: 8px;
    font-weight: 600;
  }
  
  p {
    color: rgba(255, 255, 255, 0.8);
    font-size: 14px;
  }
}

.welcome-illustration {
  width: 200px;
  height: 120px;
  
  svg {
    width: 100%;
    height: 100%;
    animation: float 3s ease-in-out infinite;
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

// ==========================================
// 统计卡片
// ==========================================
.stat-row {
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  animation: slideUp 0.5s ease backwards;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 4px;
  
  .number {
    font-size: 28px;
    font-weight: 700;
    color: var(--text-primary);
    font-family: 'DIN Alternate', 'Roboto', sans-serif;
  }
  
  .unit {
    font-size: 14px;
    color: var(--text-secondary);
  }
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 6px;
  
  &.up {
    color: #52c41a;
    background: rgba(82, 196, 26, 0.1);
  }
  
  &.down {
    color: #ff4d4f;
    background: rgba(255, 77, 79, 0.1);
  }
}

.stat-decoration {
  position: absolute;
  right: -20px;
  bottom: -20px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  opacity: 0.5;
}

// ==========================================
// 快捷操作
// ==========================================
.quick-actions {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
  
  .el-icon {
    color: #faad14;
  }
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  border-radius: 12px;
  background: var(--bg-color);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    background: white;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
    
    .action-icon {
      transform: scale(1.1);
    }
  }
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 22px;
  transition: transform 0.3s ease;
}

.action-title {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.action-row {
  margin-bottom: 24px;
}

// ==========================================
// 内容卡片
// ==========================================
.content-row {
  .content-card {
    height: 100%;
    
    :deep(.el-card__header) {
      padding: 16px 20px;
    }
    
    :deep(.el-card__body) {
      padding: 20px;
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: var(--text-primary);
    
    .header-icon {
      font-size: 18px;
      color: var(--primary-color);
    }
  }
}

// 公告列表
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: var(--bg-color);
  transition: all 0.3s ease;
  
  &:hover {
    background: var(--primary-bg);
  }
}

.notice-content {
  flex: 1;
  min-width: 0;
  
  h4 {
    font-size: 14px;
    color: var(--text-primary);
    margin-bottom: 4px;
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  p {
    font-size: 12px;
    color: var(--text-secondary);
  }
}

// 系统信息
.system-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-color-light);
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  
  .info-label {
    font-size: 12px;
    color: var(--text-secondary);
  }
  
  .info-value {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-primary);
  }
}

.feature-list {
  h4 {
    font-size: 14px;
    color: var(--text-primary);
    margin-bottom: 12px;
    font-weight: 600;
  }
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-regular);
  
  .el-icon {
    color: #52c41a;
    font-size: 16px;
  }
}

// ==========================================
// 响应式
// ==========================================
@media (max-width: 768px) {
  .welcome-content {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }
  
  .welcome-illustration {
    display: none;
  }
  
  .stat-card {
    margin-bottom: 12px;
  }
  
  .system-info {
    grid-template-columns: 1fr;
  }
  
  .feature-grid {
    grid-template-columns: 1fr;
  }
}
</style>
