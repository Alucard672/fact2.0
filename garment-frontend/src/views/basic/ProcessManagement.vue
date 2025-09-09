<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="工序编码">
          <el-input v-model="searchForm.processCode" placeholder="请输入工序编码" clearable />
        </el-form-item>
        <el-form-item label="工序名称">
          <el-input v-model="searchForm.processName" placeholder="请输入工序名称" clearable />
        </el-form-item>
        <el-form-item label="工序分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
            <el-option label="裁剪工序" value="裁剪工序" />
            <el-option label="缝制工序" value="缝制工序" />
            <el-option label="后整工序" value="后整工序" />
            <el-option label="质检工序" value="质检工序" />
            <el-option label="包装工序" value="包装工序" />
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
          新增工序
        </el-button>
        <el-button type="success" @click="handleImport">
          <el-icon><Upload /></el-icon>
          导入
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
        <el-button type="danger" :disabled="!selectedRows.length" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>
          批量删除
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
      <el-table-column prop="processCode" label="工序编码" width="120" />
      <el-table-column prop="processName" label="工序名称" min-width="150" />
      <el-table-column prop="category" label="工序分类" width="120">
        <template #default="{ row }">
          <el-tag type="info">{{ row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="unit" label="计量单位" width="100" align="center" />
      <el-table-column prop="defaultPrice" label="默认工价" width="120">
        <template #default="{ row }">
          <span style="color: #f56c6c; font-weight: 600;">
            ¥{{ row.defaultPrice?.toFixed(2) || '0.00' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="difficultyLevel" label="难度等级" width="100">
        <template #default="{ row }">
          <el-tag :type="getDifficultyType(row.difficultyLevel)">
            {{ getDifficultyText(row.difficultyLevel) }}
          </el-tag>
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
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handlePriceConfig(row)">工价配置</el-button>
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
        <el-form-item label="工序编码" prop="processCode">
          <el-input v-model="formData.processCode" placeholder="请输入工序编码" />
        </el-form-item>
        <el-form-item label="工序名称" prop="processName">
          <el-input v-model="formData.processName" placeholder="请输入工序名称" />
        </el-form-item>
        <el-form-item label="工序分类" prop="category">
          <el-select v-model="formData.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="裁剪工序" value="裁剪工序" />
            <el-option label="缝制工序" value="缝制工序" />
            <el-option label="后整工序" value="后整工序" />
            <el-option label="质检工序" value="质检工序" />
            <el-option label="包装工序" value="包装工序" />
          </el-select>
        </el-form-item>
        <el-form-item label="计量单位" prop="unit">
          <el-radio-group v-model="formData.unit">
            <el-radio label="件">件</el-radio>
            <el-radio label="个">个</el-radio>
            <el-radio label="条">条</el-radio>
            <el-radio label="套">套</el-radio>
            <el-radio label="包">包</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="默认工价" prop="defaultPrice">
          <el-input-number
            v-model="formData.defaultPrice"
            :min="0"
            :precision="2"
            :step="0.1"
            placeholder="请输入默认工价"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="难度等级" prop="difficultyLevel">
          <el-radio-group v-model="formData.difficultyLevel">
            <el-radio label="easy">简单</el-radio>
            <el-radio label="medium">中等</el-radio>
            <el-radio label="hard">困难</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="质量标准" prop="qualityStandard">
          <el-input
            v-model="formData.qualityStandard"
            type="textarea"
            :rows="3"
            placeholder="请输入质量标准要求"
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

    <!-- 工价配置对话框 -->
    <el-dialog
      v-model="priceDialogVisible"
      title="工价配置"
      width="900px"
    >
      <div class="price-config">
        <!-- 新增工价按钮 -->
        <div class="price-toolbar">
          <el-button type="primary" size="small" @click="handleAddPrice">
            <el-icon><Plus /></el-icon>
            新增工价配置
          </el-button>
        </div>
        
        <!-- 工价列表 -->
        <el-table :data="priceList" border>
          <el-table-column prop="styleName" label="款式" width="150">
            <template #default="{ row }">
              {{ row.styleName || '通用' }}
            </template>
          </el-table-column>
          <el-table-column prop="workshopName" label="车间" width="120">
            <template #default="{ row }">
              {{ row.workshopName || '通用' }}
            </template>
          </el-table-column>
          <el-table-column prop="price" label="工价" width="100">
            <template #default="{ row }">
              <span style="color: #f56c6c; font-weight: 600;">
                ¥{{ row.price?.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="priceType" label="计价方式" width="100">
            <template #default="{ row }">
              <el-tag size="small">{{ getPriceTypeText(row.priceType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="effectiveFrom" label="生效日期" width="120" />
          <el-table-column prop="effectiveTo" label="失效日期" width="120">
            <template #default="{ row }">
              {{ row.effectiveTo || '长期有效' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                {{ row.status === 'active' ? '生效' : '失效' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleEditPrice(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDeletePrice(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus, Upload, Download, Delete, Refresh } from '@element-plus/icons-vue'
import type { Process } from '@/types/api'

// 搜索表单
const searchForm = reactive({
  processCode: '',
  processName: '',
  category: '',
  status: ''
})

// 分页参数
const pageParams = reactive({
  current: 1,
  size: 20
})

// 表单数据
const formData = ref<Partial<Process>>({
  processCode: '',
  processName: '',
  category: '',
  unit: '件',
  defaultPrice: 0,
  difficultyLevel: 'medium',
  qualityStandard: '',
  sortOrder: 0,
  status: 'active'
})

// 表单验证规则
const formRules: FormRules = {
  processCode: [
    { required: true, message: '请输入工序编码', trigger: 'blur' }
  ],
  processName: [
    { required: true, message: '请输入工序名称', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择工序分类', trigger: 'change' }
  ],
  unit: [
    { required: true, message: '请选择计量单位', trigger: 'change' }
  ]
}

// 状态变量
const loading = ref(false)
const dialogVisible = ref(false)
const priceDialogVisible = ref(false)
const dialogTitle = computed(() => formData.value.id ? '编辑工序' : '新增工序')
const tableData = ref<Process[]>([])
const total = ref(0)
const selectedRows = ref<Process[]>([])
const formRef = ref<FormInstance>()
const currentProcess = ref<Process>()
const priceList = ref<any[]>([])

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 模拟API调用
    setTimeout(() => {
      tableData.value = [
        {
          id: 1,
          tenantId: 1,
          processCode: 'P001',
          processName: '裁剪',
          category: '裁剪工序',
          unit: '件',
          defaultPrice: 0.50,
          difficultyLevel: 'easy',
          qualityStandard: '裁剪整齐，无毛边',
          sortOrder: 1,
          status: 'active'
        },
        {
          id: 2,
          tenantId: 1,
          processCode: 'P002',
          processName: '缝合',
          category: '缝制工序',
          unit: '件',
          defaultPrice: 1.20,
          difficultyLevel: 'medium',
          qualityStandard: '缝线平直，针脚均匀',
          sortOrder: 2,
          status: 'active'
        },
        {
          id: 3,
          tenantId: 1,
          processCode: 'P003',
          processName: '锁边',
          category: '缝制工序',
          unit: '件',
          defaultPrice: 0.80,
          difficultyLevel: 'easy',
          qualityStandard: '锁边牢固，无脱线',
          sortOrder: 3,
          status: 'active'
        },
        {
          id: 4,
          tenantId: 1,
          processCode: 'P004',
          processName: '熨烫',
          category: '后整工序',
          unit: '件',
          defaultPrice: 0.60,
          difficultyLevel: 'easy',
          qualityStandard: '平整无褶皱',
          sortOrder: 4,
          status: 'active'
        }
      ]
      total.value = 4
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
    processCode: '',
    processName: '',
    category: '',
    status: ''
  })
  handleSearch()
}

// 新增
const handleAdd = () => {
  formData.value = {
    processCode: '',
    processName: '',
    category: '',
    unit: '件',
    defaultPrice: 0,
    difficultyLevel: 'medium',
    qualityStandard: '',
    sortOrder: 0,
    status: 'active'
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: Process) => {
  formData.value = { ...row }
  dialogVisible.value = true
}

// 删除
const handleDelete = (row: Process) => {
  ElMessageBox.confirm(
    `确定要删除工序 ${row.processName} 吗？`,
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

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 个工序吗？`,
    '批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 调用批量删除API
    ElMessage.success('批量删除成功')
    fetchData()
  })
}

// 状态切换
const handleStatusChange = (row: Process) => {
  // 调用状态更新API
  ElMessage.success(`工序已${row.status === 'active' ? '启用' : '停用'}`)
}

// 工价配置
const handlePriceConfig = (row: Process) => {
  currentProcess.value = row
  loadPriceList(row.id)
  priceDialogVisible.value = true
}

// 加载工价列表
const loadPriceList = (processId: number) => {
  // 模拟加载工价配置
  priceList.value = [
    {
      id: 1,
      processId: processId,
      styleId: 1,
      styleName: '经典T恤',
      workshopId: 1,
      workshopName: '一车间',
      price: 1.50,
      priceType: 'per_piece',
      effectiveFrom: '2024-01-01',
      effectiveTo: null,
      status: 'active'
    },
    {
      id: 2,
      processId: processId,
      styleId: null,
      styleName: null,
      workshopId: 2,
      workshopName: '二车间',
      price: 1.30,
      priceType: 'per_piece',
      effectiveFrom: '2024-01-01',
      effectiveTo: null,
      status: 'active'
    }
  ]
}

// 新增工价
const handleAddPrice = () => {
  ElMessage.info('工价配置功能开发中')
}

// 编辑工价
const handleEditPrice = (row: any) => {
  ElMessage.info('编辑工价功能开发中')
}

// 删除工价
const handleDeletePrice = (row: any) => {
  ElMessageBox.confirm(
    '确定要删除此工价配置吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('删除成功')
    loadPriceList(currentProcess.value!.id)
  })
}

// 导入
const handleImport = () => {
  ElMessage.info('导入功能开发中')
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 选择变化
const handleSelectionChange = (rows: Process[]) => {
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

// 获取难度类型
const getDifficultyType = (level: string) => {
  const types: Record<string, string> = {
    easy: 'success',
    medium: 'warning',
    hard: 'danger'
  }
  return types[level] || 'info'
}

// 获取难度文本
const getDifficultyText = (level: string) => {
  const texts: Record<string, string> = {
    easy: '简单',
    medium: '中等',
    hard: '困难'
  }
  return texts[level] || level
}

// 获取计价方式文本
const getPriceTypeText = (type: string) => {
  const texts: Record<string, string> = {
    per_piece: '按件',
    per_hour: '按时',
    fixed: '固定'
  }
  return texts[type] || type
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
.price-config {
  .price-toolbar {
    margin-bottom: 20px;
  }
}
</style>




