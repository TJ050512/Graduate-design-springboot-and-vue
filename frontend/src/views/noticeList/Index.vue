<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
      <div class="header-info">
        <h2>系统公告</h2>
        <p>查看系统发布的最新公告和通知</p>
      </div>
    </div>
    
    <!-- 公告列表 -->
    <div class="notice-list" v-loading="loading">
      <el-empty v-if="noticeList.length === 0" description="暂无公告" />
      
      <div 
        class="notice-item" 
        v-for="notice in noticeList" 
        :key="notice.noticeId"
        @click="handleView(notice)"
      >
        <div class="notice-header">
          <div class="notice-tags">
            <el-tag v-if="notice.isTop === 1" type="danger" size="small" effect="dark">
              <el-icon><Top /></el-icon>
              置顶
            </el-tag>
            <el-tag :type="getNoticeTypeTag(notice.noticeType)" effect="light">
              {{ getNoticeTypeName(notice.noticeType) }}
            </el-tag>
          </div>
          <span class="notice-time">
            <el-icon><Clock /></el-icon>
            {{ notice.createTime }}
          </span>
        </div>
        <h3 class="notice-title">{{ notice.title }}</h3>
        <p class="notice-preview">{{ getPreviewContent(notice.content) }}</p>
      </div>
    </div>
    
    <!-- 分页 -->
    <div class="pagination-container" v-if="noticeList.length > 0">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, prev, pager, next"
        background
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 公告详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="currentNotice.title"
      width="700px"
    >
      <div class="notice-detail">
        <div class="detail-meta">
          <el-tag :type="getNoticeTypeTag(currentNotice.noticeType)" effect="light">
            {{ getNoticeTypeName(currentNotice.noticeType) }}
          </el-tag>
          <span class="detail-time">
            <el-icon><Clock /></el-icon>
            {{ currentNotice.createTime }}
          </span>
        </div>
        <el-divider />
        <div class="detail-content">
          {{ currentNotice.content }}
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getNoticePage, getNoticeById } from '@/api/notice'
import {
  Top,
  Clock
} from '@element-plus/icons-vue'

const loading = ref(false)
const noticeList = ref([])
const detailDialogVisible = ref(false)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const currentNotice = reactive({
  noticeId: null,
  title: '',
  content: '',
  noticeType: 1,
  isTop: 0,
  createTime: ''
})

const getNoticeTypeName = (type) => {
  const names = { 1: '系统公告', 2: '停水通知', 3: '价格调整' }
  return names[type] || '公告'
}

const getNoticeTypeTag = (type) => {
  const tags = { 1: 'primary', 2: 'warning', 3: 'danger' }
  return tags[type] || 'info'
}

const getPreviewContent = (content) => {
  if (!content) return ''
  return content.length > 100 ? content.substring(0, 100) + '...' : content
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getNoticePage({
      page: pagination.current,
      size: pagination.size,
      status: 1  // 只查询已发布的公告
    })
    if (res.code === 200) {
      noticeList.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleView = async (notice) => {
  try {
    const res = await getNoticeById(notice.noticeId)
    if (res.code === 200 && res.data) {
      Object.assign(currentNotice, res.data)
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error(error)
  }
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
  color: white;
  
  .header-info {
    h2 { font-size: 22px; font-weight: 600; margin-bottom: 6px; }
    p { font-size: 14px; opacity: 0.85; }
  }
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notice-item {
  background: white;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
  
  .notice-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    
    .notice-tags {
      display: flex;
      gap: 8px;
    }
    
    .notice-time {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: #909399;
    }
  }
  
  .notice-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 10px;
    line-height: 1.4;
  }
  
  .notice-preview {
    font-size: 14px;
    color: #606266;
    line-height: 1.6;
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.notice-detail {
  .detail-meta {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .detail-time {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: #909399;
    }
  }
  
  .detail-content {
    white-space: pre-wrap;
    line-height: 1.8;
    color: #606266;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 8px;
    min-height: 150px;
  }
}
</style>
