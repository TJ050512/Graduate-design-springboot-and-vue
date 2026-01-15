<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
      <div class="header-info">
        <h2>公告管理</h2>
        <p>发布和管理系统公告，向用户传达重要信息</p>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加公告
      </el-button>
    </div>
    
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="标题">
          <el-input
            v-model="searchForm.title"
            placeholder="请输入标题"
            clearable
            :prefix-icon="Search"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="公告类型">
          <el-select v-model="searchForm.noticeType" placeholder="全部类型" clearable style="width: 140px">
            <el-option label="系统公告" :value="1" />
            <el-option label="停水通知" :value="2" />
            <el-option label="价格调整" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已撤回" :value="2" />
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
        <el-table-column prop="noticeId" label="ID" width="70" align="center" />
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <div class="notice-title">
              <el-icon v-if="row.isTop === 1" class="top-icon"><Top /></el-icon>
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="noticeType" label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="getNoticeTypeTag(row.noticeType)" 
              effect="light"
              class="type-tag"
            >
              <el-icon class="tag-icon"><component :is="getNoticeTypeIcon(row.noticeType)" /></el-icon>
              {{ getNoticeTypeName(row.noticeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isTop" label="置顶" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isTop === 1" type="danger" effect="dark" size="small">
              <el-icon><Top /></el-icon>
              置顶
            </el-tag>
            <span v-else class="text-secondary">-</span>
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
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" link @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button type="warning" link @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button
                v-if="row.status === 0"
                type="success"
                link
                @click="handlePublish(row)"
              >
                <el-icon><Promotion /></el-icon>
                发布
              </el-button>
              <el-button
                v-else-if="row.status === 1"
                type="info"
                link
                @click="handleRevoke(row)"
              >
                <el-icon><RefreshLeft /></el-icon>
                撤回
              </el-button>
              <el-button type="danger" link @click="handleDelete(row)">
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
      width="800px"
      destroy-on-close
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        class="dialog-form"
      >
        <el-form-item label="标题" prop="title">
          <el-input 
            v-model="formData.title" 
            placeholder="请输入公告标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="公告类型" prop="noticeType">
          <el-radio-group v-model="formData.noticeType" class="type-radio-group">
            <el-radio-button :value="1">
              <el-icon><Bell /></el-icon>
              系统公告
            </el-radio-button>
            <el-radio-button :value="2">
              <el-icon><WarningFilled /></el-icon>
              停水通知
            </el-radio-button>
            <el-radio-button :value="3">
              <el-icon><PriceTag /></el-icon>
              价格调整
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="10"
            placeholder="请输入公告内容"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否置顶">
              <el-switch
                v-model="formData.isTop"
                :active-value="1"
                :inactive-value="0"
                active-text="置顶"
                inactive-text="普通"
                inline-prompt
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :value="0">草稿</el-radio>
                <el-radio :value="1">发布</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="公告详情"
      width="700px"
    >
      <div class="notice-detail">
        <div class="detail-header">
          <div class="detail-title">
            <el-tag v-if="currentNotice.isTop === 1" type="danger" size="small" effect="dark">
              <el-icon><Top /></el-icon>
              置顶
            </el-tag>
            <h3>{{ currentNotice.title }}</h3>
          </div>
          <div class="detail-meta">
            <el-tag :type="getNoticeTypeTag(currentNotice.noticeType)" effect="light">
              {{ getNoticeTypeName(currentNotice.noticeType) }}
            </el-tag>
            <el-tag :type="getStatusType(currentNotice.status)" effect="light">
              {{ getStatusName(currentNotice.status) }}
            </el-tag>
            <span class="create-time">
              <el-icon><Clock /></el-icon>
              {{ currentNotice.createTime }}
            </span>
          </div>
        </div>
        <el-divider />
        <div class="detail-content">
          {{ currentNotice.content }}
        </div>
      </div>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, markRaw } from 'vue'
import { useUserStore } from '@/stores/user'
import {
  getNoticePage,
  addNotice,
  updateNotice,
  publishNotice,
  deleteNotice,
  getNoticeById
} from '@/api/notice'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  Refresh,
  View,
  Edit,
  Delete,
  Bell,
  Top,
  Promotion,
  RefreshLeft,
  WarningFilled,
  PriceTag,
  Clock
} from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogTitle = ref('添加公告')
const formRef = ref(null)
const userStore = useUserStore()

const searchForm = reactive({
  title: '',
  noticeType: null,
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  noticeId: null,
  title: '',
  content: '',
  noticeType: 1,
  publisherId: null,
  status: 0,
  isTop: 0
})

const currentNotice = reactive({
  noticeId: null,
  title: '',
  content: '',
  noticeType: 1,
  status: 0,
  isTop: 0,
  createTime: ''
})

const formRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择公告类型', trigger: 'change' }]
}

const getNoticeTypeName = (type) => {
  const names = { 1: '系统公告', 2: '停水通知', 3: '价格调整' }
  return names[type] || '未知'
}

const getNoticeTypeTag = (type) => {
  const tags = { 1: 'primary', 2: 'warning', 3: 'danger' }
  return tags[type] || 'info'
}

const getNoticeTypeIcon = (type) => {
  const icons = { 
    1: markRaw(Bell), 
    2: markRaw(WarningFilled), 
    3: markRaw(PriceTag) 
  }
  return icons[type] || markRaw(Bell)
}

const getStatusName = (status) => {
  const names = { 0: '草稿', 1: '已发布', 2: '已撤回' }
  return names[status] || '未知'
}

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'warning' }
  return types[status] || 'info'
}

const getStatusClass = (status) => {
  const classes = { 0: 'info', 1: 'success', 2: 'warning' }
  return classes[status] || ''
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getNoticePage({
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
  searchForm.title = ''
  searchForm.noticeType = null
  searchForm.status = null
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '添加公告'
  Object.assign(formData, {
    noticeId: null,
    title: '',
    content: '',
    noticeType: 1,
    publisherId: userStore.userInfo?.userId || null,
    status: 0,
    isTop: 0
  })
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑公告'
  try {
    const res = await getNoticeById(row.noticeId)
    if (res.code === 200 && res.data) {
      Object.assign(formData, res.data)
      dialogVisible.value = true
    }
  } catch (error) {
    console.error(error)
  }
}

const handleView = async (row) => {
  try {
    const res = await getNoticeById(row.noticeId)
    if (res.code === 200 && res.data) {
      Object.assign(currentNotice, res.data)
      viewDialogVisible.value = true
    }
  } catch (error) {
    console.error(error)
  }
}

const handlePublish = (row) => {
  ElMessageBox.confirm('确定要发布该公告吗？', '发布确认', {
    type: 'warning',
    confirmButtonText: '确认发布',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      const res = await publishNotice(row.noticeId)
      if (res.code === 200) {
        ElMessage.success('发布成功')
        loadData()
      }
    } catch (error) {
      console.error(error)
    }
  })
}

const handleRevoke = (row) => {
  ElMessageBox.confirm('确定要撤回该公告吗？', '撤回确认', {
    type: 'warning',
    confirmButtonText: '确认撤回',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      const updateData = {
        noticeId: row.noticeId,
        status: 2
      }
      const res = await updateNotice(updateData)
      if (res.code === 200) {
        ElMessage.success('撤回成功')
        loadData()
      }
    } catch (error) {
      console.error(error)
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除公告「${row.title}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    try {
      const res = await deleteNotice(row.noticeId)
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
      submitLoading.value = true
      try {
        let res
        if (formData.noticeId) {
          res = await updateNotice(formData)
        } else {
          res = await addNotice(formData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.noticeId ? '更新成功' : '添加成功')
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

// 页面头部
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

.notice-title {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .top-icon {
    color: #f56c6c;
    font-size: 16px;
  }
}

.type-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  
  .tag-icon {
    font-size: 12px;
  }
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  
  .status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    
    &.info {
      background-color: #909399;
      box-shadow: 0 0 0 3px rgba(144, 147, 153, 0.2);
    }
    
    &.success {
      background-color: #52c41a;
      box-shadow: 0 0 0 3px rgba(82, 196, 26, 0.2);
    }
    
    &.warning {
      background-color: #e6a23c;
      box-shadow: 0 0 0 3px rgba(230, 162, 60, 0.2);
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
  flex-wrap: wrap;
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
  
  .type-radio-group {
    :deep(.el-radio-button__inner) {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

// 公告详情
.notice-detail {
  .detail-header {
    .detail-title {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 12px;
      
      h3 {
        font-size: 20px;
        font-weight: 600;
        color: var(--text-primary);
      }
    }
    
    .detail-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .create-time {
        display: flex;
        align-items: center;
        gap: 4px;
        color: var(--text-secondary);
        font-size: 13px;
      }
    }
  }
  
  .detail-content {
    white-space: pre-wrap;
    line-height: 1.8;
    color: var(--text-regular);
    padding: 16px;
    background: var(--bg-color);
    border-radius: 8px;
    min-height: 200px;
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
