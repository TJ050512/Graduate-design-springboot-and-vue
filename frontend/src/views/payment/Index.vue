<template>
  <div class="payment-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>缴费管理</span>
          <el-button type="primary" @click="handleCreate">创建缴费单</el-button>
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
        <el-form-item label="缴费单号">
          <el-input
            v-model="searchForm.paymentNo"
            placeholder="请输入缴费单号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="searchForm.paymentMethod" placeholder="请选择" clearable>
            <el-option label="现金" :value="1" />
            <el-option label="微信" :value="2" />
            <el-option label="支付宝" :value="3" />
            <el-option label="银行卡" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="已退款" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="paymentId" label="ID" width="80" />
        <el-table-column prop="paymentNo" label="缴费单号" width="180" />
        <el-table-column label="用户信息" width="150">
          <template #default="{ row }">
            <div v-if="getUserInfo(row.userId)">
              {{ getUserInfo(row.userId).realName }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用水记录" width="120">
          <template #default="{ row }">
            <el-button
              v-if="row.usageId"
              type="text"
              size="small"
              @click="viewUsageDetail(row.usageId)"
            >
              查看详情
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="缴费金额(元)" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.paymentMethod === 1" type="info">现金</el-tag>
            <el-tag v-else-if="row.paymentMethod === 2" type="success">微信</el-tag>
            <el-tag v-else-if="row.paymentMethod === 3" type="primary">支付宝</el-tag>
            <el-tag v-else-if="row.paymentMethod === 4" type="warning">银行卡</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentTime" label="缴费时间" width="180">
          <template #default="{ row }">
            {{ row.paymentTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="transactionNo" label="交易流水号" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">待支付</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已支付</el-tag>
            <el-tag v-else type="danger">已退款</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="success"
              size="small"
              @click="handlePay(row)"
            >
              支付
            </el-button>
            <el-button type="primary" size="small" @click="handleView(row)">详情</el-button>
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
    
    <!-- 创建缴费单对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建缴费单"
      width="600px"
      @close="handleCreateDialogClose"
    >
      <el-form
        ref="createFormRef"
        :model="createFormData"
        :rules="createFormRules"
        label-width="120px"
      >
        <el-form-item label="用水记录" prop="usageId">
          <el-select
            v-model="createFormData.usageId"
            placeholder="请选择用水记录"
            filterable
            style="width: 100%"
            @change="handleUsageChange"
          >
            <el-option
              v-for="usage in usageList"
              :key="usage.usageId"
              :label="`${usage.readMonth} - 用户:${getUserInfo(usage.userId)?.realName || ''} - 金额:¥${usage.amount}`"
              :value="usage.usageId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="用户">
          <el-input
            :value="getUserInfo(createFormData.userId)?.realName || ''"
            disabled
          />
        </el-form-item>
        <el-form-item label="水表">
          <el-input
            :value="getMeterInfo(createFormData.meterId)?.meterNo || ''"
            disabled
          />
        </el-form-item>
        <el-form-item label="缴费金额(元)">
          <el-input-number
            v-model="createFormData.amount"
            :precision="2"
            :min="0"
            :disabled="true"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-radio-group v-model="createFormData.paymentMethod">
            <el-radio :label="1">现金</el-radio>
            <el-radio :label="2">微信</el-radio>
            <el-radio :label="3">支付宝</el-radio>
            <el-radio :label="4">银行卡</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 支付对话框 -->
    <el-dialog
      v-model="payDialogVisible"
      title="确认支付"
      width="500px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="缴费单号">{{ currentPayment.paymentNo }}</el-descriptions-item>
        <el-descriptions-item label="缴费金额">
          <span style="color: #f56c6c; font-size: 18px; font-weight: bold;">¥{{ currentPayment.amount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">
          <el-tag v-if="currentPayment.paymentMethod === 1" type="info">现金</el-tag>
          <el-tag v-else-if="currentPayment.paymentMethod === 2" type="success">微信</el-tag>
          <el-tag v-else-if="currentPayment.paymentMethod === 3" type="primary">支付宝</el-tag>
          <el-tag v-else-if="currentPayment.paymentMethod === 4" type="warning">银行卡</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户">
          {{ getUserInfo(currentPayment.userId)?.realName || '' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="success" @click="handlePayConfirm">确认支付</el-button>
      </template>
    </el-dialog>
    
    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="缴费详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="缴费单号">{{ currentPayment.paymentNo }}</el-descriptions-item>
        <el-descriptions-item label="缴费金额">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ currentPayment.amount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="用户">
          {{ getUserInfo(currentPayment.userId)?.realName || '' }}
        </el-descriptions-item>
        <el-descriptions-item label="水表">
          {{ getMeterInfo(currentPayment.meterId)?.meterNo || '' }}
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">
          <el-tag v-if="currentPayment.paymentMethod === 1" type="info">现金</el-tag>
          <el-tag v-else-if="currentPayment.paymentMethod === 2" type="success">微信</el-tag>
          <el-tag v-else-if="currentPayment.paymentMethod === 3" type="primary">支付宝</el-tag>
          <el-tag v-else-if="currentPayment.paymentMethod === 4" type="warning">银行卡</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentPayment.status === 0" type="warning">待支付</el-tag>
          <el-tag v-else-if="currentPayment.status === 1" type="success">已支付</el-tag>
          <el-tag v-else type="danger">已退款</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="缴费时间">
          {{ currentPayment.paymentTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="交易流水号">
          {{ currentPayment.transactionNo || '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getPaymentPage, createPayment, pay, getPaymentById } from '@/api/payment'
import { getUsagePage } from '@/api/waterUsage'
import { getUserPage } from '@/api/user'
import { getMeterPage } from '@/api/waterMeter'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const createDialogVisible = ref(false)
const payDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const createFormRef = ref(null)
const userList = ref([])
const meterList = ref([])
const usageList = ref([])

const searchForm = reactive({
  userId: null,
  paymentNo: '',
  paymentMethod: null,
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const createFormData = reactive({
  usageId: null,
  userId: null,
  meterId: null,
  amount: 0,
  paymentMethod: 1
})

const currentPayment = reactive({
  paymentId: null,
  paymentNo: '',
  userId: null,
  meterId: null,
  amount: 0,
  paymentMethod: 1,
  status: 0,
  paymentTime: '',
  transactionNo: ''
})

const createFormRules = {
  usageId: [{ required: true, message: '请选择用水记录', trigger: 'change' }],
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
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

// 加载可用用水记录（已确认但未缴费的）
const loadUsageList = async () => {
  try {
    const res = await getUsagePage({ page: 1, size: 1000, status: 1 })
    if (res.code === 200) {
      usageList.value = res.data.records.filter(usage => {
        // 只显示已确认但未缴费的记录（status=1）
        return usage.status === 1
      })
    }
  } catch (error) {
    console.error('加载用水记录列表失败', error)
  }
}

// 用水记录改变时，自动填充相关信息
const handleUsageChange = (usageId) => {
  const usage = usageList.value.find(u => u.usageId === usageId)
  if (usage) {
    createFormData.userId = usage.userId
    createFormData.meterId = usage.meterId
    createFormData.amount = usage.amount
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPaymentPage({
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
  searchForm.paymentNo = ''
  searchForm.paymentMethod = null
  searchForm.status = null
  handleSearch()
}

const handleCreate = () => {
  loadUsageList()
  Object.assign(createFormData, {
    usageId: null,
    userId: null,
    meterId: null,
    amount: 0,
    paymentMethod: 1
  })
  createDialogVisible.value = true
}

const handleCreateSubmit = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await createPayment(createFormData)
        if (res.code === 200) {
          ElMessage.success('创建缴费单成功')
          createDialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error(error)
      }
    }
  })
}

const handleCreateDialogClose = () => {
  createFormRef.value?.resetFields()
}

const handlePay = (row) => {
  Object.assign(currentPayment, { ...row })
  payDialogVisible.value = true
}

const handlePayConfirm = async () => {
  try {
    const res = await pay(currentPayment.paymentId)
    if (res.code === 200) {
      ElMessage.success('支付成功')
      payDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error(error)
  }
}

const handleView = async (row) => {
  try {
    const res = await getPaymentById(row.paymentId)
    if (res.code === 200 && res.data) {
      Object.assign(currentPayment, res.data)
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error(error)
  }
}

const viewUsageDetail = (usageId) => {
  ElMessage.info('用水记录详情功能待实现')
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
.payment-management {
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
