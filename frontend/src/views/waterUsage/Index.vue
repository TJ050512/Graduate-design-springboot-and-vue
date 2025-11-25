<template>
  <div class="water-usage-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用水记录管理</span>
          <el-button type="primary" @click="handleAdd">添加用水记录</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户">
          <el-select
            v-model="searchForm.userId"
            placeholder="请选择用户"
            clearable
            filterable
            style="width: 200px"
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
            style="width: 200px"
          >
            <el-option
              v-for="meter in meterList"
              :key="meter.meterId"
              :label="`${meter.meterNo}(${meter.installAddress})`"
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
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="待确认" :value="0" />
            <el-option label="已确认" :value="1" />
            <el-option label="已缴费" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="usageId" label="ID" width="80" />
        <el-table-column prop="readMonth" label="抄表月份" width="120" />
        <el-table-column prop="readDate" label="抄表日期" width="120" />
        <el-table-column label="水表信息" width="150">
          <template #default="{ row }">
            <div v-if="getMeterInfo(row.meterId)">
              {{ getMeterInfo(row.meterId).meterNo }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用户信息" width="150">
          <template #default="{ row }">
            <div v-if="getUserInfo(row.userId)">
              {{ getUserInfo(row.userId).realName }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="lastReading" label="上次读数" width="100" />
        <el-table-column prop="currentReading" label="本次读数" width="100" />
        <el-table-column prop="usage" label="用水量(立方米)" width="120" />
        <el-table-column prop="price" label="单价(元)" width="100" />
        <el-table-column prop="amount" label="应缴金额(元)" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">待确认</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已确认</el-tag>
            <el-tag v-else type="info">已缴费</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              size="small"
              @click="handleConfirm(row)"
            >
              确认
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
      width="700px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
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
        <el-form-item label="上次读数" prop="lastReading">
          <el-input-number
            v-model="formData.lastReading"
            :precision="2"
            :min="0"
            style="width: 100%"
            @change="calculateUsage"
          />
        </el-form-item>
        <el-form-item label="本次读数" prop="currentReading">
          <el-input-number
            v-model="formData.currentReading"
            :precision="2"
            :min="0"
            style="width: 100%"
            @change="calculateUsage"
          />
        </el-form-item>
        <el-form-item label="用水量(立方米)">
          <el-input-number
            v-model="formData.usage"
            :precision="2"
            :min="0"
            :disabled="true"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="水费单价(元/立方米)" prop="price">
          <el-input-number
            v-model="formData.price"
            :precision="2"
            :min="0"
            style="width: 100%"
            @change="calculateAmount"
          />
        </el-form-item>
        <el-form-item label="应缴金额(元)">
          <el-input-number
            v-model="formData.amount"
            :precision="2"
            :min="0"
            :disabled="true"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getUsagePage, addUsage, updateUsage, confirmUsage, deleteUsage, getUsageById } from '@/api/waterUsage'
import { getUserPage } from '@/api/user'
import { getMeterPage } from '@/api/waterMeter'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
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

// 获取用户信息
const getUserInfo = (userId) => {
  return userList.value.find(u => u.userId === userId)
}

// 获取水表信息
const getMeterInfo = (meterId) => {
  return meterList.value.find(m => m.meterId === meterId)
}

// 加载用户列表
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

// 加载水表列表
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

// 计算用水量
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

// 计算应缴金额
const calculateAmount = () => {
  if (formData.usage && formData.price) {
    formData.amount = parseFloat((formData.usage * formData.price).toFixed(2))
  }
}

// 水表改变时，自动填充用户和上次读数
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
    price: 2.8, // 默认单价
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
  ElMessageBox.confirm('确定要确认该用水记录吗？', '提示', {
    type: 'warning'
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
  ElMessageBox.confirm('确定要删除该用水记录吗？', '提示', {
    type: 'warning'
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
      // 验证本次读数必须大于上次读数
      if (formData.currentReading <= formData.lastReading) {
        ElMessage.error('本次读数必须大于上次读数')
        return
      }
      
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
  loadUserList()
  loadMeterList()
  loadData()
})
</script>

<style scoped lang="scss">
.water-usage-management {
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
