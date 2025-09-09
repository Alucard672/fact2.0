<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="车间编码">
          <el-input v-model="searchForm.workshopCode" placeholder="请输入车间编码" clearable />
        </el-form-item>
        <el-form-item label="车间名称">
          <el-input v-model="searchForm.workshopName" placeholder="请输入车间名称" clearable />
        </el-form-item>
        <el-form-item label="车间主管">
          <el-select v-model="searchForm.managerId" placeholder="请选择主管" clearable filterable>
            <el-option
              v-for="item in managerList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="active" />
            <el-option label="停用" value="inactive" />
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
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增车间
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
      <el-table-column prop="workshopCode" label="车间编码" width="120" />
      <el-table-column prop="workshopName" label="车间名称" min-width="150" />
      <el-table-column prop="managerName" label="车间主管" width="120">
        <template #default="{ row }">
          {{ row.managerName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="工人数量" width="100" align="center">
        <template #default="{ row }">
          <el-link type="primary" @click="handleViewWorkers(row)">
            {{ row.workerCount || 0 }}人
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="今日产量" width="100" align="center">
        <template #default="{ row }">
          {{ row.todayProduction || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="本月产量" width="100" align="center">
        <template #default="{ row }">
          {{ row.monthProduction || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="效率评分" width="100">
        <template #default="{ row }">
          <el-rate
            v-model="row.efficiencyScore"
            disabled
            show-score
            text-color="#ff9900"
            score-template="{value}"
          />
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            active-value="active"
            inactive-value="inactive"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleViewStats(row)">统计</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="车间编码" prop="workshopCode">
          <el-input v-model="formData.workshopCode" placeholder="请输入车间编码" />
        </el-form-item>
        <el-form-item label="车间名称" prop="workshopName">
          <el-input v-model="formData.workshopName" placeholder="请输入车间名称" />
        </el-form-item>
        <el-form-item label="车间主管" prop="managerId">
          <el-select v-model="formData.managerId" placeholder="请选择车间主管" style="width: 100%" filterable>
            <el-option
              v-for="item in managerList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="车间描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入车间描述"
          />
        </el-form-item>
        <el-form-item label="车间地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入车间地址" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="工位数量" prop="workstationCount">
          <el-input-number
            v-model="formData.workstationCount"
            :min="0"
            :step="1"
            placeholder="请输入工位数量"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number
            v-model="formData.sortOrder"
            :min="0"
            :step="1"
            placeholder="请输入排序号"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 车间工人列表对话框 -->
    <el-dialog
      v-model="workerDialogVisible"
      :title="`${currentWorkshop?.workshopName} - 工人列表`"
      width="900px"
    >
      <el-table :data="workerList" border max-height="400">
        <el-table-column type="index" width="50" />
        <el-table-column prop="employeeNo" label="工号" width="100" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="position" label="岗位" width="100" />
        <el-table-column prop="todayProduction" label="今日产量" width="100" align="center" />
        <el-table-column prop="monthProduction" label="本月产量" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
              {{ row.status === 'active' ? '在岗' : '离岗' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 车间统计对话框 -->
    <el-dialog
      v-model="statsDialogVisible"
      :title="`${currentWorkshop?.workshopName} - 生产统计`"
      width="900px"
    >
      <div class="workshop-stats">
        <!-- 统计卡片 -->
        <el-row :gutter="20" class="stats-cards">
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ workshopStats.totalWorkers }}</div>
              <div class="stat-label">工人总数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ workshopStats.activeWorkers }}</div>
              <div class="stat-label">在岗工人</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ workshopStats.todayProduction }}</div>
              <div class="stat-label">今日产量</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ workshopStats.monthProduction }}</div>
              <div class="stat-label">本月产量</div>
            </div>
          </el-col>
        </el-row>

        <!-- 产量趋势图表 -->
        <div class="chart-container">
          <div class="chart-title">最近7天产量趋势</div>
          <!-- 这里应该集成图表组件 -->
          <div style="height: 300px; display: flex; align-items: center; justify-content: center; color: #999;">
            图表展示区域
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus, Download, Refresh } from '@element-plus/icons-vue'
import type { Workshop } from '@/types/api'

// 搜索表单
const searchForm = reactive({
  workshopCode: '',
  workshopName: '',
  managerId: null,
  status: ''
})

// 分页参数
const pageParams = reactive({
  current: 1,
  size: 20
})

// 表单数据
const formData = ref<any>({
  workshopCode: '',
  workshopName: '',
  managerId: null,
  description: '',
  address: '',
  contactPhone: '',
  workstationCount: 0,
  sortOrder: 0,
  status: 'active'
})

// 表单验证规则
const formRules: FormRules = {
  workshopCode: [
    { required: true, message: '请输入车间编码', trigger: 'blur' }
  ],
  workshopName: [
    { required: true, message: '请输入车间名称', trigger: 'blur' }
  ]
}

// 状态变量
const loading = ref(false)
const dialogVisible = ref(false)
const workerDialogVisible = ref(false)
const statsDialogVisible = ref(false)
const dialogTitle = computed(() => formData.value.id ? '编辑车间' : '新增车间')
const tableData = ref<any[]>([])
const total = ref(0)
const selectedRows = ref<any[]>([])
const formRef = ref<FormInstance>()
const currentWorkshop = ref<any>()
const workerList = ref<any[]>([])
const workshopStats = ref({
  totalWorkers: 0,
  activeWorkers: 0,
  todayProduction: 0,
  monthProduction: 0
})

// 主管列表（实际从API获取）
const managerList = ref([
  { id: 1, name: '张主管' },
  { id: 2, name: '李主管' },
  { id: 3, name: '王主管' }
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
          workshopCode: 'WS001',
          workshopName: '一车间',
          managerId: 1,
          managerName: '张主管',
          workerCount: 35,
          todayProduction: 1580,
          monthProduction: 45200,
          efficiencyScore: 4.5,
          sortOrder: 1,
          status: 'active',
          description: '主要负责基础工序'
        },
        {
          id: 2,
          workshopCode: 'WS002',
          workshopName: '二车间',
          managerId: 2,
          managerName: '李主管',
          workerCount: 28,
          todayProduction: 1320,
          monthProduction: 38600,
          efficiencyScore: 4.0,
          sortOrder: 2,
          status: 'active',
          description: '主要负责精细工序'
        },
        {
          id: 3,
          workshopCode: 'WS003',
          workshopName: '三车间',
          managerId: 3,
          managerName: '王主管',
          workerCount: 42,
          todayProduction: 2100,
          monthProduction: 52800,
          efficiencyScore: 5.0,
          sortOrder: 3,
          status: 'active',
          description: '主要负责后整工序'
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
    workshopCode: '',
    workshopName: '',
    managerId: null,
    status: ''
  })
  handleSearch()
}

// 新增
const handleAdd = () => {
  formData.value = {
    workshopCode: '',
    workshopName: '',
    managerId: null,
    description: '',
    address: '',
    contactPhone: '',
    workstationCount: 0,
    sortOrder: 0,
    status: 'active'
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  formData.value = { ...row }
  dialogVisible.value = true
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除车间 ${row.workshopName} 吗？`,
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

// 查看工人列表
const handleViewWorkers = (row: any) => {
  currentWorkshop.value = row
  // 模拟加载工人列表
  workerList.value = [
    {
      id: 1,
      employeeNo: 'EMP001',
      name: '张三',
      phone: '13800138001',
      position: '缝纫工',
      todayProduction: 85,
      monthProduction: 2200,
      status: 'active'
    },
    {
      id: 2,
      employeeNo: 'EMP002',
      name: '李四',
      phone: '13800138002',
      position: '裁剪工',
      todayProduction: 92,
      monthProduction: 2450,
      status: 'active'
    }
  ]
  workerDialogVisible.value = true
}

// 查看统计
const handleViewStats = (row: any) => {
  currentWorkshop.value = row
  // 模拟加载统计数据
  workshopStats.value = {
    totalWorkers: row.workerCount,
    activeWorkers: Math.floor(row.workerCount * 0.9),
    todayProduction: row.todayProduction,
    monthProduction: row.monthProduction
  }
  statsDialogVisible.value = true
}

// 状态切换
const handleStatusChange = (row: any) => {
  // 调用状态更新API
  ElMessage.success(`车间已${row.status === 'active' ? '启用' : '停用'}`)
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 选择变化
const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate((valid) => {
    if (valid) {
      // 调用保存API
      ElMessage.success('保存成功')
      dialogVisible.value = false
      fetchData()
    }
  })
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
.workshop-stats {
  .stats-cards {
    margin-bottom: 30px;
    
    .stat-card {
      background: #f5f7fa;
      padding: 20px;
      border-radius: 8px;
      text-align: center;
      
      .stat-value {
        font-size: 32px;
        font-weight: 600;
        color: #1890ff;
        margin-bottom: 10px;
      }
      
      .stat-label {
        font-size: 14px;
        color: #909399;
      }
    }
  }
  
  .chart-container {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    border: 1px solid #e4e7ed;
    
    .chart-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 20px;
    }
  }
}
</style>




