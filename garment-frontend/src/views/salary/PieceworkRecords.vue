<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            :shortcuts="dateShortcuts"
          />
        </el-form-item>
        <el-form-item label="员工">
          <el-select v-model="searchForm.workerId" placeholder="请选择员工" clearable filterable>
            <el-option
              v-for="item in workerList"
              :key="item.id"
              :label="`${item.name} (${item.employeeNo})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="车间">
          <el-select v-model="searchForm.workshopId" placeholder="请选择车间" clearable @change="onWorkshopChange">
            <el-option
              v-for="item in workshopList"
              :key="item.id"
              :label="item.workshopName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="工序">
          <el-select v-model="searchForm.processId" placeholder="请选择工序" clearable>
            <el-option
              v-for="item in processList"
              :key="item.id"
              :label="item.processName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="结算状态">
          <el-select v-model="searchForm.settlementStatus" placeholder="请选择状态" clearable>
            <el-option label="未结算" value="pending" />
            <el-option label="已结算" value="settled" />
            <el-option label="已锁账" value="locked" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-label">总记录数</div>
          <div class="stat-value">{{ statistics.totalRecords }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-label">总件数</div>
          <div class="stat-value">{{ statistics.totalQuantity.toLocaleString() }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-label">总金额</div>
          <div class="stat-value text-danger">¥{{ statistics.totalAmount.toFixed(2) }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-label">平均单价</div>
          <div class="stat-value">¥{{ statistics.avgPrice.toFixed(2) }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 表格工具栏 -->
    <div class="table-toolbar">
      <div class="toolbar-left">
        <el-button type="success" :disabled="!selectedRows.length" @click="handleBatchSettle">
          <el-icon><Check /></el-icon>
          批量结算
        </el-button>
        <el-button type="warning" :disabled="!selectedRows.length" @click="handleBatchAdjust">
          <el-icon><Edit /></el-icon>
          批量调整
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-button :icon="Refresh" circle @click="fetchData" />
      </div>
    </div>

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      stripe
      border
      show-summary
      :summary-method="getSummaries"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" :selectable="checkSelectable" />
      <el-table-column type="index" width="60" label="序号" />
      <el-table-column prop="workDate" label="工作日期" width="110" />
      <el-table-column prop="workerName" label="员工" width="100">
        <template #default="{ row }">
          <el-tooltip :content="`工号: ${row.employeeNo}`" placement="top">
            <span>{{ row.workerName }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="workshopName" label="车间" width="100" />
      <el-table-column prop="bundleNo" label="包号" width="100">
        <template #default="{ row }">
          <el-link type="primary" @click="handleViewBundle(row)">{{ row.bundleNo }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="processName" label="工序" width="100" />
      <el-table-column prop="styleName" label="款式" min-width="150" />
      <el-table-column prop="quantity" label="数量" width="80" align="center">
        <template #default="{ row }">
          <span class="quantity-text">{{ row.quantity }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="unitPrice" label="单价" width="100">
        <template #default="{ row }">
          ¥{{ row.unitPrice.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="小计" width="100">
        <template #default="{ row }">
          <span class="amount-text">¥{{ row.amount.toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="加扣项" width="120">
        <template #default="{ row }">
          <div class="adjust-info">
            <span v-if="row.bonus > 0" class="bonus">+{{ row.bonus.toFixed(2) }}</span>
            <span v-if="row.penalty > 0" class="penalty">-{{ row.penalty.toFixed(2) }}</span>
            <span v-if="!row.bonus && !row.penalty">-</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="finalAmount" label="实际金额" width="120">
        <template #default="{ row }">
          <span class="final-amount">¥{{ row.finalAmount.toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="settlementStatus" label="结算状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.settlementStatus)" size="small">
            {{ getStatusText(row.settlementStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="periodName" label="结算期" width="120">
        <template #default="{ row }">
          {{ row.periodName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">详情</el-button>
          <el-button link type="primary" @click="handleAdjust(row)" v-if="row.settlementStatus === 'pending'">调整</el-button>
          <el-button link type="danger" @click="handleDelete(row)" v-if="row.settlementStatus === 'pending'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pageParams.current"
        v-model:page-size="pageParams.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 调整对话框 -->
    <el-dialog
      v-model="adjustDialogVisible"
      title="调整计件记录"
      width="500px"
    >
      <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-width="100px">
        <el-form-item label="原始金额">
          <el-input :value="`¥${currentRecord?.amount?.toFixed(2) || '0.00'}`" disabled />
        </el-form-item>
        <el-form-item label="奖金" prop="bonus">
          <el-input-number
            v-model="adjustForm.bonus"
            :min="0"
            :precision="2"
            :step="10"
            placeholder="请输入奖金金额"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="扣款" prop="penalty">
          <el-input-number
            v-model="adjustForm.penalty"
            :min="0"
            :precision="2"
            :step="10"
            placeholder="请输入扣款金额"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="调整原因" prop="reason" required>
          <el-input
            v-model="adjustForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入调整原因"
          />
        </el-form-item>
        <el-form-item label="最终金额">
          <el-input :value="`¥${calculateFinalAmount()}`" disabled />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="adjustDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitAdjust">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Check, Edit, Download, Refresh } from '@element-plus/icons-vue'
import type { PieceworkRecord } from '@/types/api'
import dayjs from 'dayjs'

// 搜索表单
const searchForm = reactive({
  dateRange: [],
  workerId: null,
  workshopId: null,
  processId: null,
  settlementStatus: ''
})

// 分页参数
const pageParams = reactive({
  current: 1,
  size: 20
})

// 统计数据
const statistics = ref({
  totalRecords: 0,
  totalQuantity: 0,
  totalAmount: 0,
  avgPrice: 0
})

// 调整表单
const adjustForm = ref({
  bonus: 0,
  penalty: 0,
  reason: ''
})

const adjustRules: FormRules = {
  reason: [
    { required: true, message: '请输入调整原因', trigger: 'blur' }
  ]
}

// 状态变量
const loading = ref(false)
const adjustDialogVisible = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const selectedRows = ref<any[]>([])
const currentRecord = ref<any>()
const adjustFormRef = ref<FormInstance>()

// 日期快捷选项
const dateShortcuts = [
  {
    text: '今天',
    value: () => {
      const today = new Date()
      return [today, today]
    }
  },
  {
    text: '昨天',
    value: () => {
      const yesterday = new Date()
      yesterday.setDate(yesterday.getDate() - 1)
      return [yesterday, yesterday]
    }
  },
  {
    text: '本周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - start.getDay())
      return [start, end]
    }
  },
  {
    text: '本月',
    value: () => {
      const end = new Date()
      const start = new Date(end.getFullYear(), end.getMonth(), 1)
      return [start, end]
    }
  },
  {
    text: '上月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 1)
      start.setDate(1)
      end.setDate(0)
      return [start, end]
    }
  }
]

// 员工列表（实际从API获取）
const workerList = ref([
  { id: 1, name: '张三', employeeNo: 'EMP001' },
  { id: 2, name: '李四', employeeNo: 'EMP002' },
  { id: 3, name: '王五', employeeNo: 'EMP003' }
])

// 车间列表
const workshopList = ref([
  { id: 1, workshopName: '一车间' },
  { id: 2, workshopName: '二车间' },
  { id: 3, workshopName: '三车间' }
])

// 工序列表
const processList = ref([
  { id: 1, processName: '裁剪' },
  { id: 2, processName: '缝合' },
  { id: 3, processName: '锁边' },
  { id: 4, processName: '熨烫' }
])

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 模拟API调用
    setTimeout(() => {
      const records = [
        {
          id: 1,
          workerId: 1,
          workerName: '张三',
          employeeNo: 'EMP001',
          workshopId: 1,
          workshopName: '一车间',
          bundleId: 1,
          bundleNo: 'BDL001',
          processId: 2,
          processName: '缝合',
          styleId: 1,
          styleName: '经典T恤-白色',
          quantity: 100,
          unitPrice: 1.20,
          amount: 120.00,
          bonus: 10.00,
          penalty: 0,
          finalAmount: 130.00,
          settlementStatus: 'pending',
          workDate: '2024-01-15',
          periodId: null,
          periodName: null
        },
        {
          id: 2,
          workerId: 1,
          workerName: '张三',
          employeeNo: 'EMP001',
          workshopId: 1,
          workshopName: '一车间',
          bundleId: 2,
          bundleNo: 'BDL002',
          processId: 2,
          processName: '缝合',
          styleId: 1,
          styleName: '经典T恤-黑色',
          quantity: 80,
          unitPrice: 1.20,
          amount: 96.00,
          bonus: 0,
          penalty: 5.00,
          finalAmount: 91.00,
          settlementStatus: 'pending',
          workDate: '2024-01-15',
          periodId: null,
          periodName: null
        },
        {
          id: 3,
          workerId: 2,
          workerName: '李四',
          employeeNo: 'EMP002',
          workshopId: 1,
          workshopName: '一车间',
          bundleId: 3,
          bundleNo: 'BDL003',
          processId: 1,
          processName: '裁剪',
          styleId: 2,
          styleName: '休闲裤-黑色',
          quantity: 150,
          unitPrice: 0.50,
          amount: 75.00,
          bonus: 0,
          penalty: 0,
          finalAmount: 75.00,
          settlementStatus: 'settled',
          workDate: '2024-01-14',
          periodId: 1,
          periodName: '2024年1月上半月'
        }
      ]
      
      tableData.value = records
      total.value = records.length
      
      // 计算统计数据
      statistics.value = {
        totalRecords: records.length,
        totalQuantity: records.reduce((sum, r) => sum + r.quantity, 0),
        totalAmount: records.reduce((sum, r) => sum + r.finalAmount, 0),
        avgPrice: records.length > 0 ? 
          records.reduce((sum, r) => sum + r.unitPrice, 0) / records.length : 0
      }
      
      loading.value = false
    }, 1000)
  } catch (error) {
    console.error('获取数据失败:', error)
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageParams.current = 1
  fetchData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    dateRange: [],
    workerId: null,
    workshopId: null,
    processId: null,
    settlementStatus: ''
  })
  handleSearch()
}

// 车间变化
const onWorkshopChange = () => {
  // 根据车间筛选员工列表
  console.log('车间变化:', searchForm.workshopId)
}

// 查看详情
const handleView = (row: any) => {
  ElMessage.info('查看详情功能开发中')
}

// 查看包详情
const handleViewBundle = (row: any) => {
  ElMessage.info(`查看包 ${row.bundleNo} 详情`)
}

// 调整记录
const handleAdjust = (row: any) => {
  currentRecord.value = row
  adjustForm.value = {
    bonus: row.bonus || 0,
    penalty: row.penalty || 0,
    reason: ''
  }
  adjustDialogVisible.value = true
}

// 批量调整
const handleBatchAdjust = () => {
  ElMessage.info('批量调整功能开发中')
}

// 提交调整
const handleSubmitAdjust = async () => {
  if (!adjustFormRef.value) return
  
  await adjustFormRef.value.validate((valid) => {
    if (valid) {
      // 调用调整API
      ElMessage.success('调整成功')
      adjustDialogVisible.value = false
      fetchData()
    }
  })
}

// 计算最终金额
const calculateFinalAmount = () => {
  const amount = currentRecord.value?.amount || 0
  const bonus = adjustForm.value.bonus || 0
  const penalty = adjustForm.value.penalty || 0
  return (amount + bonus - penalty).toFixed(2)
}

// 批量结算
const handleBatchSettle = () => {
  const ids = selectedRows.value.map(r => r.id)
  ElMessageBox.confirm(
    `确定要结算选中的 ${selectedRows.value.length} 条记录吗？`,
    '批量结算',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 调用批量结算API
    ElMessage.success('结算成功')
    fetchData()
  })
}

// 删除记录
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    '确定要删除此计件记录吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 调用删除API
    ElMessage.success('删除成功')
    fetchData()
  })
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 选择变化
const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

// 判断是否可选
const checkSelectable = (row: any) => {
  return row.settlementStatus === 'pending'
}

// 获取汇总数据
const getSummaries = (param: any) => {
  const { columns, data } = param
  const sums: string[] = []
  
  columns.forEach((column: any, index: number) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    
    if (['quantity', 'amount', 'finalAmount'].includes(column.property)) {
      const values = data.map((item: any) => Number(item[column.property]))
      if (!values.every((value: number) => isNaN(value))) {
        const total = values.reduce((prev: number, curr: number) => {
          const value = Number(curr)
          if (!isNaN(value)) {
            return prev + curr
          } else {
            return prev
          }
        }, 0)
        
        if (column.property === 'quantity') {
          sums[index] = total.toLocaleString()
        } else {
          sums[index] = `¥${total.toFixed(2)}`
        }
      } else {
        sums[index] = ''
      }
    } else {
      sums[index] = ''
    }
  })
  
  return sums
}

// 获取状态类型
const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    pending: 'warning',
    settled: 'success',
    locked: 'info'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    pending: '未结算',
    settled: '已结算',
    locked: '已锁账'
  }
  return texts[status] || status
}

// 分页大小变化
const handleSizeChange = () => {
  fetchData()
}

// 页码变化
const handleCurrentChange = () => {
  fetchData()
}

// 初始化
onMounted(() => {
  // 设置默认日期范围为本月
  const now = new Date()
  const start = new Date(now.getFullYear(), now.getMonth(), 1)
  searchForm.dateRange = [
    dayjs(start).format('YYYY-MM-DD'),
    dayjs(now).format('YYYY-MM-DD')
  ]
  
  fetchData()
})
</script>

<style lang="scss" scoped>
.stats-cards {
  margin-bottom: 20px;
  
  .stat-card {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    
    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-bottom: 10px;
    }
    
    .stat-value {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
      
      &.text-danger {
        color: #f56c6c;
      }
    }
  }
}

.quantity-text {
  font-weight: 600;
  color: #1890ff;
}

.amount-text {
  font-weight: 600;
  color: #67c23a;
}

.final-amount {
  font-weight: 600;
  color: #f56c6c;
  font-size: 14px;
}

.adjust-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  
  .bonus {
    color: #67c23a;
    font-weight: 500;
  }
  
  .penalty {
    color: #f56c6c;
    font-weight: 500;
  }
}
</style>




