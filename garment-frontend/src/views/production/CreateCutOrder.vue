<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">创建裁床单</h1>
      <div class="page-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          保存
        </el-button>
      </div>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="90px"
      size="small"
      class="create-form"
    >
      <!-- 款式信息卡片 -->
      <el-card class="form-card">
        <template #header>
          <span>款式信息</span>
        </template>
        <div class="style-info">
          <div class="style-field">
            <label>款号：</label>
            <span class="style-text">{{ selectedStyleDetail?.styleCode || '—' }}</span>
            <el-button type="primary" link @click="openStyleDialog">更改款式</el-button>
          </div>
          <div class="style-field">
            <label>款式名称：</label>
            <span class="style-text">{{ selectedStyleDetail?.styleName || '—' }}</span>
          </div>
          <div class="style-field">
            <label>工序：</label>
            <span class="style-text">{{ selectedStyleDetail?.processCount ?? 0 }}</span>
          </div>
          <div class="style-field">
            <label>默认工价：</label>
            <el-input-number v-model="form.defaultPrice" :min="0" :step="0.1" style="width: 160px" />
          </div>
        </div>
      </el-card>

      <!-- 选择款式弹窗 -->
      <el-dialog v-model="styleDialogVisible" title="选择款式" width="600px">
        <el-select
          v-model="styleSelectTemp"
          filterable
          remote
          :remote-method="searchStyles"
          :loading="styleLoading"
          placeholder="搜索款式（款号/名称）"
          style="width: 100%"
        >
          <el-option
            v-for="style in styleList"
            :key="style.id"
            :label="`${style.styleCode} - ${style.styleName}`"
            :value="style.id"
          />
        </el-select>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="styleDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmSelectStyle">确定</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 基本信息 -->
      <el-card class="form-card">
        <template #header>
          <span>基本信息</span>
        </template>
        
        <div class="form-grid">
          <el-form-item label="裁床单号" prop="orderNo">
            <el-input
              v-model="form.orderNo"
              placeholder="请输入裁床单号"
              clearable
            />
          </el-form-item>
          
          <el-form-item label="紧急程度" prop="urgentLevel">
            <el-select v-model="form.urgentLevel" placeholder="请选择紧急程度">
              <el-option label="普通" value="normal" />
              <el-option label="紧急" value="urgent" />
              <el-option label="特急" value="very_urgent" />
            </el-select>
          </el-form-item>

          <el-form-item label="客户" prop="customerId">
            <el-select
              v-model="form.customerId"
              placeholder="请选择客户"
              filterable
              remote
              :remote-method="searchCustomers"
              :loading="customerLoading"
            >
              <el-option
                v-for="customer in customerList"
                :key="customer.id"
                :label="customer.name"
                :value="customer.id"
              />
            </el-select>
          </el-form-item>

          <!-- 隐藏原“款式”选择 -->
          <el-form-item v-if="false" label="款式" prop="styleId">
            <el-select
              v-model="form.styleId"
              placeholder="请选择款式"
              filterable
              remote
              :remote-method="searchStyles"
              :loading="styleLoading"
            >
              <el-option
                v-for="style in styleList"
                :key="style.id"
                :label="`${style.styleCode} - ${style.styleName}`"
                :value="style.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="裁次号" prop="cutSeqNo">
            <div class="inline-field">
              <el-input v-model="form.cutSeqNo" placeholder="如：A01"></el-input>
              <el-button link type="primary" @click="autoSupplement">自动补数</el-button>
            </div>
          </el-form-item>
          
          <el-form-item label="订单号" prop="externalOrderNo">
            <el-input v-model="form.externalOrderNo" placeholder="请输入订单号"></el-input>
          </el-form-item>

          <el-form-item label="总数量" prop="totalQuantity">
            <el-input-number
              v-model="form.totalQuantity"
              :min="1"
              :step="1"
              placeholder="请输入总数量"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="加工部门" prop="departmentId">
            <el-select v-model="form.departmentId" placeholder="请选择加工部门">
              <el-option v-for="d in deptList" :key="d.id" :label="d.name" :value="d.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="裁床日期" prop="cutDate">
            <el-date-picker v-model="form.cutDate" type="date" placeholder="选择裁床日期" style="width: 100%" />
          </el-form-item>
          <el-form-item label="交货日期" prop="deliveryDate">
            <el-date-picker v-model="form.deliveryDate" type="date" placeholder="选择交货日期" style="width: 100%" />
          </el-form-item>
        </div>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-card>

      <!-- 编辑/打印设置 -->
      <el-card class="form-card">
        <template #header>
          <span>编辑/打印设置</span>
        </template>
        <div class="form-row">
          <el-form-item label="非票备注">
            <el-input v-model="form.nonInvoiceRemark" placeholder="请输入非票备注" />
          </el-form-item>
          <el-form-item label="后缀备注">
            <el-input v-model="form.suffixRemark" placeholder="请输入后缀备注" />
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="公司名称">
            <el-input v-model="form.companyName" placeholder="公司名称" />
          </el-form-item>
        </div>
      </el-card>

      <!-- 颜色/尺码组合选择 -->
      <el-card class="form-card">
        <template #header>
          <span>颜色 / 尺码组合</span>
        </template>
        <div class="form-row">
          <el-form-item label="颜色组合">
            <el-select
              v-model="selectedColors"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="选择或输入颜色"
              style="width: 100%"
            >
              <el-option v-for="c in colorOptions" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>
          <el-form-item label="尺码组合">
            <el-select
              v-model="selectedSizes"
              multiple
              filterable
              placeholder="选择尺码"
              style="width: 100%"
            >
              <el-option v-for="s in sizeOptions" :key="s" :label="s" :value="s" />
            </el-select>
          </el-form-item>
        </div>
      </el-card>

      <!-- 核心裁床表 + 右侧工具 -->
      <el-card class="form-card">
        <template #header>
          <span>裁床表</span>
        </template>
        <!-- 顶部工具栏：添加颜色、自定义扎号/层号、清空 -->
        <div class="sheet-topbar">
          <div class="left">
            <el-button type="primary" @click="addColorRow">添加颜色行</el-button>
          </div>
          <div class="right">
            <el-input v-model="bundlePrefix" placeholder="自定义扎号" style="width: 200px" />
            <el-input v-model="layerPrefix" placeholder="层号前缀" style="width: 100px; margin-left: 8px;" />
            <el-input-number v-model="layerStart" :min="1" style="margin-left: 8px;" />
            <el-button type="danger" plain @click="clearTable" style="margin-left: 8px;">清空裁床表</el-button>
          </div>
        </div>
        <div class="sheet-layout">
          <!-- 表格区域 -->
          <div class="sheet-table-wrapper">
            <table class="sheet-table">
              <thead>
                <tr>
                  <th class="sticky-left">颜色</th>
                  <th v-for="size in selectedSizes" :key="size" class="size-col">
                    <div class="size-header">
                      <span class="size-text">{{ size }}</span>
                      <el-tooltip content="复制该尺码到其他尺码" placement="top">
                        <el-button link :icon="DocumentCopy" class="icon-btn" @click="copyColToOthersWithSize(size)" />
                      </el-tooltip>
                    </div>
                  </th>
                  <th>合计</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, rIdx) in tableRows" :key="row.color">
                  <td class="sticky-left">
                    <div class="color-cell">
                      <span class="color-name">{{ row.color }}</span>
                      <el-tooltip content="复制该颜色行到其他颜色" placement="right">
                        <el-button link :icon="DocumentCopy" class="icon-btn" @click="copyRowToOthersWithColor(row.color)" />
                      </el-tooltip>
                    </div>
                  </td>
                  <td v-for="size in selectedSizes" :key="size">
                    <el-input-number v-model="row.qty[size]" :min="0" :step="1" @change="onCellChange(rIdx, size)" />
                  </td>
                  <td class="sum-cell">{{ rowTotal(row) }}</td>
                </tr>
              </tbody>
              <tfoot>
                <tr>
                  <th class="sticky-left">合计</th>
                  <th v-for="size in selectedSizes" :key="size" class="size-col">{{ sizeTotals[size] || 0 }}</th>
                  <th>{{ grandTotal }}</th>
                </tr>
              </tfoot>
            </table>
          </div>
          <!-- 约束提醒 -->
          <div v-if="violationMessages.length" class="tool-block warning" style="flex: 0 0 100%; margin-top: 12px;">
            <h4>约束提醒</h4>
            <ul>
              <li v-for="(msg,i) in violationMessages" :key="i">{{ msg }}</li>
            </ul>
          </div>
        </div>

        <!-- 底部统计 -->
        <div class="stats">
          <div class="stats-left">
            <div v-for="row in tableRows" :key="row.color" class="stat-item">
              <span class="label">{{ row.color }} 合计：</span>
              <span class="value">{{ rowTotal(row) }}</span>
            </div>
          </div>
          <div class="stats-right">
            <span class="total-label">总计件数：</span>
            <span class="total-value">{{ grandTotal }}</span>
          </div>
        </div>
      </el-card>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, DocumentCopy } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const customerLoading = ref(false)
const styleLoading = ref(false)

// 表单类型定义
interface SizeRatio { size: string; quantity?: number }
interface FormModel {
  orderNo: string
  customerId?: number
  styleId?: number
  totalQuantity?: number
  urgentLevel: 'normal' | 'urgent' | 'very_urgent'
  expectedFinishTime?: Date | string
  remark: string
  sizeRatios: SizeRatio[]
  defaultPrice?: number
  // 新增字段
  cutSeqNo?: string
  externalOrderNo?: string
  departmentId?: number
  cutDate?: Date | string
  deliveryDate?: Date | string
  nonInvoiceRemark?: string
  suffixRemark?: string
  companyName?: string
}

// 表单数据
const form = reactive<FormModel>({
  orderNo: '',
  customerId: undefined,
  styleId: undefined,
  totalQuantity: undefined,
  urgentLevel: 'normal',
  expectedFinishTime: undefined,
  remark: '',
  sizeRatios: [
    { size: '', quantity: undefined }
  ],
  defaultPrice: undefined,
  cutSeqNo: '',
  externalOrderNo: '',
  departmentId: undefined,
  cutDate: undefined,
  deliveryDate: undefined,
  nonInvoiceRemark: '',
  suffixRemark: '',
  companyName: ''
})

// 验证规则
const rules = {
  orderNo: [
    { required: true, message: '请输入裁床单号', trigger: 'blur' }
  ],
  customerId: [
    { required: true, message: '请选择客户', trigger: 'change' }
  ],
  // styleId 校验由 handleSubmit 兜底校验
  totalQuantity: [
    { required: true, message: '请输入总数量', trigger: 'blur' }
  ]
}

// 本地类型与存储键
type CustomerOption = { id: number; name: string }
type StyleOption = { id: number; styleCode: string; styleName: string; processCount: number; defaultPrice: number }
type DepartmentOption = { id: number; name: string }
type MatrixRow = { color: string; qty: Record<string, number> }
type CutOrderOptionLocal = {
  id: number; orderNo: string; styleName: string; totalQuantity: number;
  sizes?: Array<{ size: string; quantity: number }>; // 兼容老字段
  createdAt?: string; expectedFinishTime?: string; customerId?: number; styleId?: number; remark?: string; urgentLevel?: string; defaultPrice?: number;
  // 新增保存字段
  cutSeqNo?: string; externalOrderNo?: string; departmentId?: number; cutDate?: string; deliveryDate?: string; nonInvoiceRemark?: string; suffixRemark?: string; companyName?: string;
  colors?: string[]; selectedSizes?: string[]; matrix?: MatrixRow[]; grandTotal?: number
}

const CUTORDER_KEY = 'local_cut_orders_v1'
const CUSTOMER_KEY = 'local_customers_v1'
const STYLE_KEY = 'local_styles_v1'
const DEPT_KEY = 'local_departments_v1'

const ensureSeedCustomers = (): CustomerOption[] => {
  try {
    const raw = localStorage.getItem(CUSTOMER_KEY)
    if (raw) return JSON.parse(raw) as CustomerOption[]
  } catch {}
  const seed: CustomerOption[] = [
    { id: 1, name: '宁波雅戈尔' },
    { id: 2, name: '海澜之家' },
    { id: 3, name: '森马服饰' },
    { id: 4, name: '太平鸟' }
  ]
  localStorage.setItem(CUSTOMER_KEY, JSON.stringify(seed))
  return seed
}

const ensureSeedStyles = (): StyleOption[] => {
  try {
    const raw = localStorage.getItem(STYLE_KEY)
    if (raw) return JSON.parse(raw) as StyleOption[]
  } catch {}
  const seed: StyleOption[] = [
    { id: 11, styleCode: 'TS-2401', styleName: '春季T恤', processCount: 18, defaultPrice: 3.5 },
    { id: 12, styleCode: 'HD-2402', styleName: '休闲卫衣', processCount: 22, defaultPrice: 4.2 },
    { id: 13, styleCode: 'JK-2403', styleName: '轻薄夹克', processCount: 28, defaultPrice: 5.6 }
  ]
  localStorage.setItem(STYLE_KEY, JSON.stringify(seed))
  return seed
}

const ensureSeedDepartments = (): DepartmentOption[] => {
  try {
    const raw = localStorage.getItem(DEPT_KEY)
    if (raw) return JSON.parse(raw) as DepartmentOption[]
  } catch {}
  const seed: DepartmentOption[] = [
    { id: 101, name: '裁床车间' },
    { id: 102, name: '缝制一车间' },
    { id: 103, name: '缝制二车间' },
    { id: 104, name: '后整包装' }
  ]
  localStorage.setItem(DEPT_KEY, JSON.stringify(seed))
  return seed
}

const nowText = () => {
  const d = new Date()
  const p = (n: number) => n.toString().padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

// 计算比例（保留用于尺码配比卡片兼容）
const calculateRatio = (quantity?: number) => {
  if (!form.totalQuantity || !quantity) return 0
  return ((quantity / form.totalQuantity) * 100).toFixed(1)
}

// 选中款式详情
const selectedStyleDetail = computed<StyleOption | null>(() => {
  if (!form.styleId) return null
  const all = ensureSeedStyles()
  return all.find(s => s.id === form.styleId) || null
})

// 打开/确认款式弹窗
const styleDialogVisible = ref(false)
const styleSelectTemp = ref<number | undefined>(undefined)
const openStyleDialog = () => {
  styleSelectTemp.value = form.styleId
  styleDialogVisible.value = true
}
const confirmSelectStyle = () => {
  if (styleSelectTemp.value) {
    form.styleId = styleSelectTemp.value
    const st = ensureSeedStyles().find(s => s.id === styleSelectTemp.value)
    if (st && (form.defaultPrice === undefined || form.defaultPrice === null)) {
      form.defaultPrice = st.defaultPrice
    }
  }
  styleDialogVisible.value = false
}

watch(() => form.styleId, (val) => {
  if (!val) return
  const st = ensureSeedStyles().find(s => s.id === val)
  if (st && (form.defaultPrice === undefined || form.defaultPrice === null)) {
    form.defaultPrice = st.defaultPrice
  }
})

// 组合选择与表格数据
const sizeOptions = ref<string[]>(['S/46','M/48','L/50','XL/52','2XL/54','3XL/56','4XL/58'])
const colorOptions = ref<string[]>(['黑色','白色','藏青','墨绿','大红'])
const selectedSizes = ref<string[]>([])
const selectedColors = ref<string[]>([])
const tableRows = ref<MatrixRow[]>([])

// 同步颜色/尺码变化到表格
watch(selectedColors, (colors) => {
  // 添加新颜色行
  colors.forEach(c => {
    if (!tableRows.value.find(r => r.color === c)) {
      const qty: Record<string, number> = {}
      selectedSizes.value.forEach(s => qty[s] = 0)
      tableRows.value.push({ color: c, qty })
    }
  })
  // 移除未选颜色
  tableRows.value = tableRows.value.filter(r => colors.includes(r.color))
}, { deep: true })

watch(selectedSizes, (sizes) => {
  // 为所有行补齐/移除尺码列
  tableRows.value.forEach(r => {
    // 移除未选
    Object.keys(r.qty).forEach(k => { if (!sizes.includes(k)) delete r.qty[k] })
    // 添加新选
    sizes.forEach(s => { if (!(s in r.qty)) r.qty[s] = 0 })
  })
}, { deep: true })

// 汇总
const rowTotal = (row: MatrixRow) => Object.values(row.qty).reduce((a,b) => a + (Number(b) || 0), 0)
const sizeTotals = computed<Record<string, number>>(() => {
  const map: Record<string, number> = {}
  selectedSizes.value.forEach(s => { map[s] = 0 })
  tableRows.value.forEach(r => {
    selectedSizes.value.forEach(s => { map[s] += Number(r.qty[s] || 0) })
  })
  return map
})
const grandTotal = computed<number>(() => Object.values(sizeTotals.value).reduce((a,b) => a + b, 0))

// 单元格变更时校验倍数限制
const multipleLimit = ref<number>(0)
const perBundleLimit = ref<number>(0)
const violationMessages = ref<string[]>([])
const onCellChange = (_rIdx: number, size: string) => {
  violationMessages.value = []
  if (multipleLimit.value > 0) {
    tableRows.value.forEach(r => {
      const v = Number(r.qty[size] || 0)
      if (v % multipleLimit.value !== 0) {
        violationMessages.value.push(`${r.color}-${size} 数量(${v})非 ${multipleLimit.value} 的倍数`)
      }
    })
  }
  if (perBundleLimit.value > 0) {
    tableRows.value.forEach(r => {
      const v = Number(r.qty[size] || 0)
      if (v > perBundleLimit.value) {
        violationMessages.value.push(`${r.color}-${size} 数量(${v})超过每扎上限 ${perBundleLimit.value}`)
      }
    })
  }
}

// 右侧工具逻辑
const bundlePrefix = ref<string>('')
const rowCopySource = ref<string | undefined>(undefined)
const colCopySource = ref<string | undefined>(undefined)

const copyRowToOthers = () => {
  if (!rowCopySource.value) return
  const src = tableRows.value.find(r => r.color === rowCopySource.value)
  if (!src) return
  tableRows.value.forEach(r => {
    if (r.color !== src.color) {
      selectedSizes.value.forEach(s => { r.qty[s] = Number(src.qty[s] || 0) })
    }
  })
  ElMessage.success('已复制到其他颜色')
}

const copyColToOthers = () => {
  if (!colCopySource.value) return
  const s = colCopySource.value
  selectedSizes.value.forEach(size => {
    if (size === s) return
    tableRows.value.forEach(r => { r.qty[size] = Number(r.qty[s] || 0) })
  })
  ElMessage.success('已复制到其他尺码')
}

// 图标触发：从尺码表头/颜色单元触发复制
const copyRowToOthersWithColor = (color: string) => {
  rowCopySource.value = color
  copyRowToOthers()
}
const copyColToOthersWithSize = (size: string) => {
  colCopySource.value = size
  copyColToOthers()
}

const layerPrefix = ref<string>('L')
const layerStart = ref<number>(1)
const genLayerPreview = computed(() => `${layerPrefix.value}${String(layerStart.value).padStart(2,'0')}, ${layerPrefix.value}${String(layerStart.value+1).padStart(2,'0')}`)

const clearTable = async () => {
  try {
    await ElMessageBox.confirm('确定清空裁床表所有录入数据？', '提示', { type: 'warning' })
    tableRows.value.forEach(r => {
      Object.keys(r.qty).forEach(k => r.qty[k] = 0)
    })
    violationMessages.value = []
    ElMessage.success('已清空')
  } catch {}
}

// 自动补数：将差值填入最后一行最后一列
const autoSupplement = async () => {
  if (!form.totalQuantity || selectedColors.value.length === 0 || selectedSizes.value.length === 0) {
    ElMessage.warning('请先设置总数量、颜色与尺码组合')
    return
  }
  const current = grandTotal.value
  const diff = Number(form.totalQuantity) - current
  if (diff === 0) {
    ElMessage.info('当前总数已与目标一致')
    return
  }
  try {
    await ElMessageBox.confirm(`将把差值 ${diff > 0 ? '+'+diff : diff} 补到最后单元格，是否继续？`, '自动补数', { type: 'info' })
    const lastRow = tableRows.value[tableRows.value.length - 1]
    const lastSize = selectedSizes.value[selectedSizes.value.length - 1]
    const next = Math.max(0, Number(lastRow.qty[lastSize] || 0) + diff)
    lastRow.qty[lastSize] = next
    onCellChange(tableRows.value.length - 1, lastSize)
    ElMessage.success('已自动补数')
  } catch {}
}

// 客户/款式/部门下拉
const customerList = ref<CustomerOption[]>([])
const styleList = ref<StyleOption[]>([])
const deptList = ref<DepartmentOption[]>([])

const searchCustomers = async (query: string) => {
  customerLoading.value = true
  try {
    const all = ensureSeedCustomers()
    const q = (query || '').trim().toLowerCase()
    customerList.value = q ? all.filter(c => c.name.toLowerCase().includes(q)) : all.slice(0, 20)
  } catch (error) {
    ElMessage.error('搜索客户失败')
  } finally {
    customerLoading.value = false
  }
}

const searchStyles = async (query: string) => {
  styleLoading.value = true
  try {
    const all = ensureSeedStyles()
    const q = (query || '').trim().toLowerCase()
    styleList.value = q
      ? all.filter(s => `${s.styleCode} ${s.styleName}`.toLowerCase().includes(q))
      : all.slice(0, 20)
  } catch (error) {
    ElMessage.error('搜索款式失败')
  } finally {
    styleLoading.value = false
  }
}

// 工具：序列化时间
const toIso = (v: unknown): string | undefined => {
  if (v instanceof Date) return v.toISOString()
  if (typeof v === 'string') return v
  return undefined
}

// 提交表单（本地保存）
const handleSubmit = async () => {
   if (!formRef.value) return
   try {
     await formRef.value.validate()
     if (!form.styleId) { ElMessage.error('请选择款式'); return }
     // 校验：至少一个颜色与尺码
     if (selectedColors.value.length === 0 || selectedSizes.value.length === 0) {
       ElMessage.error('请设置颜色与尺码组合')
       return
     }
     // 校验：总数一致
     if (!form.totalQuantity || grandTotal.value !== Number(form.totalQuantity)) {
       ElMessage.error(`当前总数(${grandTotal.value})必须等于总数量(${form.totalQuantity || 0})`)
       return
     }

     loading.value = true
     const selectedStyle = ensureSeedStyles().find(s => s.id === form.styleId) || null
     const listRaw = localStorage.getItem(CUTORDER_KEY)
     const list: CutOrderOptionLocal[] = listRaw ? JSON.parse(listRaw) as CutOrderOptionLocal[] : []
     const item: CutOrderOptionLocal = {
       id: Date.now(),
       orderNo: form.orderNo,
       styleName: selectedStyle ? selectedStyle.styleName : '未知款式',
       totalQuantity: form.totalQuantity || 0,
       // 兼容旧字段
       sizes: selectedSizes.value.map(s => ({ size: s, quantity: sizeTotals.value[s] || 0 })),
       createdAt: nowText(),
       expectedFinishTime: toIso(form.expectedFinishTime),
       customerId: form.customerId || undefined,
       styleId: form.styleId || undefined,
       remark: form.remark || undefined,
       urgentLevel: form.urgentLevel || undefined,
       defaultPrice: form.defaultPrice || undefined,
       // 新字段
       cutSeqNo: form.cutSeqNo || undefined,
       externalOrderNo: form.externalOrderNo || undefined,
       departmentId: form.departmentId || undefined,
       cutDate: toIso(form.cutDate),
       deliveryDate: toIso(form.deliveryDate),
       nonInvoiceRemark: form.nonInvoiceRemark || undefined,
       suffixRemark: form.suffixRemark || undefined,
       companyName: form.companyName || undefined,
       colors: [...selectedColors.value],
       selectedSizes: [...selectedSizes.value],
       matrix: JSON.parse(JSON.stringify(tableRows.value)),
       grandTotal: grandTotal.value
     }
     list.unshift(item)
     localStorage.setItem(CUTORDER_KEY, JSON.stringify(list))
     ElMessage.success('创建成功（本地）')
     router.push('/production/cut-orders')
   } catch (error) {
     console.error('创建失败:', error)
     ElMessage.error('创建失败')
   } finally {
     loading.value = false
   }
 }

// 取消
const handleCancel = () => {
  router.back()
}

onMounted(() => {
  // 生成默认裁床单号
  const now = new Date()
  const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
  const timeStr = now.getTime().toString().slice(-4)
  form.orderNo = `CO${dateStr}${timeStr}`
  // 初始化下拉数据
  searchCustomers('')
  searchStyles('')
  deptList.value = ensureSeedDepartments()
})

// 新增：添加颜色行
const addColorRow = async () => {
  try {
    const { value } = await ElMessageBox.prompt('输入要新增的颜色名称', '添加颜色', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：黑色',
      inputValidator: (val: string) => !!val && val.trim().length > 0 || '颜色名称不能为空'
    })
    const name = String(value).trim()
    if (!selectedColors.value.includes(name)) {
      selectedColors.value.push(name)
      ElMessage.success(`已添加颜色：${name}`)
    } else {
      ElMessage.info('该颜色已存在')
    }
  } catch {}
}
</script>

<style scoped>
.create-form { max-width: none; width: 100%; }
.form-card { margin-bottom: 20px; }
.form-row { display: flex; gap: 20px; }
.form-row .el-form-item { flex: 1; }
.form-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 12px 16px; }
.inline-field { display: flex; gap: 8px; align-items: center; }

/* 款式信息 */
.style-info { display: grid; grid-template-columns: repeat(2, minmax(240px, 1fr)); gap: 12px 24px; }
.style-field { display: flex; align-items: center; gap: 8px; }
.style-field label { color: #666; }
.style-text { font-weight: 600; }

/* 裁床表布局 */
.sheet-layout { display: flex; gap: 16px; flex-wrap: wrap; }
.sheet-table-wrapper { flex: 1; overflow-x: auto; }
.sheet-topbar { display:flex; justify-content: space-between; align-items:center; margin-bottom:12px; }
.sheet-table-wrapper { flex: 1 1 100%; overflow-x: auto; }
.sheet-left-actions { margin-bottom: 8px; }
.sheet-table { width: 100%; border-collapse: collapse; min-width: 1100px; }
.sheet-table th, .sheet-table td { border: 1px solid #e5e7eb; padding: 8px; text-align: center; white-space: nowrap; }
.sheet-table thead th { background: #fafafa; position: sticky; top: 0; z-index: 1; }
 .sticky-left { position: sticky; left: 0; background: #fff; z-index: 2; }
 .sum-cell { font-weight: 600; color: #1f2937; }
 .color-name { font-weight: 600; }
 .color-cell { display:flex; align-items:center; gap:6px; }
 .size-col { width: 120px; }
 .size-header { display:flex; align-items:center; justify-content:center; gap:6px; }
 .icon-btn { padding: 0; height: 22px; }
 
 /* 右侧工具 */
 .tool-block { margin-bottom: 14px; }
 .tool-block h4 { margin: 0 0 8px; font-size: 14px; color: #333; }
 .two-cols { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
 .mt8 { margin-top: 8px; }
 .layer-preview { margin-top: 6px; color: #666; font-size: 12px; }
 .tool-block.warning { border: 1px dashed #f59e0b; padding: 8px; border-radius: 4px; background: #fffbeb; }
 .tool-block.warning ul { padding-left: 18px; margin: 6px 0 0; }
 </style>




