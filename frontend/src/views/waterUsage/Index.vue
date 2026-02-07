<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header" style="background: linear-gradient(135deg, #ee9ca7 0%, #ffdde1 100%);">
      <div class="header-info">
        <h2>用水记录</h2>
        <p>{{ isAdmin ? '管理用水记录，通知抄表员进行抄表' : '查看和添加用水记录' }}</p>
      </div>
      <div class="header-actions">
        <el-button v-if="isReader" type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加用水记录
        </el-button>
      </div>
    </div>
    
    <!-- 管理员：需要抄表的水表面板 -->
    <el-card v-if="isAdmin" class="notify-card">
      <template #header>
        <div class="notify-header">
          <div class="notify-title">
            <el-icon><Bell /></el-icon>
            <span>待抄表水表（读数已变化）</span>
          </div>
          <div class="notify-actions">
            <el-button size="small" @click="loadNeedReadMeters" :loading="needReadLoading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button size="small" type="warning" @click="handleNotifyAll" :disabled="unnotifiedList.length === 0">
              <el-icon><Bell /></el-icon>
              一键通知全部抄表 ({{ unnotifiedList.length }})
            </el-button>
          </div>
        </div>
      </template>
      <!-- 未通知的水表 -->
      <el-table :data="unnotifiedList" v-loading="needReadLoading" size="small" stripe>
        <el-table-column prop="meterNo" label="水表编号" width="120" />
        <el-table-column prop="meterType" label="水表类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.meterType === 1 ? 'success' : row.meterType === 2 ? 'warning' : ''" size="small" effect="light">
              {{ {1: '家用表', 2: '商用表', 3: '工业表'}[row.meterType] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用户" width="100">
          <template #default="{ row }">
            {{ getUserInfo(row.userId)?.realName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="installAddress" label="安装地址" min-width="150" show-overflow-tooltip />
        <el-table-column prop="lastRecordedReading" label="上次记录" width="120" align="right">
          <template #default="{ row }">
            <span>{{ Number(row.lastRecordedReading).toFixed(4) }} m³</span>
          </template>
        </el-table-column>
        <el-table-column prop="currentReading" label="当前读数" width="120" align="right">
          <template #default="{ row }">
            <span style="color: #1890ff; font-weight: bold;">{{ Number(row.currentReading).toFixed(4) }} m³</span>
          </template>
        </el-table-column>
        <el-table-column prop="diff" label="变化量" width="110" align="right">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">+{{ Number(row.diff).toFixed(4) }} m³</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="center">
          <template #default="{ row }">
            <el-button 
              type="warning" 
              size="small" 
              @click="handleNotify(row)"
            >
              <el-icon><Bell /></el-icon>
              通知抄表
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="unnotifiedList.length === 0 && !needReadLoading" class="empty-tip">
        {{ notifiedList.length > 0 ? '所有变化的水表都已通知抄表员' : '当前所有水表读数无变化，无需抄表' }}
      </div>
      
      <!-- 已通知待处理的水表 -->
      <div v-if="notifiedList.length > 0" class="notified-section">
        <div class="notified-title">
          <el-icon><Clock /></el-icon>
          <span>已通知，等待抄表员处理（{{ notifiedList.length }}）</span>
        </div>
        <el-table :data="notifiedList" size="small" stripe>
          <el-table-column prop="meterNo" label="水表编号" width="120" />
          <el-table-column prop="meterType" label="水表类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.meterType === 1 ? 'success' : row.meterType === 2 ? 'warning' : ''" size="small" effect="light">
                {{ {1: '家用表', 2: '商用表', 3: '工业表'}[row.meterType] || '未知' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="用户" width="100">
            <template #default="{ row }">
              {{ getUserInfo(row.userId)?.realName || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="installAddress" label="安装地址" min-width="150" show-overflow-tooltip />
          <el-table-column prop="diff" label="通知时变化量" width="130" align="right">
            <template #default="{ row }">
              <span style="color: #909399;">+{{ Number(row.diff).toFixed(4) }} m³</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120" align="center">
            <template #default>
              <el-tag type="info" size="small" effect="plain">
                <el-icon><Clock /></el-icon>
                等待抄表
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
    
    <!-- 抄表员：待处理抄表任务面板 -->
    <el-card v-if="isReader && pendingTasks.length > 0" class="task-card">
      <template #header>
        <div class="task-header">
          <div class="task-title">
            <el-icon><EditPen /></el-icon>
            <span>待处理抄表任务</span>
            <el-tag type="danger" size="small" effect="dark" round>{{ pendingTasks.length }}</el-tag>
          </div>
          <el-button size="small" @click="loadPendingTasks" :loading="taskLoading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      <div class="task-list">
        <div v-for="task in pendingTasks" :key="task.taskId" class="task-item">
          <div class="task-info">
            <div class="task-meter">
              <el-icon><Monitor /></el-icon>
              <span class="meter-no">水表 {{ getMeterInfo(task.meterId)?.meterNo || task.meterId }}</span>
              <el-tag :type="getMeterInfo(task.meterId)?.meterType === 1 ? 'success' : getMeterInfo(task.meterId)?.meterType === 2 ? 'warning' : ''" size="small" effect="light">
                {{ {1: '家用表', 2: '商用表', 3: '工业表'}[getMeterInfo(task.meterId)?.meterType] || '' }}
              </el-tag>
              <span class="task-user">
                <el-icon><User /></el-icon>
                {{ getUserInfo(task.userId)?.realName || '-' }}
              </span>
              <span class="task-address">{{ getMeterInfo(task.meterId)?.installAddress || '' }}</span>
            </div>
            <div class="task-readings">
              <div class="reading-item">
                <span class="reading-label">上次读数</span>
                <span class="reading-value">{{ Number(task.lastReading).toFixed(4) }} m³</span>
              </div>
              <div class="reading-arrow">→</div>
              <div class="reading-item current">
                <span class="reading-label">当前读数</span>
                <span class="reading-value">{{ Number(getMeterInfo(task.meterId)?.currentReading || task.currentReading).toFixed(4) }} m³</span>
              </div>
              <div class="reading-diff">
                +{{ (Number(getMeterInfo(task.meterId)?.currentReading || task.currentReading) - Number(task.lastReading)).toFixed(4) }} m³
              </div>
            </div>
          </div>
          <div class="task-action">
            <div class="task-input-group">
              <span class="input-label">确认本次读数（m³）</span>
              <el-input-number
                v-model="task._confirmReading"
                :precision="4"
                :min="Number(task.lastReading)"
                :step="0.01"
                size="default"
                controls-position="right"
              />
            </div>
            <el-button 
              type="primary" 
              @click="handleSubmitTask(task)" 
              :loading="task._submitting"
              :disabled="!task._confirmReading"
            >
              <el-icon><CircleCheck /></el-icon>
              提交记录
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户">
          <el-select
            v-model="searchForm.userId"
            placeholder="请选择用户"
            clearable
            filterable
            style="width: 180px"
          >
            <el-option
              v-for="user in userList"
              :key="user.userId"
              :label="`${user.realName}(${user.username})`"
              :value="user.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="水表">
          <el-select
            v-model="searchForm.meterId"
            placeholder="请选择水表"
            clearable
            filterable
            style="width: 180px"
          >
            <el-option
              v-for="meter in meterList"
              :key="meter.meterId"
              :label="meter.meterNo"
              :value="meter.meterId"
            />
          </el-select>
        </el-form-item>
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
        <el-table-column prop="usageId" label="ID" width="70" align="center" />
        <el-table-column prop="readMonth" label="抄表月份" width="110" align="center">
          <template #default="{ row }">
            <el-tag effect="plain" type="info">{{ row.readMonth }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="readDate" label="抄表日期" width="110" align="center" />
        <el-table-column label="水表信息" width="140">
          <template #default="{ row }">
            <div class="meter-info" v-if="getMeterInfo(row.meterId)">
              <el-icon><Monitor /></el-icon>
              <span>{{ getMeterInfo(row.meterId).meterNo }}</span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="用户信息" width="120">
          <template #default="{ row }">
            <div class="user-info" v-if="getUserInfo(row.userId)">
              <el-icon><User /></el-icon>
              <span>{{ getUserInfo(row.userId).realName }}</span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastReading" label="上次读数" width="100" align="right">
          <template #default="{ row }">
            <span class="reading-text">{{ row.lastReading }} m³</span>
          </template>
        </el-table-column>
        <el-table-column prop="currentReading" label="本次读数" width="100" align="right">
          <template #default="{ row }">
            <span class="reading-text current">{{ row.currentReading }} m³</span>
          </template>
        </el-table-column>
        <el-table-column prop="usage" label="用水量" width="100" align="right">
          <template #default="{ row }">
            <span class="usage-value">{{ row.usage }} m³</span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="应缴金额" width="110" align="right">
          <template #default="{ row }">
            <span class="amount-value">¥{{ row.amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="getStatusType(row.status)"
              effect="light"
              class="status-tag"
            >
              <span class="status-dot" :class="getStatusClass(row.status)"></span>
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <!-- 管理员：只能确认 -->
              <el-button
                v-if="isAdmin && row.status === 0"
                type="success"
                link
                @click="handleConfirm(row)"
              >
                <el-icon><CircleCheck /></el-icon>
                确认
              </el-button>
              <!-- 抄表员：编辑、删除 -->
              <el-button v-if="isReader" type="primary" link @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button v-if="isReader" type="danger" link @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      destroy-on-close
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="130px"
        class="dialog-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="水表" prop="meterId">
              <el-select
                v-model="formData.meterId"
                placeholder="请选择水表"
                filterable
                style="width: 100%"
                @change="handleMeterChange"
              >
                <el-option
                  v-for="meter in meterList"
                  :key="meter.meterId"
                  :label="`${meter.meterNo} - ${meter.installAddress}`"
                  :value="meter.meterId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户" prop="userId">
              <el-select
                v-model="formData.userId"
                placeholder="请选择用户"
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="user in userList"
                  :key="user.userId"
                  :label="`${user.realName}(${user.username})`"
                  :value="user.userId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="抄表月份" prop="readMonth">
              <el-date-picker
                v-model="formData.readMonth"
                type="month"
                placeholder="选择月份"
                format="YYYY-MM"
                value-format="YYYY-MM"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="抄表日期" prop="readDate">
              <el-date-picker
                v-model="formData.readDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上次读数" prop="lastReading">
              <el-input-number
                v-model="formData.lastReading"
                :precision="2"
                :min="0"
                style="width: 100%"
                @change="calculateUsage"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="本次读数" prop="currentReading">
              <el-input-number
                v-model="formData.currentReading"
                :precision="2"
                :min="0"
                style="width: 100%"
                @change="calculateUsage"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用水量(m³)">
              <el-input-number
                v-model="formData.usage"
                :precision="2"
                :min="0"
                :disabled="true"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单价(元/m³)" prop="price">
              <el-input-number
                v-model="formData.price"
                :precision="2"
                :min="0"
                style="width: 100%"
                @change="calculateAmount"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="应缴金额(元)">
          <el-input-number
            v-model="formData.amount"
            :precision="2"
            :min="0"
            :disabled="true"
            style="width: 100%"
            class="amount-input"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getUsagePage, addUsage, updateUsage, confirmUsage, deleteUsage, getUsageById } from '@/api/waterUsage'
import { getUserPage } from '@/api/user'
import { getMeterPage } from '@/api/waterMeter'
import { getNeedReadMeters, notifyMeterRead, notifyAllMeterRead, getPendingTasks as fetchPendingTasks, completeTask } from '@/api/meterReadTask'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  Refresh,
  Edit,
  Delete,
  Monitor,
  User,
  CircleCheck,
  Bell,
  EditPen,
  Clock
} from '@element-plus/icons-vue'

// 角色判断
const isAdmin = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return userInfo.userType === 1
})
const isReader = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return userInfo.userType === 3
})

// 需要抄表的水表列表（管理员用）
const needReadList = ref([])
const needReadLoading = ref(false)

// 分成两组：未通知的 / 已通知待处理的
const unnotifiedList = computed(() => needReadList.value.filter(item => !item.hasPendingTask))
const notifiedList = computed(() => needReadList.value.filter(item => item.hasPendingTask))

// 抄表员待处理任务
const pendingTasks = ref([])
const taskLoading = ref(false)

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加用水记录')
const formRef = ref(null)
const userList = ref([])
const meterList = ref([])

const searchForm = reactive({
  userId: null,
  meterId: null,
  readMonth: '',
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  usageId: null,
  meterId: null,
  userId: null,
  readerId: null,
  readMonth: '',
  readDate: '',
  lastReading: 0,
  currentReading: 0,
  usage: 0,
  price: 0,
  amount: 0,
  status: 0
})

const formRules = {
  meterId: [{ required: true, message: '请选择水表', trigger: 'change' }],
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  readMonth: [{ required: true, message: '请选择抄表月份', trigger: 'change' }],
  readDate: [{ required: true, message: '请选择抄表日期', trigger: 'change' }],
  lastReading: [{ required: true, message: '请输入上次读数', trigger: 'blur' }],
  currentReading: [{ required: true, message: '请输入本次读数', trigger: 'blur' }],
  price: [{ required: true, message: '请输入水费单价', trigger: 'blur' }]
}

const getStatusName = (status) => {
  const names = { 0: '待确认', 1: '已确认', 2: '已缴费' }
  return names[status] || '未知'
}

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'info' }
  return types[status] || 'info'
}

const getStatusClass = (status) => {
  const classes = { 0: 'warning', 1: 'success', 2: 'info' }
  return classes[status] || ''
}

const getUserInfo = (userId) => {
  return userList.value.find(u => u.userId === userId)
}

const getMeterInfo = (meterId) => {
  return meterList.value.find(m => m.meterId === meterId)
}

const loadUserList = async () => {
  try {
    const res = await getUserPage({ page: 1, size: 1000 })
    if (res.code === 200) {
      userList.value = res.data.records
    }
  } catch (error) {
    console.error('加载用户列表失败', error)
  }
}

const loadMeterList = async () => {
  try {
    const res = await getMeterPage({ page: 1, size: 1000 })
    if (res.code === 200) {
      meterList.value = res.data.records
    }
  } catch (error) {
    console.error('加载水表列表失败', error)
  }
}

const calculateUsage = () => {
  if (formData.currentReading && formData.lastReading) {
    formData.usage = parseFloat((formData.currentReading - formData.lastReading).toFixed(2))
    if (formData.usage < 0) {
      ElMessage.warning('本次读数不能小于上次读数')
      formData.usage = 0
    }
    calculateAmount()
  }
}

const calculateAmount = () => {
  if (formData.usage && formData.price) {
    formData.amount = parseFloat((formData.usage * formData.price).toFixed(2))
  }
}

const handleMeterChange = (meterId) => {
  const meter = getMeterInfo(meterId)
  if (meter) {
    formData.userId = meter.userId
    formData.lastReading = meter.currentReading || 0
    calculateUsage()
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUsagePage({
      page: pagination.current,
      size: pagination.size,
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
  searchForm.userId = null
  searchForm.meterId = null
  searchForm.readMonth = ''
  searchForm.status = null
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '添加用水记录'
  Object.assign(formData, {
    usageId: null,
    meterId: null,
    userId: null,
    readerId: null,
    readMonth: '',
    readDate: '',
    lastReading: 0,
    currentReading: 0,
    usage: 0,
    price: 2.8,
    amount: 0,
    status: 0
  })
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑用水记录'
  try {
    const res = await getUsageById(row.usageId)
    if (res.code === 200 && res.data) {
      Object.assign(formData, {
        ...res.data,
        readDate: res.data.readDate || '',
        readMonth: res.data.readMonth || ''
      })
      dialogVisible.value = true
    }
  } catch (error) {
    console.error(error)
  }
}

const handleConfirm = (row) => {
  ElMessageBox.confirm('确定要确认该用水记录吗？', '确认操作', {
    type: 'warning',
    confirmButtonText: '确认',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      const res = await confirmUsage(row.usageId)
      if (res.code === 200) {
        ElMessage.success('确认成功')
        loadData()
      }
    } catch (error) {
      console.error(error)
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该用水记录吗？', '删除确认', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    try {
      const res = await deleteUsage(row.usageId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadData()
      }
    } catch (error) {
      console.error(error)
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (formData.currentReading <= formData.lastReading) {
        ElMessage.error('本次读数必须大于上次读数')
        return
      }
      
      submitLoading.value = true
      try {
        let res
        if (formData.usageId) {
          res = await updateUsage(formData)
        } else {
          res = await addUsage(formData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.usageId ? '更新成功' : '添加成功')
          dialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

// ========== 管理员：抄表通知功能 ==========
const loadNeedReadMeters = async () => {
  needReadLoading.value = true
  try {
    const res = await getNeedReadMeters()
    if (res.code === 200) {
      needReadList.value = res.data || []
    }
  } catch (error) {
    console.error('加载需要抄表的水表失败', error)
  } finally {
    needReadLoading.value = false
  }
}

const handleNotify = async (row) => {
  ElMessageBox.confirm(
    `确定要通知抄表员对水表 ${row.meterNo} 进行抄表吗？\n变化量：+${Number(row.diff).toFixed(4)} m³`,
    '通知抄表',
    {
      confirmButtonText: '发送通知',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      const res = await notifyMeterRead([row.meterId])
      if (res.code === 200) {
        ElMessage.success('已通知抄表员')
        loadNeedReadMeters()
      }
    } catch (error) {
      ElMessage.error('通知失败')
    }
  }).catch(() => {})
}

const handleNotifyAll = async () => {
  const pendingCount = needReadList.value.filter(item => !item.hasPendingTask).length
  if (pendingCount === 0) {
    ElMessage.warning('所有水表都已通知')
    return
  }
  
  ElMessageBox.confirm(
    `确定要通知抄表员对 ${pendingCount} 个水表进行抄表吗？`,
    '批量通知抄表',
    {
      confirmButtonText: '全部通知',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await notifyAllMeterRead()
      if (res.code === 200) {
        ElMessage.success(res.data)
        loadNeedReadMeters()
      }
    } catch (error) {
      ElMessage.error('通知失败')
    }
  }).catch(() => {})
}

// ========== 抄表员：待处理任务功能 ==========
const loadPendingTasks = async () => {
  taskLoading.value = true
  try {
    const res = await fetchPendingTasks()
    if (res.code === 200) {
      // 给每个任务添加临时字段
      pendingTasks.value = (res.data || []).map(task => ({
        ...task,
        _confirmReading: Number(getMeterInfo(task.meterId)?.currentReading || task.currentReading),
        _submitting: false
      }))
    }
  } catch (error) {
    console.error('加载待处理任务失败', error)
  } finally {
    taskLoading.value = false
  }
}

const handleSubmitTask = async (task) => {
  if (!task._confirmReading) {
    ElMessage.warning('请填写本次读数')
    return
  }

  if (task._confirmReading <= Number(task.lastReading)) {
    ElMessage.error('本次读数必须大于上次读数')
    return
  }

  task._submitting = true
  try {
    // 自动生成抄表日期和月份
    const now = new Date()
    const today = now.toISOString().split('T')[0]
    const readMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    
    // 1. 创建用水记录
    const usageData = {
      meterId: task.meterId,
      userId: task.userId,
      readMonth: readMonth,
      readDate: today,
      lastReading: Number(task.lastReading),
      currentReading: task._confirmReading,
      status: 0
    }

    const res = await addUsage(usageData)
    if (res.code === 200) {
      // 2. 完成抄表任务
      await completeTask(task.taskId, res.data?.usageId || null)
      
      ElMessage.success('抄表记录提交成功！')
      // 刷新任务列表和数据
      loadPendingTasks()
      loadData()
    }
  } catch (error) {
    ElMessage.error('提交失败，请重试')
    console.error(error)
  } finally {
    task._submitting = false
  }
}

onMounted(() => {
  loadUserList()
  loadMeterList()
  loadData()
  
  // 管理员加载需要抄表的水表
  if (isAdmin.value) {
    loadNeedReadMeters()
  }
  
  // 抄表员加载待处理任务（需要在 meterList 加载后执行）
  if (isReader.value) {
    // 延迟加载，确保 meterList 已加载
    setTimeout(() => {
      loadPendingTasks()
    }, 500)
  }
})
</script>

<style scoped lang="scss">
.header-actions {
  display: flex;
  gap: 12px;
}

// 抄表员任务面板
.task-card {
  margin-bottom: 20px;
  border: 1px solid #d4edda;
  background: linear-gradient(135deg, #f0fff4 0%, #e8f8ee 100%);
  
  :deep(.el-card__header) {
    padding: 12px 20px;
    background: rgba(82, 196, 26, 0.05);
    border-bottom: 1px solid #d4edda;
  }
  
  .task-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .task-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      color: #52c41a;
      
      .el-icon {
        font-size: 18px;
      }
    }
  }
  
  .task-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
    
    .task-item {
      background: white;
      border-radius: 10px;
      padding: 16px 20px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      border: 1px solid #f0f0f0;
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 24px;
      transition: box-shadow 0.3s;
      
      &:hover {
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
      }
      
      .task-info {
        flex: 1;
        
        .task-meter {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 12px;
          
          .el-icon {
            color: #52c41a;
          }
          
          .meter-no {
            font-weight: 600;
            font-size: 15px;
            color: #303133;
          }
          
          .task-user {
            display: flex;
            align-items: center;
            gap: 4px;
            color: #606266;
            font-size: 13px;
            margin-left: 8px;
          }
          
          .task-address {
            color: #909399;
            font-size: 12px;
            margin-left: 8px;
          }
        }
        
        .task-readings {
          display: flex;
          align-items: center;
          gap: 12px;
          
          .reading-item {
            display: flex;
            flex-direction: column;
            gap: 2px;
            
            .reading-label {
              font-size: 12px;
              color: #909399;
            }
            
            .reading-value {
              font-size: 16px;
              font-weight: 600;
              color: #606266;
              font-family: 'Courier New', monospace;
            }
            
            &.current .reading-value {
              color: #1890ff;
            }
          }
          
          .reading-arrow {
            color: #c0c4cc;
            font-size: 18px;
            margin: 8px 0 0;
          }
          
          .reading-diff {
            margin: 8px 0 0;
            color: #f56c6c;
            font-weight: bold;
            font-size: 14px;
            background: rgba(245, 108, 108, 0.1);
            padding: 2px 8px;
            border-radius: 4px;
          }
        }
      }
      
      .task-action {
        display: flex;
        align-items: flex-end;
        gap: 12px;
        flex-shrink: 0;
        
        .task-input-group {
          display: flex;
          flex-direction: column;
          gap: 4px;
          
          .input-label {
            font-size: 12px;
            color: #909399;
          }
          
          .input-unit {
            font-size: 12px;
            color: #909399;
            align-self: flex-end;
            margin-bottom: 6px;
            display: none;
          }
        }
      }
    }
  }
}

// 抄表通知面板
.notify-card {
  margin-bottom: 20px;
  border: 1px solid #ffecd2;
  background: linear-gradient(135deg, #fff9f0 0%, #fff3e0 100%);
  
  :deep(.el-card__header) {
    padding: 12px 20px;
    background: rgba(230, 162, 60, 0.05);
    border-bottom: 1px solid #ffecd2;
  }
  
  .notify-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .notify-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      color: #e6a23c;
      
      .el-icon {
        font-size: 18px;
      }
    }
    
    .notify-actions {
      display: flex;
      gap: 8px;
    }
  }
  
  .empty-tip {
    text-align: center;
    padding: 24px 0;
    color: #909399;
    font-size: 14px;
  }
  
  .notified-section {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px dashed #e0e0e0;
    
    .notified-title {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #909399;
      margin-bottom: 8px;
      
      .el-icon {
        font-size: 14px;
      }
    }
  }
}

.page-container {
  animation: fadeIn 0.3s ease;
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

// 页面头部
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 24px;
  border-radius: 12px;
  color: #333;
  
  .header-info {
    h2 {
      font-size: 22px;
      font-weight: 600;
      margin-bottom: 6px;
    }
    
    p {
      font-size: 14px;
      opacity: 0.75;
    }
  }
  
  .el-button {
    background: rgba(255, 255, 255, 0.8);
    border-color: rgba(255, 255, 255, 0.9);
    color: #333;
    
    &:hover {
      background: white;
      border-color: white;
    }
  }
}

// 搜索卡片
.search-card {
  margin-bottom: 20px;
  
  :deep(.el-card__body) {
    padding: 20px;
  }
  
  .search-form {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    
    .el-form-item {
      margin-bottom: 0;
      margin-right: 0;
    }
  }
}

// 表格卡片
.table-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}

.meter-info,
.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  
  .el-icon {
    color: var(--primary-color);
  }
}

.reading-text {
  font-family: 'Monaco', 'Consolas', monospace;
  color: var(--text-secondary);
  
  &.current {
    color: var(--primary-color);
    font-weight: 500;
  }
}

.usage-value {
  font-weight: 600;
  color: #11998e;
}

.amount-value {
  font-weight: 600;
  color: #f56c6c;
  font-size: 14px;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  
  .status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    
    &.warning {
      background-color: #e6a23c;
      box-shadow: 0 0 0 3px rgba(230, 162, 60, 0.2);
    }
    
    &.success {
      background-color: #52c41a;
      box-shadow: 0 0 0 3px rgba(82, 196, 26, 0.2);
    }
    
    &.info {
      background-color: #909399;
      box-shadow: 0 0 0 3px rgba(144, 147, 153, 0.2);
    }
  }
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 4px;
}

// 分页
.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid var(--border-color-light);
}

// 对话框表单
.dialog-form {
  padding: 10px 20px;
}

.amount-input {
  :deep(.el-input__inner) {
    color: #f56c6c;
    font-weight: 600;
    font-size: 16px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 响应式
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .search-form {
    flex-direction: column;
  }
}
</style>
