<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="工序名称">
          <el-input v-model="searchForm.processName" placeholder="请输入工序名称" clearable />
        </el-form-item>
        <el-form-item label="工序类型">
          <el-select v-model="searchForm.processType" placeholder="请选择工序类型" clearable>
            <el-option label="裁剪" value="cutting" />
            <el-option label="缝制" value="sewing" />
            <el-option label="整烫" value="ironing" />
            <el-option label="包装" value="packing" />
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
          新增工价
        </el-button>
        <el-button type="success" @click="handleImport">
          <el-icon><Upload /></el-icon>
          导入
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
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="processName" label="工序名称" width="150" />
      <el-table-column prop="processType" label="工序类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getProcessTypeTag(row.processType)">
            {{ getProcessTypeName(row.processType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="difficulty" label="难度系数" width="100" />
      <el-table-column prop="standardPrice" label="标准工价" width="120">
        <template #default="{ row }">
          ¥{{ row.standardPrice?.toFixed(2) || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="seasonalPrice" label="旺季工价" width="120">
        <template #default="{ row }">
          ¥{{ row.seasonalPrice?.toFixed(2) || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="urgentPrice" label="急单工价" width="120">
        <template #default="{ row }">
          ¥{{ row.urgentPrice?.toFixed(2) || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="qualityBonus" label="质量奖励" width="100">
        <template #default="{ row }">
          ¥{{ row.qualityBonus?.toFixed(2) || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'info'">
            {{ row.status === 'active' ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="effectiveDate" label="生效日期" width="120" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleCopy(row)">复制</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
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
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="工序名称" prop="processName">
          <el-input v-model="form.processName" placeholder="请输入工序名称" :disabled="isViewMode" />
        </el-form-item>
        
        <el-form-item label="工序类型" prop="processType">
          <el-select v-model="form.processType" placeholder="请选择工序类型" :disabled="isViewMode">
            <el-option label="裁剪" value="cutting" />
            <el-option label="缝制" value="sewing" />
            <el-option label="整烫" value="ironing" />
            <el-option label="包装" value="packing" />
          </el-select>
        </el-form-item>

        <el-form-item label="难度系数" prop="difficulty">
          <el-input-number
            v-model="form.difficulty"
            :min="0.1"
            :max="5.0"
            :step="0.1"
            :precision="1"
            style="width: 100%"
            :disabled="isViewMode"
          />
        </el-form-item>

        <div class="form-row">
          <el-form-item label="标准工价" prop="standardPrice">
            <el-input-number
              v-model="form.standardPrice"
              :min="0"
              :step="0.01"
              :precision="2"
              style="width: 100%"
              :disabled="isViewMode"
            />
          </el-form-item>
          
          <el-form-item label="旺季工价" prop="seasonalPrice">
            <el-input-number
              v-model="form.seasonalPrice"
              :min="0"
              :step="0.01"
              :precision="2"
              style="width: 100%"
              :disabled="isViewMode"
            />
          </el-form-item>
        </div>

        <div class="form-row">
          <el-form-item label="急单工价" prop="urgentPrice">
            <el-input-number
              v-model="form.urgentPrice"
              :min="0"
              :step="0.01"
              :precision="2"
              style="width: 100%"
              :disabled="isViewMode"
            />
          </el-form-item>
          
          <el-form-item label="质量奖励" prop="qualityBonus">
            <el-input-number
              v-model="form.qualityBonus"
              :min="0"
              :step="0.01"
              :precision="2"
              style="width: 100%"
              :disabled="isViewMode"
            />
          </el-form-item>
        </div>

        <el-form-item label="生效日期" prop="effectiveDate">
          <el-date-picker
            v-model="form.effectiveDate"
            type="date"
            placeholder="请选择生效日期"
            style="width: 100%"
            :disabled="isViewMode"
          />
        </el-form-item>

        <el-form-item label="工序描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入工序描述"
            :disabled="isViewMode"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ isViewMode ? '关闭' : '取消' }}</el-button>
          <el-button v-if="!isViewMode" type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, Download, Refresh } from '@element-plus/icons-vue'

// 本地存储 Key
const LOCAL_KEY = 'dev.processPrice.list'

// 简易类型（保持灵活以避免严格类型报错）
interface ProcessPriceItem {
  id: number
  processName: string
  processType: string
  difficulty: number
  standardPrice: number | null
  seasonalPrice: number | null
  urgentPrice: number | null
  qualityBonus: number | null
  status: 'active' | 'inactive'
  effectiveDate: string | null
  description?: string
}

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const isViewMode = ref(false)

// 搜索表单
const searchForm = reactive({
  processName: '',
  processType: '',
  status: ''
})

// 表格数据
const tableData = ref<ProcessPriceItem[]>([])
const selectedRows = ref<ProcessPriceItem[]>([])

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单数据
const form = reactive<any>({
  id: null,
  processName: '',
  processType: '',
  difficulty: 1.0,
  standardPrice: null,
  seasonalPrice: null,
  urgentPrice: null,
  qualityBonus: null,
  effectiveDate: null,
  description: '',
  status: 'active'
})

// 表单验证规则
const rules = {
  processName: [
    { required: true, message: '请输入工序名称', trigger: 'blur' }
  ],
  processType: [
    { required: true, message: '请选择工序类型', trigger: 'change' }
  ],
  difficulty: [
    { required: true, message: '请输入难度系数', trigger: 'blur' }
  ],
  standardPrice: [
    { required: true, message: '请输入标准工价', trigger: 'blur' }
  ],
  effectiveDate: [
    { required: true, message: '请选择生效日期', trigger: 'change' }
  ]
}

// 工具：加载/保存/过滤/分页
const loadAll = (): ProcessPriceItem[] => {
  const raw = localStorage.getItem(LOCAL_KEY)
  if (!raw) return []
  try {
    const arr = JSON.parse(raw)
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}

const saveAll = (list: ProcessPriceItem[]) => {
  localStorage.setItem(LOCAL_KEY, JSON.stringify(list))
}

const applyFilter = (list: ProcessPriceItem[]) => {
  return list.filter((it) => {
    const byName = !searchForm.processName || it.processName.toLowerCase().includes(searchForm.processName.toLowerCase())
    const byType = !searchForm.processType || it.processType === searchForm.processType
    const byStatus = !searchForm.status || it.status === searchForm.status
    return byName && byType && byStatus
  })
}

const paginate = (list: ProcessPriceItem[], page: number, size: number) => {
  const start = (page - 1) * size
  return list.slice(start, start + size)
}

// 工序类型标签颜色
const getProcessTypeTag = (type: string): 'success' | 'primary' | 'warning' | 'info' | 'danger' | undefined => {
  const tags: Record<string, 'warning' | 'success' | 'info' | 'primary'> = {
    cutting: 'warning',
    sewing: 'success',
    ironing: 'info',
    packing: 'primary'
  }
  return tags[type as keyof typeof tags]
}

// 工序类型名称
const getProcessTypeName = (type: string) => {
  const names: Record<string, string> = {
    cutting: '裁剪',
    sewing: '缝制',
    ironing: '整烫',
    packing: '包装'
  }
  return names[type] || type
}

// 首次注入演示数据
const ensureSeed = () => {
  const list = loadAll()
  if (list.length === 0) {
    const seed: ProcessPriceItem[] = [
      {
        id: Date.now(),
        processName: '前片缝制',
        processType: 'sewing',
        difficulty: 1.5,
        standardPrice: 2.5,
        seasonalPrice: 3.0,
        urgentPrice: 3.5,
        qualityBonus: 0.5,
        status: 'active',
        effectiveDate: '2024-01-01',
        description: '示例数据'
      }
    ]
    saveAll(seed)
  }
}

// 获取数据（本地降级）
const fetchData = async () => {
  loading.value = true
  try {
    ensureSeed()
    const all = loadAll()
    const filtered = applyFilter(all)
    pagination.total = filtered.length
    tableData.value = paginate(filtered, pagination.page, pagination.size)
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    processName: '',
    processType: '',
    status: ''
  })
  handleSearch()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增工价'
  isViewMode.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑工价'
  isViewMode.value = false
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

// 查看
const handleView = (row: any) => {
  dialogTitle.value = '查看工价'
  isViewMode.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

// 复制
const handleCopy = (row: any) => {
  dialogTitle.value = '复制工价'
  isViewMode.value = false
  Object.assign(form, { ...row, id: null, processName: `${row.processName}_副本` })
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这个工价吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const all = loadAll()
    const next = all.filter((it) => it.id !== row.id)
    saveAll(next)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    // 用户取消删除
  }
}

// 导入（支持 JSON 文件）
const handleImport = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'application/json,.json'
  input.onchange = async () => {
    const file = input.files?.[0]
    if (!file) return
    try {
      const text = await file.text()
      const arr = JSON.parse(text)
      if (!Array.isArray(arr)) throw new Error('文件格式不正确，应为数组')
      const normalized: ProcessPriceItem[] = arr.map((x: any, idx: number) => ({
        id: Number(x.id) || Date.now() + idx,
        processName: String(x.processName || ''),
        processType: String(x.processType || ''),
        difficulty: Number(x.difficulty ?? 1),
        standardPrice: x.standardPrice != null ? Number(x.standardPrice) : null,
        seasonalPrice: x.seasonalPrice != null ? Number(x.seasonalPrice) : null,
        urgentPrice: x.urgentPrice != null ? Number(x.urgentPrice) : null,
        qualityBonus: x.qualityBonus != null ? Number(x.qualityBonus) : null,
        status: (x.status === 'inactive') ? 'inactive' : 'active',
        effectiveDate: x.effectiveDate ? String(x.effectiveDate) : null,
        description: x.description ? String(x.description) : ''
      }))
      const all = loadAll()
      // 合并（同 id 覆盖）
      const map = new Map<number, ProcessPriceItem>()
      ;[...all, ...normalized].forEach((it) => map.set(it.id, it))
      saveAll(Array.from(map.values()))
      ElMessage.success('导入成功')
      fetchData()
    } catch (e: any) {
      ElMessage.error(`导入失败：${e?.message || '解析错误'}`)
    }
  }
  input.click()
}

// 导出 CSV
const handleExport = () => {
  const all = applyFilter(loadAll())
  const headers = ['id','processName','processType','difficulty','standardPrice','seasonalPrice','urgentPrice','qualityBonus','status','effectiveDate','description']
  const rows = all.map((x) => headers.map((h) => (x as any)[h] ?? ''))
  const csv = [headers.join(','), ...rows.map((r) => r.map((v) => String(v).replace(/\n/g, ' ').replace(/,/g, '，')).join(','))].join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'process-prices.csv'
  a.click()
  URL.revokeObjectURL(url)
}

// 表格选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection as any
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.size = size
  fetchData()
}

// 当前页变化
const handleCurrentChange = (page: number) => {
  pagination.page = page
  fetchData()
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: null,
    processName: '',
    processType: '',
    difficulty: 1.0,
    standardPrice: null,
    seasonalPrice: null,
    urgentPrice: null,
    qualityBonus: null,
    effectiveDate: null,
    description: '',
    status: 'active'
  })
  formRef.value?.clearValidate()
}

// 对话框关闭
const handleDialogClose = () => {
  isViewMode.value = false
  resetForm()
}

// 提交表单（本地保存）
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true

    const all = loadAll()
    if (!form.id) {
      const newItem: ProcessPriceItem = {
        id: Date.now(),
        processName: form.processName,
        processType: form.processType,
        difficulty: Number(form.difficulty || 1),
        standardPrice: form.standardPrice != null ? Number(form.standardPrice) : null,
        seasonalPrice: form.seasonalPrice != null ? Number(form.seasonalPrice) : null,
        urgentPrice: form.urgentPrice != null ? Number(form.urgentPrice) : null,
        qualityBonus: form.qualityBonus != null ? Number(form.qualityBonus) : null,
        status: form.status === 'inactive' ? 'inactive' : 'active',
        effectiveDate: form.effectiveDate,
        description: form.description || ''
      }
      all.unshift(newItem)
      saveAll(all)
      ElMessage.success('创建成功')
    } else {
      const idx = all.findIndex((x) => x.id === form.id)
      if (idx !== -1) {
        const updated: ProcessPriceItem = {
          id: form.id,
          processName: form.processName,
          processType: form.processType,
          difficulty: Number(form.difficulty || 1),
          standardPrice: form.standardPrice != null ? Number(form.standardPrice) : null,
          seasonalPrice: form.seasonalPrice != null ? Number(form.seasonalPrice) : null,
          urgentPrice: form.urgentPrice != null ? Number(form.urgentPrice) : null,
          qualityBonus: form.qualityBonus != null ? Number(form.qualityBonus) : null,
          status: form.status === 'inactive' ? 'inactive' : 'active',
          effectiveDate: form.effectiveDate,
          description: form.description || ''
        }
        all.splice(idx, 1, updated)
        saveAll(all)
        ElMessage.success('更新成功')
      }
    }

    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.form-row {
  display: flex;
  gap: 20px;
}

.form-row .el-form-item {
  flex: 1;
}
</style>




