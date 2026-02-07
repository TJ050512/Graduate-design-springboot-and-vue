<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);">
      <div class="header-info">
        <h2>我的报修</h2>
        <p>查看和提交报修工单，跟踪处理进度</p>
      </div>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        提交报修
      </el-button>
    </div>
    
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="工单编号">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="请输入工单编号"
            clearable
            :prefix-icon="Search"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="报修类型">
          <el-select v-model="searchForm.repairType" placeholder="全部类型" clearable style="width: 130px">
            <el-option label="水表故障" :value="1" />
            <el-option label="漏水" :value="2" />
            <el-option label="水质问题" :value="3" />
            <el-option label="水压异常" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="待处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已取消" :value="3" />
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
        <el-table-column prop="orderNo" label="工单编号" width="180">
          <template #default="{ row }">
            <span class="order-no">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="报修标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="repairType" label="报修类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getRepairTypeTag(row.repairType)" effect="light">
              {{ getRepairTypeName(row.repairType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="紧急度" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" effect="dark" size="small">
              {{ getPriorityName(row.priority) }}
            </el-tag>
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
        <el-table-column prop="createTime" label="提交时间" width="170" align="center" />
        <el-table-column prop="completeTime" label="完成时间" width="170" align="center">
          <template #default="{ row }">
            <span>{{ row.completeTime || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="150" align="center">
          <template #default="{ row }">
            <el-rate v-if="row.rating" v-model="row.rating" disabled size="small" />
            <span v-else class="text-secondary">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" link @click="handleView(row)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button
                v-if="row.status === 2 && !row.rating"
                type="success"
                link
                @click="handleFeedback(row)"
              >
                <el-icon><Star /></el-icon>
                评价
              </el-button>
              <el-button
                v-if="row.status === 0"
                type="danger"
                link
                @click="handleCancel(row)"
              >
                <el-icon><CircleClose /></el-icon>
                取消
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
    
    <!-- 创建报修对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="提交报修"
      width="600px"
      destroy-on-close
      @close="handleCreateDialogClose"
    >
      <el-form
        ref="createFormRef"
        :model="createFormData"
        :rules="createFormRules"
        label-width="100px"
        class="dialog-form"
      >
        <el-form-item label="报修类型" prop="repairType">
          <el-select v-model="createFormData.repairType" placeholder="请选择报修类型" style="width: 100%">
            <el-option label="水表故障" :value="1" />
            <el-option label="漏水" :value="2" />
            <el-option label="水质问题" :value="3" />
            <el-option label="水压异常" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联水表">
          <el-select
            v-model="createFormData.meterId"
            placeholder="请选择关联水表（可选）"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="meter in meterList"
              :key="meter.meterId"
              :label="`${meter.meterNo} - ${meter.installAddress || '未知地址'}`"
              :value="meter.meterId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="报修标题" prop="title">
          <el-input v-model="createFormData.title" placeholder="请输入报修标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="问题描述" prop="description">
          <el-input
            v-model="createFormData.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述您遇到的问题..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="createFormData.priority">
            <el-radio-button :value="1">
              <el-tag type="danger" effect="dark" size="small">紧急</el-tag>
            </el-radio-button>
            <el-radio-button :value="2">
              <el-tag type="warning" effect="dark" size="small">普通</el-tag>
            </el-radio-button>
            <el-radio-button :value="3">
              <el-tag type="success" effect="dark" size="small">低</el-tag>
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="createFormData.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="createFormData.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="报修地址" prop="address">
          <el-input v-model="createFormData.address" placeholder="请输入详细地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreateSubmit" :loading="submitLoading">提交</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="工单详情"
      width="650px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工单编号" :span="2">
          <span class="order-no">{{ currentOrder.orderNo }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="报修标题" :span="2">
          {{ currentOrder.title }}
        </el-descriptions-item>
        <el-descriptions-item label="报修类型">
          <el-tag :type="getRepairTypeTag(currentOrder.repairType)" effect="light">
            {{ getRepairTypeName(currentOrder.repairType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="紧急度">
          <el-tag :type="getPriorityType(currentOrder.priority)" effect="dark">
            {{ getPriorityName(currentOrder.priority) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentOrder.status)" effect="light">
            {{ getStatusName(currentOrder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系人">
          {{ currentOrder.contactName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          {{ currentOrder.contactPhone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="报修地址" :span="2">
          {{ currentOrder.address || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">
          <div class="description-text">{{ currentOrder.description }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">
          {{ currentOrder.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="完成时间">
          {{ currentOrder.completeTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="处理结果" :span="2" v-if="currentOrder.handleResult">
          <div class="description-text result-text">{{ currentOrder.handleResult }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
    
    <!-- 评价对话框 -->
    <el-dialog
      v-model="feedbackDialogVisible"
      title="评价工单"
      width="500px"
    >
      <div class="feedback-form">
        <div class="rating-section">
          <span class="label">服务评分：</span>
          <el-rate v-model="feedbackForm.rating" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" show-text />
        </div>
        <div class="feedback-section">
          <span class="label">反馈意见：</span>
          <el-input
            v-model="feedbackForm.feedback"
            type="textarea"
            :rows="4"
            placeholder="请输入您的反馈意见（可选）..."
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="feedbackDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleFeedbackSubmit" :loading="submitLoading">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRepairOrderPage, getRepairOrderById, createRepairOrder, cancelOrder, feedbackOrder } from '@/api/repair'
import { getMyMeters } from '@/api/waterMeter'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  Refresh,
  View,
  Star,
  CircleClose
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const feedbackDialogVisible = ref(false)
const createFormRef = ref(null)
const meterList = ref([])

const searchForm = reactive({
  orderNo: '',
  repairType: null,
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const createFormData = reactive({
  repairType: null,
  meterId: null,
  title: '',
  description: '',
  priority: 2,
  contactName: '',
  contactPhone: '',
  address: ''
})

const currentOrder = reactive({
  orderId: null,
  orderNo: '',
  repairType: null,
  title: '',
  description: '',
  contactName: '',
  contactPhone: '',
  address: '',
  status: null,
  handleResult: '',
  createTime: '',
  completeTime: ''
})

const feedbackForm = reactive({
  orderId: null,
  feedback: '',
  rating: 5
})

const createFormRules = {
  repairType: [{ required: true, message: '请选择报修类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入报修标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入问题描述', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  address: [{ required: true, message: '请输入报修地址', trigger: 'blur' }]
}

const getRepairTypeName = (type) => {
  const names = { 1: '水表故障', 2: '漏水', 3: '水质问题', 4: '水压异常', 5: '其他' }
  return names[type] || '未知'
}

const getRepairTypeTag = (type) => {
  const types = { 1: 'danger', 2: 'warning', 3: 'info', 4: 'primary', 5: '' }
  return types[type] || 'info'
}

const getPriorityName = (priority) => {
  const names = { 1: '紧急', 2: '普通', 3: '低' }
  return names[priority] || '普通'
}

const getPriorityType = (priority) => {
  const types = { 1: 'danger', 2: 'warning', 3: 'success' }
  return types[priority] || 'warning'
}

const getStatusName = (status) => {
  const names = { 0: '待处理', 1: '处理中', 2: '已完成', 3: '已取消' }
  return names[status] || '未知'
}

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }
  return types[status] || 'info'
}

const getStatusClass = (status) => {
  const classes = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }
  return classes[status] || ''
}

const loadMeterList = async () => {
  try {
    const res = await getMyMeters()
    if (res.code === 200) {
      meterList.value = res.data
    }
  } catch (error) {
    console.error('加载水表列表失败', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRepairOrderPage({
      page: pagination.current,
      size: pagination.size,
      userId: userStore.userInfo?.userId,
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
  searchForm.orderNo = ''
  searchForm.repairType = null
  searchForm.status = null
  handleSearch()
}

const handleCreate = () => {
  loadMeterList()
  Object.assign(createFormData, {
    repairType: null,
    meterId: null,
    title: '',
    description: '',
    priority: 2,
    contactName: userStore.userInfo?.realName || '',
    contactPhone: userStore.userInfo?.phone || '',
    address: userStore.userInfo?.address || ''
  })
  createDialogVisible.value = true
}

const handleCreateSubmit = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const res = await createRepairOrder(createFormData)
        if (res.code === 200) {
          ElMessage.success('报修提交成功')
          createDialogVisible.value = false
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

const handleCreateDialogClose = () => {
  createFormRef.value?.resetFields()
}

const handleView = async (row) => {
  try {
    const res = await getRepairOrderById(row.orderId)
    if (res.code === 200 && res.data) {
      Object.assign(currentOrder, res.data)
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error(error)
  }
}

const handleFeedback = (row) => {
  feedbackForm.orderId = row.orderId
  feedbackForm.feedback = ''
  feedbackForm.rating = 5
  feedbackDialogVisible.value = true
}

const handleFeedbackSubmit = async () => {
  if (!feedbackForm.rating) {
    ElMessage.warning('请选择评分')
    return
  }
  
  submitLoading.value = true
  try {
    const res = await feedbackOrder(feedbackForm.orderId, feedbackForm.feedback, feedbackForm.rating)
    if (res.code === 200) {
      ElMessage.success('评价提交成功')
      feedbackDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定取消该报修工单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await cancelOrder(row.orderId)
    if (res.code === 200) {
      ElMessage.success('工单已取消')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
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

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 24px;
  border-radius: 12px;
  color: white;
  
  .header-info {
    h2 {
      font-size: 22px;
      font-weight: 600;
      margin-bottom: 6px;
    }
    
    p {
      font-size: 14px;
      opacity: 0.85;
    }
  }
  
  .el-button {
    background: rgba(255, 255, 255, 0.2);
    border-color: rgba(255, 255, 255, 0.3);
    color: white;
    
    &:hover {
      background: rgba(255, 255, 255, 0.3);
      border-color: rgba(255, 255, 255, 0.4);
    }
  }
}

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

.table-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}

.order-no {
  font-family: 'Monaco', 'Consolas', monospace;
  color: #11998e;
  font-weight: 500;
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
    
    &.primary {
      background-color: #409eff;
      box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.2);
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

.text-secondary {
  color: var(--text-secondary);
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 4px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid var(--border-color-light);
}

.dialog-form {
  padding: 10px 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.description-text {
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.6;
}

.result-text {
  color: #52c41a;
  background: #f6ffed;
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #b7eb8f;
}

.feedback-form {
  .rating-section {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    
    .label {
      margin-right: 12px;
      font-weight: 500;
    }
  }
  
  .feedback-section {
    .label {
      display: block;
      margin-bottom: 8px;
      font-weight: 500;
    }
  }
}

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
