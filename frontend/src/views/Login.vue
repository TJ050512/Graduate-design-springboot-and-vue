<template>
  <div class="login-container">
    <!-- 动态背景 -->
    <div class="bg-animation">
      <div class="wave wave1"></div>
      <div class="wave wave2"></div>
      <div class="wave wave3"></div>
    </div>
    
    <!-- 浮动水滴装饰 -->
    <div class="floating-drops">
      <div class="drop" v-for="n in 8" :key="n" :style="getDropStyle(n)"></div>
    </div>
    
    <!-- 登录卡片 -->
    <div class="login-card">
      <div class="card-header">
        <div class="logo">
          <div class="logo-icon">
            <svg viewBox="0 0 100 120" fill="none" xmlns="http://www.w3.org/2000/svg">
              <defs>
                <linearGradient id="dropGradient" x1="50%" y1="0%" x2="50%" y2="100%">
                  <stop offset="0%" stop-color="#4facfe"/>
                  <stop offset="100%" stop-color="#00f2fe"/>
                </linearGradient>
                <filter id="dropShadow" x="-20%" y="-20%" width="140%" height="140%">
                  <feDropShadow dx="0" dy="4" stdDeviation="8" flood-color="#4facfe" flood-opacity="0.4"/>
                </filter>
              </defs>
              <!-- 水滴形状 -->
              <path d="M50 8 C50 8, 85 50, 85 75 C85 95, 70 110, 50 110 C30 110, 15 95, 15 75 C15 50, 50 8, 50 8Z" 
                    fill="url(#dropGradient)" filter="url(#dropShadow)"/>
              <!-- 电路线条装饰 -->
              <g stroke="rgba(255,255,255,0.6)" stroke-width="1.5" fill="none" stroke-linecap="round">
                <path d="M35 55 L35 75 L45 85"/>
                <path d="M35 65 L25 65"/>
                <path d="M45 45 L45 60 L55 70 L55 90"/>
                <path d="M55 55 L65 55"/>
                <path d="M55 70 L70 70"/>
                <circle cx="35" cy="55" r="2.5" fill="rgba(255,255,255,0.8)"/>
                <circle cx="25" cy="65" r="2" fill="rgba(255,255,255,0.8)"/>
                <circle cx="45" cy="45" r="2.5" fill="rgba(255,255,255,0.8)"/>
                <circle cx="45" cy="85" r="2" fill="rgba(255,255,255,0.8)"/>
                <circle cx="65" cy="55" r="2" fill="rgba(255,255,255,0.8)"/>
                <circle cx="55" cy="90" r="2.5" fill="rgba(255,255,255,0.8)"/>
                <circle cx="70" cy="70" r="2" fill="rgba(255,255,255,0.8)"/>
              </g>
            </svg>
          </div>
        </div>
        <h1>水务管理系统</h1>
        <p class="subtitle">Water Management System</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
            class="custom-input"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            class="custom-input"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="card-footer">
        <div class="divider">
          <span>测试账号</span>
        </div>
        <div class="test-accounts">
          <div 
            class="account-item" 
            v-for="account in testAccounts" 
            :key="account.username"
            @click="fillTestAccount(account.username, account.password)"
          >
            <el-tag :type="account.type" effect="plain" size="small">{{ account.label }}</el-tag>
            <span class="account-text">{{ account.username }} / {{ account.password }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 版权信息 -->
    <div class="copyright">
      <p>© 2024 水务管理系统 All Rights Reserved</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 生成随机水滴样式
const getDropStyle = (index) => {
  const left = Math.random() * 100
  const delay = Math.random() * 5
  const duration = 3 + Math.random() * 4
  const size = 10 + Math.random() * 20
  return {
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`,
    width: `${size}px`,
    height: `${size}px`
  }
}

// 测试账号数据（与数据库一致）
const testAccounts = [
  { username: 'admin', password: 'admin123', label: '管理员', type: 'danger' },
  { username: 'user001', password: '123456', label: '普通用户', type: 'success' },
  { username: 'reader001', password: '123456', label: '抄表员', type: 'warning' },
  { username: 'repair001', password: '123456', label: '维修人员', type: 'primary' }
]

// 填充测试账号
const fillTestAccount = (username, password) => {
  loginForm.username = username
  loginForm.password = password
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.userLogin(loginForm)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 50%, #1e3c72 100%);
}

// ==========================================
// 动态波浪背景
// ==========================================
.bg-animation {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 200%;
  height: 100%;
  background-repeat: repeat-x;
  transform-origin: center bottom;
}

.wave1 {
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='rgba(255,255,255,0.1)' d='M0,192L48,197.3C96,203,192,213,288,229.3C384,245,480,267,576,250.7C672,235,768,181,864,181.3C960,181,1056,235,1152,234.7C1248,235,1344,181,1392,154.7L1440,128L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E") repeat-x;
  background-size: 50% 100%;
  animation: wave 15s linear infinite;
  opacity: 0.8;
}

.wave2 {
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='rgba(255,255,255,0.05)' d='M0,64L48,80C96,96,192,128,288,128C384,128,480,96,576,90.7C672,85,768,107,864,144C960,181,1056,235,1152,234.7C1248,235,1344,181,1392,154.7L1440,128L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E") repeat-x;
  background-size: 50% 100%;
  animation: wave 12s linear infinite reverse;
  opacity: 0.6;
}

.wave3 {
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='rgba(255,255,255,0.03)' d='M0,160L48,170.7C96,181,192,203,288,202.7C384,203,480,181,576,186.7C672,192,768,224,864,213.3C960,203,1056,149,1152,138.7C1248,128,1344,160,1392,176L1440,192L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E") repeat-x;
  background-size: 50% 100%;
  animation: wave 18s linear infinite;
  opacity: 0.4;
}

@keyframes wave {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}

// ==========================================
// 浮动水滴
// ==========================================
.floating-drops {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.drop {
  position: absolute;
  bottom: -50px;
  background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.3), rgba(255, 255, 255, 0.1));
  border-radius: 50%;
  animation: float-up 6s ease-in infinite;
  
  &::before {
    content: '';
    position: absolute;
    top: 10%;
    left: 20%;
    width: 30%;
    height: 30%;
    background: rgba(255, 255, 255, 0.5);
    border-radius: 50%;
  }
}

@keyframes float-up {
  0% {
    transform: translateY(0) scale(1);
    opacity: 0;
  }
  10% {
    opacity: 0.6;
  }
  90% {
    opacity: 0.6;
  }
  100% {
    transform: translateY(-100vh) scale(0.5);
    opacity: 0;
  }
}

// ==========================================
// 登录卡片
// ==========================================
.login-card {
  width: 420px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.2),
              0 0 0 1px rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 10;
  animation: card-appear 0.6s ease-out;
  
  // 光晕效果
  &::before {
    content: '';
    position: absolute;
    top: -2px;
    left: -2px;
    right: -2px;
    bottom: -2px;
    background: linear-gradient(135deg, rgba(24, 144, 255, 0.3), rgba(64, 169, 255, 0.1), rgba(24, 144, 255, 0.3));
    border-radius: 22px;
    z-index: -1;
    animation: glow 3s ease-in-out infinite alternate;
  }
}

@keyframes card-appear {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes glow {
  from {
    opacity: 0.5;
  }
  to {
    opacity: 1;
  }
}

.card-header {
  text-align: center;
  margin-bottom: 36px;
  
  .logo {
    margin-bottom: 16px;
    
    .logo-icon {
      width: 80px;
      height: 96px;
      margin: 0 auto;
      display: flex;
      align-items: center;
      justify-content: center;
      animation: logo-pulse 2s ease-in-out infinite;
      
      svg {
        width: 100%;
        height: 100%;
      }
    }
  }
  
  h1 {
    font-size: 26px;
    font-weight: 700;
    color: #1e3c72;
    margin-bottom: 8px;
    letter-spacing: 2px;
  }
  
  .subtitle {
    font-size: 13px;
    color: #909399;
    letter-spacing: 1px;
  }
}

@keyframes logo-pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

// ==========================================
// 表单样式
// ==========================================
.login-form {
  .el-form-item {
    margin-bottom: 24px;
  }
  
  .custom-input {
    :deep(.el-input__wrapper) {
      height: 48px;
      border-radius: 12px;
      padding: 0 16px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      border: 1px solid #e4e7ed;
      transition: all 0.3s ease;
      
      &:hover {
        border-color: #1890ff;
        box-shadow: 0 2px 12px rgba(24, 144, 255, 0.1);
      }
      
      &.is-focus {
        border-color: #1890ff;
        box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
      }
      
      .el-input__prefix {
        color: #909399;
      }
    }
    
    :deep(.el-input__inner) {
      height: 100%;
      font-size: 15px;
      
      &::placeholder {
        color: #c0c4cc;
      }
    }
  }
  
  .login-button {
    width: 100%;
    height: 48px;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 4px;
    background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
    border: none;
    box-shadow: 0 8px 20px rgba(24, 144, 255, 0.3);
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 12px 28px rgba(24, 144, 255, 0.4);
      background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%);
    }
    
    &:active {
      transform: translateY(0);
      box-shadow: 0 6px 16px rgba(24, 144, 255, 0.3);
    }
  }
}

// ==========================================
// 卡片底部
// ==========================================
.card-footer {
  margin-top: 24px;
  
  .divider {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    
    &::before,
    &::after {
      content: '';
      flex: 1;
      height: 1px;
      background: linear-gradient(90deg, transparent, #e4e7ed, transparent);
    }
    
    span {
      padding: 0 16px;
      color: #909399;
      font-size: 12px;
    }
  }
  
  .test-accounts {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .account-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 14px;
    background: #f8f9fa;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      background: #e8f4ff;
      transform: translateX(4px);
    }
    
    .account-text {
      font-size: 13px;
      color: #606266;
      font-family: 'Monaco', 'Consolas', monospace;
    }
  }
}

// ==========================================
// 版权信息
// ==========================================
.copyright {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  
  p {
    color: rgba(255, 255, 255, 0.6);
    font-size: 12px;
    letter-spacing: 0.5px;
  }
}

// ==========================================
// 响应式
// ==========================================
@media (max-width: 480px) {
  .login-card {
    width: calc(100% - 32px);
    margin: 16px;
    padding: 32px 24px;
  }
  
  .card-header {
    h1 {
      font-size: 22px;
    }
  }
}
</style>
