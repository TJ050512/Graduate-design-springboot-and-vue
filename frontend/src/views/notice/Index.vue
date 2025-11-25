<template>
  <div class="notice-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公告管理</span>
          <el-button type="primary" @click="handleAdd">添加公告</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="标题">
          <el-input
            v-model="searchForm.title"
            placeholder="请输入标题"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="公告类型">
          <el-select v-model="searchForm.noticeType" placeholder="请选择" clearable>
            <el-option label="系统公告" :value="1" />
            <el-option label="停水通知" :value="2" />
            <el-option label="价格调整" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已撤回" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="noticeId" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="noticeType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.noticeType === 1" type="primary">系统公告</el-tag>
            <el-tag v-else-if="row.noticeType === 2" type="warning">停水通知</el-tag>
            <el-tag v-else-if="row.noticeType === 3" type="danger">价格调整</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isTop" label="置顶" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isTop === 1" type="danger">置顶</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="info">草稿</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已发布</el-tag>
            <el-tag v-else type="warning">已撤回</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
              <el-button type="warning" size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button
                v-if="row.status === 0"
                type="success"
                size="small"
                @click="handlePublish(row)"
              >
                发布
              </el-button>
              <el-button
                v-else-if="row.status === 1"
                type="info"
                size="small"
                @click="handleRevoke(row)"
              >
                撤回
              </el-button>
              <el-divider direction="vertical" style="margin: 0 5px; height: 20px;" />
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告类型" prop="noticeType">
          <el-radio-group v-model="formData.noticeType">
            <el-radio :label="1">系统公告</el-radio>
            <el-radio :label="2">停水通知</el-radio>
            <el-radio :label="3">价格调整</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="10"
            placeholder="请输入公告内容"
          />
        </el-form-item>
        <el-form-item label="是否置顶">
          <el-switch
            v-model="formData.isTop"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">已发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="公告详情"
      width="700px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="标题">
          <strong>{{ currentNotice.title }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag v-if="currentNotice.noticeType === 1" type="primary">系统公告</el-tag>
          <el-tag v-else-if="currentNotice.noticeType === 2" type="warning">停水通知</el-tag>
          <el-tag v-else-if="currentNotice.noticeType === 3" type="danger">价格调整</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentNotice.status === 0" type="info">草稿</el-tag>
          <el-tag v-else-if="currentNotice.status === 1" type="success">已发布</el-tag>
          <el-tag v-else type="warning">已撤回</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="是否置顶">
          <el-tag v-if="currentNotice.isTop === 1" type="danger">置顶</el-tag>
          <span v-else>否</span>
        </el-descriptions-item>
        <el-descriptions-item label="内容">
          <div style="white-space: pre-wrap; line-height: 1.6;">{{ currentNotice.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ currentNotice.createTime }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
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

const loading = ref(false)
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
  ElMessageBox.confirm('确定要发布该公告吗？', '提示', {
    type: 'warning'
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
  ElMessageBox.confirm('确定要撤回该公告吗？', '提示', {
    type: 'warning'
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
  ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
    type: 'warning'
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
.notice-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .search-form {
    margin-bottom: 20px;
  }
}
</style>
