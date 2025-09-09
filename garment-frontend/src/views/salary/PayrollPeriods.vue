<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="结算期">
          <el-input v-model="searchForm.periodName" placeholder="请输入结算期名称" clearable />
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" value="draft" />
            <el-option label="已确认" value="confirmed" />
            <el-option label="已锁账" value="locked" />
            <el-option label="已发放" value="paid" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格工具栏 -->
    <div class="table-toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          创建结算期
        </el-button>
        <el-button type="success" @click="handleAutoGenerate">
          <el-icon><MagicStick /></el-icon>
          自动生成
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
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="periodName" label="结算期名称" min-width="180">
        <template #default="{ row }">
          <el-link type="primary" @click="handleView(row)">{{ row.periodName }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="结算周期" width="200">
        <template #default="{ row }">
          {{ row.fromDate }} 至 {{ row.toDate }}
        </template>
      </el-table-column>
      <el-table-column prop="workerCount" label="员工数" width="100" align="center">
        <template #default="{ row }">
          {{ row.workerCount }}人
        </template>
      </el-table-column>
      <el-table-column prop="recordCount" label="记录数" width="100" align="center">
        <template #default="{ row }">
          {{ row.recordCount.toLocaleString() }}
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="总金额" width="150">
        <template #default="{ row }">
          <span class="amount-text">¥{{ row.totalAmount.toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdBy" label="创建人" width="100" />
      <el-table-column prop="createdAt" label="创建时间" width="160" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button link type="primary" @click="handleEdit(row)" v-if="row.status === 'draft'">编辑</el-button>
          <el-button link type="primary" @click="handleConfirm(row)" v-if="row.status === 'draft'">确认</el-button>
          <el-button link type="primary" @click="handleLock(row)" v-if="row.status === 'confirmed'">锁账</el-button>
          <el-button link type="primary" @click="handleUnlock(row)" v-if="row.status === 'locked'">解锁</el-button>
          <el-button link type="primary" @click="handlePay(row)" v-if="row.status === 'locked'">发放</el-button>
          <el-button link type="primary" @click="handleExportDetail(row)">导出明细</el-button>
          <el-button link type="danger" @click="handleDelete(row)" v-if="row.status === 'draft'">删除</el-button>
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

    <!-- 创建结算期对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建结算期"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="120px">
        <el-form-item label="结算期名称" prop="periodName">
          <el-input v-model="createForm.periodName" placeholder="例如：2024年1月上半月" />
        </el-form-item>
        <el-form-item label="结算周期" prop="dateRange" required>
          <el-date-picker
            v-model="createForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="包含车间" prop="workshopIds">
          <el-select
            v-model="createForm.workshopIds"
            multiple
            placeholder="请选择车间（不选则包含所有）"
            style="width: 100%"
          >
            <el-option
              v-for="item in workshopList"
              :key="item.id"
              :label="item.workshopName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="createForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitCreate" :loading="createLoading">
            创建并统计
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 自动生成对话框 -->
    <el-dialog
      v-model="autoDialogVisible"
      title="自动生成结算期"
      width="500px"
    >
      <el-form ref="autoFormRef" :model="autoForm" label-width="120px">
        <el-form-item label="生成类型" prop="type">
          <el-radio-group v-model="autoForm.type">
            <el-radio label="weekly">按周</el-radio>
            <el-radio label="biweekly">按半月</el-radio>
            <el-radio label="monthly">按月</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生成月份" prop="month">
          <el-date-picker
            v-model="autoForm.month"
            type="month"
            placeholder="选择月份"
            value-format="YYYY-MM"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="生成说明">
          <el-alert
            :title="getAutoGenerateDesc()"
            type="info"
            :closable="false"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="autoDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitAuto">生成</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus, MagicStick, Download, Refresh } from '@element-plus/icons-vue'
import type { PayrollPeriod } from '@/types/api'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  periodName: '',
  dateRange: [],
  status: ''
})

// 分页参数
const pageParams = reactive({
  current: 1,
  size: 20
})

// 创建表单
const createForm = ref({
  periodName: '',
  dateRange: [],
  workshopIds: [],
  remark: ''
})

// 自动生成表单
const autoForm = ref({
  type: 'biweekly',
  month: ''
})

const createRules: FormRules = {
  periodName: [
    { required: true, message: '请输入结算期名称', trigger: 'blur' }
  ],
  dateRange: [
    { required: true, message: '请选择结算周期', trigger: 'change' }
  ]
}

// 状态变量
const loading = ref(false)
const createLoading = ref(false)
const createDialogVisible = ref(false)
const autoDialogVisible = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const selectedRows = ref<any[]>([])
const createFormRef = ref<FormInstance>()
const autoFormRef = ref<FormInstance>()

// 车间列表
const workshopList = ref([
  { id: 1, workshopName: '一车间' },
  { id: 2, workshopName: '二车间' },
  { id: 3, workshopName: '三车间' }
])

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 模拟API调用
    setTimeout(() => {
      tableData.value = [
        {
          id: 1,
          periodName: '2024年1月上半月',
          fromDate: '2024-01-01',
          toDate: '2024-01-15',
          totalAmount: 158650.80,
          workerCount: 126,
          recordCount: 3580,
          status: 'locked',
          createdBy: '张管理',
          createdAt: '2024-01-16 09:00:00',
          lockedBy: '李财务',
          lockedAt: '2024-01-16 15:30:00'
        },
        {
          id: 2,
          periodName: '2024年1月下半月',
          fromDate: '2024-01-16',
          toDate: '2024-01-31',
          totalAmount: 182350.60,
          workerCount: 132,
          recordCount: 4120,
          status: 'confirmed',
          createdBy: '张管理',
          createdAt: '2024-02-01 09:00:00'
        },
        {
          id: 3,
          periodName: '2024年2月上半月',
          fromDate: '2024-02-01',
          toDate: '2024-02-15',
          totalAmount: 95280.40,
          workerCount: 118,
          recordCount: 2450,
          status: 'draft',
          createdBy: '张管理',
          createdAt: '2024-02-16 09:00:00'
        }
      ]
      total.value = 3
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
    periodName: '',
    dateRange: [],
    status: ''
  })
  handleSearch()
}

// 创建结算期
const handleCreate = () => {
  createForm.value = {
    periodName: '',
    dateRange: [],
    workshopIds: [],
    remark: ''
  }
  createDialogVisible.value = true
}

// 提交创建
const handleSubmitCreate = async () => {
  if (!createFormRef.value) return
  
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      createLoading.value = true
      try {
        // 调用创建API
        await new Promise(resolve => setTimeout(resolve, 2000))
        ElMessage.success('结算期创建成功，正在统计数据...')
        createDialogVisible.value = false
        fetchData()
      } catch (error) {
        ElMessage.error('创建失败')
      } finally {
        createLoading.value = false
      }
    }
  })
}

// 自动生成
const handleAutoGenerate = () => {
  const now = new Date()
  autoForm.value = {
    type: 'biweekly',
    month: `${now.getFullYear()}-${(now.getMonth() + 1).toString().padStart(2, '0')}`
  }
  autoDialogVisible.value = true
}

// 获取自动生成说明
const getAutoGenerateDesc = () => {
  const type = autoForm.value.type
  const month = autoForm.value.month
  
  if (!month) return '请选择生成月份'
  
  const texts: Record<string, string> = {
    weekly: `将为 ${month} 生成4-5个按周的结算期`,
    biweekly: `将为 ${month} 生成2个半月结算期（1-15日，16-月末）`,
    monthly: `将为 ${month} 生成1个月度结算期`
  }
  
  return texts[type] || ''
}

// 提交自动生成
const handleSubmitAuto = () => {
  if (!autoForm.value.month) {
    ElMessage.warning('请选择生成月份')
    return
  }
  
  ElMessageBox.confirm(
    getAutoGenerateDesc(),
    '确认生成',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    // 调用自动生成API
    ElMessage.success('结算期生成成功')
    autoDialogVisible.value = false
    fetchData()
  })
}

// 查看详情
const handleView = (row: any) => {
  router.push(`/salary/payroll-details/${row.id}`)
}

// 编辑
const handleEdit = (row: any) => {
  ElMessage.info('编辑功能开发中')
}

// 确认
const handleConfirm = (row: any) => {
  ElMessageBox.confirm(
    `确认结算期 ${row.periodName} 吗？确认后将不能修改记录`,
    '确认结算期',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 调用确认API
    ElMessage.success('确认成功')
    fetchData()
  })
}

// 锁账
const handleLock = (row: any) => {
  ElMessageBox.confirm(
    `锁定结算期 ${row.periodName} 吗？锁定后数据将不能修改`,
    '锁定结算期',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 调用锁账API
    ElMessage.success('锁账成功')
    fetchData()
  })
}

// 解锁
const handleUnlock = (row: any) => {
  ElMessageBox.prompt(
    `请输入解锁原因`,
    '解锁结算期',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '请输入解锁原因'
    }
  ).then(({ value }) => {
    // 调用解锁API
    ElMessage.success('解锁成功')
    fetchData()
  })
}

// 发放工资
const handlePay = (row: any) => {
  ElMessageBox.confirm(
    `确定发放 ${row.periodName} 的工资吗？总金额：¥${row.totalAmount.toFixed(2)}`,
    '发放工资',
    {
      confirmButtonText: '确定发放',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 调用发放API
    ElMessage.success('工资发放成功')
    fetchData()
  })
}

// 导出明细
const handleExportDetail = (row: any) => {
  ElMessage.info(`导出 ${row.periodName} 明细`)
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除结算期 ${row.periodName} 吗？`,
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

// 获取状态类型
const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    draft: 'info',
    confirmed: 'warning',
    locked: 'success',
    paid: 'primary'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    draft: '草稿',
    confirmed: '已确认',
    locked: '已锁账',
    paid: '已发放'
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
  fetchData()
})
</script>

<style lang="scss" scoped>
.amount-text {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}
</style>




