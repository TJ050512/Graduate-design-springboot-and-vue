<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header" style="background: linear-gradient(135deg, #ee9ca7 0%, #ffdde1 100%);">
      <div class="header-info">
        <h2>我的用水</h2>
        <p>查看您的用水记录和费用明细</p>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ totalUsage.toFixed(2) }} <small>m³</small></div>
            <div class="stat-label">总用水量</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ totalAmount.toFixed(2) }}</div>
            <div class="stat-label">总费用</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ unpaidAmount.toFixed(2) }}</div>
            <div class="stat-label">待缴费用</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="抄表月份">
          <el-date-picker
            v-model="searchForm.readMonth"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
            clearable
            style="width: 140px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="待确认" :value="0" />
            <el-option label="已确认" :value="1" />
            <el-option label="已缴费" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table 
        :data="tableData" 
        v-loading="loading"
        stripe
        highlight-current-row
      >
        <el-table-column prop="readMonth" label="抄表月份" width="120" align="center">
          <template #default="{ row }">
            <el-tag effect="plain" type="info">{{ row.readMonth }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="readDate" label="抄表日期" width="120" align="center" />
        <el-table-column prop="lastReading" label="上次读数" width="110" align="right">
          <template #default="{ row }">
            <span>{{ row.lastReading }} m³</span>
          </template>
        </el-table-column>
        <el-table-column prop="currentReading" label="本次读数" width="110" align="right">
          <template #default="{ row }">
            <span class="current-reading">{{ row.currentReading }} m³</span>
          </template>
        </el-table-column>
        <el-table-column prop="usage" label="用水量" width="110" align="right">
          <template #default="{ row }">
            <span class="usage-value">{{ row.usage }} m³</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="单价" width="100" align="right">
          <template #default="{ row }">
            <span>¥{{ row.price }}/m³</span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="费用" width="110" align="right">
          <template #default="{ row }">
            <span class="amount-value">¥{{ row.amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="getStatusType(row.status)"
              effect="light"
            >
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="primary"
              size="small"
              @click="goToPayment"
            >
              <el-icon><Wallet /></el-icon>
              去缴费
            </el-button>
            <span v-else-if="row.status === 2" class="paid-text">已缴费</span>
            <span v-else class="wait-text">等待确认</span>
          </template>
        </el-table-column>
      </el-table>
    
    <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUsagePage, getUsageStatistics } from '@/api/waterUsage'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import {
  Search,
  Refresh,
  Document,
  Money,
  Warning,
  Wallet
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({
  readMonth: '',
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 统计数据（从后端获取）
const totalUsage = ref(0)
const totalAmount = ref(0)
const unpaidAmount = ref(0)

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await getUsageStatistics(userStore.userInfo?.userId)
    if (res.code === 200 && res.data) {
      totalUsage.value = res.data.totalUsage || 0
      totalAmount.value = res.data.totalAmount || 0
      unpaidAmount.value = res.data.unpaidAmount || 0
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const getStatusName = (status) => {
  const names = { 0: '待确认', 1: '待缴费', 2: '已缴费' }
  return names[status] || '未知'
}

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'danger', 2: 'success' }
  return types[status] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUsagePage({
      page: pagination.current,
      size: pagination.size,
      userId: userStore.userInfo?.userId,  // 只查询当前用户的数据
      ...searchForm
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.readMonth = ''
  searchForm.status = null
  handleSearch()
}

const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

// 跳转到缴费页面
const goToPayment = () => {
  router.push('/myPayment')
}

onMounted(() => {
  loadData()
  loadStatistics()
})
</script>

<style scoped lang="scss">
.page-container {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 24px;
  border-radius: 12px;
  color: #333;
  
  .header-info {
    h2 { font-size: 22px; font-weight: 600; margin-bottom: 6px; }
    p { font-size: 14px; opacity: 0.75; }
  }
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  .stat-icon {
    width: 50px;
    height: 50px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 22px;
  }
  
  .stat-info {
    .stat-value {
      font-size: 24px;
      font-weight: 700;
      color: #303133;
      
      small { font-size: 14px; font-weight: 400; color: #909399; }
    }
    .stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
  }
}

.search-card {
  margin-bottom: 20px;
  :deep(.el-card__body) { padding: 20px; }
  .search-form {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    .el-form-item { margin-bottom: 0; margin-right: 0; }
  }
}

.table-card {
  :deep(.el-card__body) { padding: 0; }
}

.current-reading { color: var(--primary-color); font-weight: 500; }
.usage-value { font-weight: 600; color: #11998e; }
.amount-value { font-weight: 600; color: #f56c6c; }
.paid-text { color: #67c23a; font-size: 13px; }
.wait-text { color: #909399; font-size: 13px; }

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid var(--border-color-light);
}
</style>
