<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="款号">
          <el-input v-model="searchForm.styleCode" placeholder="请输入款号" clearable />
        </el-form-item>
        <el-form-item label="款式名称">
          <el-input v-model="searchForm.styleName" placeholder="请输入款式名称" clearable />
        </el-form-item>
        <el-form-item label="客户">
          <el-select v-model="searchForm.customerId" placeholder="请选择客户" clearable>
            <el-option
              v-for="item in customerList"
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
          新增款式
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
      <el-table-column prop="styleCode" label="款号" width="120" />
      <el-table-column prop="styleName" label="款式名称" min-width="150" />
      <el-table-column prop="customerName" label="客户" width="120" />
      <el-table-column prop="season" label="季节" width="80" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column label="颜色" width="150">
        <template #default="{ row }">
          <el-tag v-for="color in row.colors" :key="color" size="small" style="margin-right: 4px;">
            {{ color }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="尺码" width="150">
        <template #default="{ row }">
          <el-tag v-for="size in row.sizes" :key="size" size="small" type="info" style="margin-right: 4px;">
            {{ size }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="unitPrice" label="单价" width="100">
        <template #default="{ row }">
          ¥{{ row.unitPrice?.toFixed(2) || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'info'">
            {{ row.status === 'active' ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
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
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="款号" prop="styleCode">
              <el-input v-model="formData.styleCode" placeholder="请输入款号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="款式名称" prop="styleName">
              <el-input v-model="formData.styleName" placeholder="请输入款式名称" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="formData.customerId" placeholder="请选择客户" style="width: 100%">
                <el-option
                  v-for="item in customerList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="季节" prop="season">
              <el-select v-model="formData.season" placeholder="请选择季节" style="width: 100%">
                <el-option label="春季" value="春季" />
                <el-option label="夏季" value="夏季" />
                <el-option label="秋季" value="秋季" />
                <el-option label="冬季" value="冬季" />
                <el-option label="全季" value="全季" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-input v-model="formData.category" placeholder="请输入分类" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单价" prop="unitPrice">
              <el-input-number
                v-model="formData.unitPrice"
                :min="0"
                :precision="2"
                placeholder="请输入单价"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="颜色" prop="colors">
          <el-select
            v-model="formData.colors"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择或输入颜色"
            style="width: 100%"
          >
            <el-option label="白色" value="白色" />
            <el-option label="黑色" value="黑色" />
            <el-option label="灰色" value="灰色" />
            <el-option label="红色" value="红色" />
            <el-option label="蓝色" value="蓝色" />
            <el-option label="黄色" value="黄色" />
            <el-option label="绿色" value="绿色" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="尺码" prop="sizes">
          <el-checkbox-group v-model="formData.sizes">
            <el-checkbox label="XS" />
            <el-checkbox label="S" />
            <el-checkbox label="M" />
            <el-checkbox label="L" />
            <el-checkbox label="XL" />
            <el-checkbox label="XXL" />
            <el-checkbox label="XXXL" />
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="尺码比例">
          <el-table :data="sizeRatioData" style="width: 100%" size="small">
            <el-table-column prop="size" label="尺码" width="80" />
            <el-table-column label="比例(%)">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.ratio"
                  :min="0"
                  :max="100"
                  size="small"
                  style="width: 100px"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        
        <el-form-item label="工艺说明" prop="techSpec">
          <el-input
            v-model="formData.techSpec"
            type="textarea"
            :rows="3"
            placeholder="请输入工艺说明"
          />
        </el-form-item>
        
        <el-form-item label="款式图片">
          <el-upload
            class="upload-demo"
            action="/api/upload"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :file-list="fileList"
            list-type="picture-card"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus, Upload, Download, Refresh } from '@element-plus/icons-vue'
import type { Style } from '@/types/api'

// 搜索表单
const searchForm = reactive({
  styleCode: '',
  styleName: '',
  customerId: null,
  status: ''
})

// 分页参数
const pageParams = reactive({
  current: 1,
  size: 20
})

// 表单数据
const formData = ref<Partial<Style>>({
  styleCode: '',
  styleName: '',
  customerId: null,
  season: '',
  category: '',
  colors: [],
  sizes: [],
  sizeRatio: {},
  unitPrice: null,
  techSpec: '',
  status: 'active'
})

// 表单验证规则
const formRules: FormRules = {
  styleCode: [
    { required: true, message: '请输入款号', trigger: 'blur' }
  ],
  styleName: [
    { required: true, message: '请输入款式名称', trigger: 'blur' }
  ],
  colors: [
    { required: true, message: '请选择颜色', trigger: 'change' }
  ],
  sizes: [
    { required: true, message: '请选择尺码', trigger: 'change' }
  ]
}

// 状态变量
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = computed(() => formData.value.id ? '编辑款式' : '新增款式')
const tableData = ref<Style[]>([])
const total = ref(0)
const selectedRows = ref<Style[]>([])
const formRef = ref<FormInstance>()
const fileList = ref([])

// 客户列表（实际从API获取）
const customerList = ref([
  { id: 1, name: '优衣库' },
  { id: 2, name: 'ZARA' },
  { id: 3, name: 'H&M' }
])

// 尺码比例数据
const sizeRatioData = computed(() => {
  return formData.value.sizes?.map(size => ({
    size,
    ratio: formData.value.sizeRatio?.[size] || 0
  })) || []
})

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
          styleCode: 'ST001',
          styleName: '经典T恤',
          customerId: 1,
          customerName: '优衣库',
          season: '夏季',
          category: 'T恤',
          colors: ['白色', '黑色', '灰色'],
          sizes: ['S', 'M', 'L', 'XL'],
          sizeRatio: { S: 20, M: 30, L: 30, XL: 20 },
          unitPrice: 39.9,
          status: 'active',
          createdAt: '2024-01-01',
          updatedAt: '2024-01-01'
        },
        {
          id: 2,
          tenantId: 1,
          styleCode: 'ST002',
          styleName: '休闲裤',
          customerId: 2,
          customerName: 'ZARA',
          season: '全季',
          category: '裤装',
          colors: ['黑色', '深蓝', '卡其'],
          sizes: ['28', '30', '32', '34', '36'],
          sizeRatio: { '28': 15, '30': 25, '32': 30, '34': 20, '36': 10 },
          unitPrice: 99.9,
          status: 'active',
          createdAt: '2024-01-02',
          updatedAt: '2024-01-02'
        }
      ]
      total.value = 2
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
    styleCode: '',
    styleName: '',
    customerId: null,
    status: ''
  })
  handleSearch()
}

// 新增
const handleAdd = () => {
  formData.value = {
    styleCode: '',
    styleName: '',
    customerId: null,
    season: '',
    category: '',
    colors: [],
    sizes: [],
    sizeRatio: {},
    unitPrice: null,
    techSpec: '',
    status: 'active'
  }
  dialogVisible.value = true
}

// 查看
const handleView = (row: Style) => {
  ElMessage.info('查看功能开发中')
}

// 编辑
const handleEdit = (row: Style) => {
  formData.value = { ...row }
  dialogVisible.value = true
}

// 复制
const handleCopy = (row: Style) => {
  formData.value = { ...row, id: undefined, styleCode: row.styleCode + '-COPY' }
  dialogVisible.value = true
}

// 删除
const handleDelete = (row: Style) => {
  ElMessageBox.confirm(
    `确定要删除款式 ${row.styleName} 吗？`,
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

// 导入
const handleImport = () => {
  ElMessage.info('导入功能开发中')
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 选择变化
const handleSelectionChange = (rows: Style[]) => {
  selectedRows.value = rows
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate((valid) => {
    if (valid) {
      // 处理尺码比例
      const ratio: Record<string, number> = {}
      sizeRatioData.value.forEach(item => {
        ratio[item.size] = item.ratio
      })
      formData.value.sizeRatio = ratio
      
      // 调用保存API
      ElMessage.success('保存成功')
      dialogVisible.value = false
      fetchData()
    }
  })
}

// 文件预览
const handlePreview = (file: any) => {
  console.log('预览文件:', file)
}

// 文件移除
const handleRemove = (file: any) => {
  console.log('移除文件:', file)
}

// 分页大小变化
const handleSizeChange = () => {
  fetchData()
}

// 页码变化
const handleCurrentChange = () => {
  fetchData()
}

// 监听尺码变化，更新比例数据
watch(() => formData.value.sizes, (newSizes) => {
  if (newSizes) {
    const ratio = formData.value.sizeRatio || {}
    newSizes.forEach(size => {
      if (!ratio[size]) {
        ratio[size] = 0
      }
    })
    // 删除不存在的尺码
    Object.keys(ratio).forEach(size => {
      if (!newSizes.includes(size)) {
        delete ratio[size]
      }
    })
    formData.value.sizeRatio = ratio
  }
})

// 初始化
onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
// 页面样式已在全局样式中定义
</style>




