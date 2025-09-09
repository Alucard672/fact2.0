<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="裁床单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入裁床单号" clearable />
        </el-form-item>
        <el-form-item label="款式">
          <el-select v-model="searchForm.styleId" placeholder="请选择款式" clearable filterable>
            <el-option
              v-for="item in styleList"
              :key="item.id"
              :label="`${item.styleCode} - ${item.styleName}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" value="draft" />
            <el-option label="已确认" value="confirmed" />
            <el-option label="裁剪中" value="cutting" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="裁剪日期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
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
          新建裁床单
        </el-button>
        <el-button type="success" @click="handleBatchPrint" :disabled="!selectedRows.length">
          <el-icon><Printer /></el-icon>
          批量打印菲票
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-select v-model="printLayout" size="small" style="width: 160px; margin-right: 8px" placeholder="打印布局">
          <el-option label="A4 单列(默认)" value="a4-1" />
          <el-option label="A4 两列" value="a4-2" />
          <el-option label="58mm 工位贴纸" value="58" />
        </el-select>
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
      <el-table-column prop="orderNo" label="裁床单号" width="150" fixed="left">
        <template #default="{ row }">
          <el-link type="primary" @click="handleView(row)">{{ row.orderNo }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="styleCode" label="款号" width="100" />
      <el-table-column prop="styleName" label="款式名称" min-width="150" />
      <el-table-column prop="color" label="颜色" width="80" />
      <el-table-column prop="bedNo" label="床次" width="80" />
      <el-table-column prop="totalLayers" label="层数" width="80">
        <template #default="{ row }">
          {{ row.totalLayers }}层
        </template>
      </el-table-column>
      <el-table-column prop="cuttingType" label="裁切方式" width="100">
        <template #default="{ row }">
          <el-tag :type="row.cuttingType === 'average' ? 'primary' : 'warning'">
            {{ row.cuttingType === 'average' ? '平均裁' : '分层段裁' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalQuantity" label="总数量" width="100">
        <template #default="{ row }">
          {{ row.totalQuantity.toLocaleString() }}件
        </template>
      </el-table-column>
      <el-table-column prop="bundleCount" label="包数" width="80">
        <template #default="{ row }">
          {{ row.bundleCount || 0 }}包
        </template>
      </el-table-column>
      <el-table-column prop="cuttingDate" label="裁剪日期" width="110" />
      <el-table-column prop="deliveryDate" label="交期" width="110">
        <template #default="{ row }">
          <span :class="{ 'text-danger': isOverdue(row.deliveryDate) }">
            {{ row.deliveryDate }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="90">
        <template #default="{ row }">
          <el-tag :type="getPriorityType(row.priority)" effect="dark" size="small">
            {{ getPriorityText(row.priority) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button link type="primary" @click="handleEdit(row)" v-if="row.status === 'draft'">编辑</el-button>
          <el-button link type="primary" @click="handleConfirm(row)" v-if="row.status === 'draft'">确认</el-button>
          <el-button link type="primary" @click="handleGenerateBundles(row)" v-if="row.status === 'confirmed'">生成包</el-button>
          <el-button link type="warning" @click="handleRestoreCut(row)" v-if="['confirmed','cutting'].includes(row.status)">还原裁床</el-button>
          <el-button link type="primary" @click="handlePreview(row)">预览</el-button>
          <el-button link type="primary" @click="handlePrint(row)" v-if="(row.bundleCount || 0) > 0">打印</el-button>
          <el-button link type="danger" @click="handleCancel(row)" v-if="['draft', 'confirmed'].includes(row.status)">取消</el-button>
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

    <!-- 查看详情对话框 -->
    <el-dialog v-model="detailVisible" title="裁床单详情" width="900px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="裁床单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentOrder.status)">
            {{ getStatusText(currentOrder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="款号">{{ currentOrder.styleCode }}</el-descriptions-item>
        <el-descriptions-item label="款式名称">{{ currentOrder.styleName }}</el-descriptions-item>
        <el-descriptions-item label="颜色">{{ currentOrder.color }}</el-descriptions-item>
        <el-descriptions-item label="床次">{{ currentOrder.bedNo }}</el-descriptions-item>
        <el-descriptions-item label="总层数">{{ currentOrder.totalLayers }}层</el-descriptions-item>
        <el-descriptions-item label="裁切方式">
          {{ currentOrder.cuttingType === 'average' ? '平均裁' : '分层段裁' }}
        </el-descriptions-item>
        <el-descriptions-item label="总数量">{{ currentOrder.totalQuantity?.toLocaleString() }}件</el-descriptions-item>
        <el-descriptions-item label="包数">{{ currentOrder.bundleCount || 0 }}包</el-descriptions-item>
        <el-descriptions-item label="裁剪日期">{{ currentOrder.cuttingDate }}</el-descriptions-item>
        <el-descriptions-item label="交期">{{ currentOrder.deliveryDate }}</el-descriptions-item>
        <el-descriptions-item label="优先级" :span="2">
          <el-tag :type="getPriorityType(currentOrder.priority)" effect="dark" size="small">
            {{ getPriorityText(currentOrder.priority) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 尺码比例 -->
      <div class="section-title">尺码比例</div>
      <el-table :data="sizeRatioTableData" border>
        <el-table-column prop="size" label="尺码" width="100" />
        <el-table-column prop="ratio" label="比例(%)" width="100" />
        <el-table-column prop="quantity" label="数量" />
      </el-table>

      <!-- 分层段方案（仅分层段裁显示） -->
      <template v-if="currentOrder.cuttingType === 'segment'">
        <div class="section-title">分层段方案</div>
        <el-table :data="segmentPlanData" border>
          <el-table-column prop="segment" label="层段" width="100" />
          <el-table-column prop="layerRange" label="层范围" width="120" />
          <el-table-column prop="sizeRatio" label="尺码比例" />
        </el-table>
      </template>

      <!-- 包列表 -->
      <template v-if="(currentOrder.bundleCount || 0) > 0">
        <div class="section-title">包列表</div>
        <el-table :data="bundleList" border max-height="300">
          <el-table-column type="index" width="50" />
          <el-table-column prop="bundleNo" label="包号" width="120" />
          <el-table-column prop="size" label="尺码" width="80" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="layerFrom" label="起始层" width="80" v-if="currentOrder.cuttingType === 'segment'" />
          <el-table-column prop="layerTo" label="结束层" width="80" v-if="currentOrder.cuttingType === 'segment'" />
          <el-table-column prop="segmentTag" label="层段" width="80" v-if="currentOrder.cuttingType === 'segment'">
            <template #default="{ row }">
              <el-tag :type="getSegmentType(row.segmentTag)" size="small">
                {{ row.segmentTag }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="getBundleStatusType(row.status)" size="small">
                {{ getBundleStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="qrCode" label="二维码" width="100">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="showQrCode(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>

    <!-- 预览抽屉 -->
    <el-drawer
      v-model="previewVisible"
      title="裁床单预览"
      :size="'80%'"
      direction="rtl"
      :destroy-on-close="true"
    >
      <div class="preview-toolbar">
        <div class="left">
          <el-select v-model="previewLayout" size="small" style="width: 160px; margin-right: 8px" placeholder="预览布局">
            <el-option label="A4 单列(默认)" value="a4-1" />
            <el-option label="A4 两列" value="a4-2" />
            <el-option label="58mm 工位贴纸" value="58" />
          </el-select>
          <el-button size="small" @click="refreshPreview">刷新预览</el-button>
        </div>
        <div class="right">
          <el-button type="primary" size="small" @click="handlePreviewPrint">打印当前预览</el-button>
        </div>
      </div>
      <div class="preview-container">
        <iframe ref="previewIframe" class="preview-iframe"></iframe>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Printer, Download, Refresh } from '@element-plus/icons-vue'
import type { CutOrder, Bundle } from '@/types/api'
import dayjs from 'dayjs'
import { post, get } from '@/utils/request'

// 打印布局（A4 单列 / A4 两列 / 58mm 工位贴纸）
const printLayout = ref<'a4-1'|'a4-2'|'58'>('a4-1')
const router = useRouter()

// 预览状态
const previewVisible = ref(false)
const previewRows = ref<Partial<CutOrder>[]>([])
const previewLayout = ref<'a4-1'|'a4-2'|'58'>(printLayout.value)
const previewIframe = ref<HTMLIFrameElement | null>(null)

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  styleId: null as number | null,
  status: '',
  dateRange: [] as string[]
})

// 分页参数
const pageParams = reactive({
  current: 1,
  size: 20
})

// 状态变量
const loading = ref(false)
const tableData = ref<Partial<CutOrder>[]>([])
const total = ref(0)
const selectedRows = ref<Partial<CutOrder>[]>([])
const detailVisible = ref(false)
const currentOrder = ref<Partial<CutOrder>>({})
const bundleList = ref<Bundle[]>([])

// 款式列表（实际从API获取）
const styleList = ref([
  { id: 1, styleCode: 'ST001', styleName: '经典T恤' },
  { id: 2, styleCode: 'ST002', styleName: '休闲裤' },
  { id: 3, styleCode: 'ST003', styleName: '连衣裙' }
])

// 尺码比例表格数据
const sizeRatioTableData = computed(() => {
  if (!currentOrder.value.sizeRatio) return []
  return Object.entries(currentOrder.value.sizeRatio).map(([size, ratio]) => ({
    size,
    ratio: ratio as number,
    quantity: Math.round((currentOrder.value.totalQuantity || 0) * (ratio as number) / 100)
  }))
})

// 分层段方案数据
const segmentPlanData = computed(() => {
  if (!currentOrder.value.segmentPlan) return []
  return (currentOrder.value as any).segmentPlan.segments || []
})

// 全量数据（模拟后端返回的完整数据集）
const allData = ref<Partial<CutOrder>[]>([
  {
    id: 1,
    tenantId: 1,
    orderNo: 'CUT202401001',
    styleId: 1,
    styleCode: 'ST001',
    styleName: '经典T恤',
    color: '白色',
    bedNo: 'B001',
    totalLayers: 100,
    cuttingType: 'average',
    sizeRatio: { S: 20, M: 30, L: 30, XL: 20 },
    totalQuantity: 5000,
    bundleCount: 50,
    cuttingDate: '2024-01-15',
    deliveryDate: '2024-01-25',
    priority: 'high',
    status: 'confirmed',
    createdBy: 1,
    createdAt: '2024-01-10'
  },
  {
    id: 2,
    tenantId: 1,
    orderNo: 'CUT202401002',
    styleId: 2,
    styleCode: 'ST002',
    styleName: '休闲裤',
    color: '黑色',
    bedNo: 'B002',
    totalLayers: 80,
    cuttingType: 'segment',
    sizeRatio: { '28': 15, '30': 25, '32': 30, '34': 20, '36': 10 },
    segmentPlan: {
      segments: [
        { segment: 'HIGH', layerRange: '1-40', sizeRatio: '28:10, 30:20, 32:35, 34:25, 36:10' },
        { segment: 'LOW', layerRange: '41-80', sizeRatio: '28:20, 30:30, 32:25, 34:15, 36:10' }
      ]
    },
    totalQuantity: 3000,
    bundleCount: 30,
    cuttingDate: '2024-01-16',
    deliveryDate: '2024-01-26',
    priority: 'medium',
    status: 'cutting',
    createdBy: 1,
    createdAt: '2024-01-11'
  },
  {
    id: 3,
    tenantId: 1,
    orderNo: 'CUT202401003',
    styleId: 3,
    styleCode: 'ST003',
    styleName: '连衣裙',
    color: '红色',
    bedNo: 'B003',
    totalLayers: 60,
    cuttingType: 'average',
    sizeRatio: { XS: 10, S: 25, M: 35, L: 20, XL: 10 },
    totalQuantity: 2400,
    bundleCount: 0,
    cuttingDate: '2024-01-17',
    deliveryDate: '2024-01-27',
    priority: 'low',
    status: 'draft',
    createdBy: 1,
    createdAt: '2024-01-12'
  },
  {
    id: 4,
    tenantId: 1,
    orderNo: 'CUT202401004',
    styleId: 1,
    styleCode: 'ST001',
    styleName: '经典T恤',
    color: '蓝色',
    bedNo: 'B004',
    totalLayers: 120,
    cuttingType: 'segment',
    sizeRatio: { S: 15, M: 35, L: 35, XL: 15 },
    segmentPlan: {
      segments: [
        { segment: 'A', layerRange: '1-60', sizeRatio: 'S:20, M:30, L:30, XL:20' },
        { segment: 'B', layerRange: '61-120', sizeRatio: 'S:10, M:40, L:40, XL:10' }
      ]
    },
    totalQuantity: 6000,
    bundleCount: 15,
    cuttingDate: '2024-01-18',
    deliveryDate: '2024-01-28',
    priority: 'urgent',
    status: 'completed',
    createdBy: 1,
    createdAt: '2024-01-13'
  },
  {
    id: 5,
    tenantId: 1,
    orderNo: 'CUT202401005',
    styleId: 2,
    styleCode: 'ST002',
    styleName: '休闲裤',
    color: '灰色',
    bedNo: 'B005',
    totalLayers: 90,
    cuttingType: 'average',
    sizeRatio: { '28': 20, '30': 30, '32': 25, '34': 15, '36': 10 },
    totalQuantity: 3600,
    bundleCount: 0,
    cuttingDate: '2024-01-19',
    deliveryDate: '2024-01-29',
    priority: 'medium',
    status: 'cancelled',
    createdBy: 1,
    createdAt: '2024-01-14'
  }
])

// 本地筛选逻辑
const applyFilter = (data: Partial<CutOrder>[]) => {
  return data.filter(item => {
    if (searchForm.orderNo && !item.orderNo?.includes(searchForm.orderNo.trim())) return false
    if (searchForm.styleId && item.styleId !== searchForm.styleId) return false
    if (searchForm.status && item.status !== searchForm.status) return false
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      const [startDate, endDate] = searchForm.dateRange
      const itemDate = dayjs(item.cuttingDate)
      if (itemDate.isBefore(dayjs(startDate)) || itemDate.isAfter(dayjs(endDate))) return false
    }
    return true
  })
}

// 本地分页逻辑
const applyPagination = (data: Partial<CutOrder>[]) => {
  const start = (pageParams.current - 1) * pageParams.size
  return data.slice(start, start + pageParams.size)
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    setTimeout(() => {
      const filtered = applyFilter(allData.value)
      total.value = filtered.length
      tableData.value = applyPagination(filtered)
      loading.value = false
    }, 300)
  } catch (error) {
    console.error('获取数据失败:', error)
    loading.value = false
  }
}

// 搜索/重置
const handleSearch = () => { pageParams.current = 1; fetchData() }
const handleReset = () => {
  Object.assign(searchForm, { orderNo: '', styleId: null, status: '', dateRange: [] })
  handleSearch()
}

// 新建/详情/编辑/确认
const handleCreate = () => { router.push('/production/cut-orders/create') }
const handleView = async (row: Partial<CutOrder>) => {
  currentOrder.value = row
  bundleList.value = [
    { id: 1, tenantId: 1, cutOrderId: (row.id as number) || 0, bundleNo: 'BDL001', size: 'M', color: row.color || '-', quantity: 100, status: 'pending', qrCode: 'BDL-1-1-1-check' },
    { id: 2, tenantId: 1, cutOrderId: (row.id as number) || 0, bundleNo: 'BDL002', size: 'L', color: row.color || '-', quantity: 100, layerFrom: 1, layerTo: 40, segmentTag: 'HIGH', status: 'in_work', qrCode: 'BDL-1-1-2-HIGH-check' }
  ] as any
  detailVisible.value = true
}
const handleEdit = (row: Partial<CutOrder>) => { router.push(`/production/cut-orders/edit/${row?.id}`) }
const handleConfirm = async (row: Partial<CutOrder>) => {
  ElMessageBox.confirm(`确认裁床单 ${row?.orderNo} 吗？确认后将不能修改`, '提示', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' })
    .then(() => { ElMessage.success('确认成功'); fetchData() })
}

// 本地存储与工具（与扎包管理兼容）
const BUNDLE_KEY = 'local_bundles_v1'

type LocalBundleRecord = {
  id: number
  bundleCode: string
  cutOrderId: number
  cutOrderNo: string
  styleName: string
  size: string
  quantity: number
  processName: string
  workerName: string
  status: 'pending' | 'in_work' | 'completed' | 'returned'
  createdAt: string
}

const loadAllBundles = (): LocalBundleRecord[] => {
  try { const raw = localStorage.getItem(BUNDLE_KEY); return raw ? (JSON.parse(raw) as LocalBundleRecord[]) : [] } catch { return [] }
}
const saveAllBundles = (list: LocalBundleRecord[]) => { localStorage.setItem(BUNDLE_KEY, JSON.stringify(list)) }
const genBundleCode = (seq: number) => {
  const date = new Date()
  const y = date.getFullYear().toString().padStart(4, '0')
  const m = (date.getMonth() + 1).toString().padStart(2, '0')
  const d = date.getDate().toString().padStart(2, '0')
  return `B${y}${m}${d}${seq.toString().padStart(3, '0')}`
}
const nowText = () => {
  const d = new Date(); const p = (n: number) => n.toString().padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

// 公共：获取状态/优先级展示
const getPriorityType = (p: any) => ({ urgent: 'danger', high: 'warning', medium: 'primary', low: 'info' } as any)[p] || 'info'
const getPriorityText = (p: any) => ({ urgent: '加急', high: '高', medium: '中', low: '低' } as any)[p] || '-'
const getStatusType = (s: any) => ({ draft: 'info', confirmed: 'primary', cutting: 'warning', completed: 'success', cancelled: 'danger' } as any)[s] || 'info'
const getStatusText = (s: any) => ({ draft: '草稿', confirmed: '已确认', cutting: '裁剪中', completed: '已完成', cancelled: '已取消' } as any)[s] || '-'
const getSegmentType = (t: any) => ({ HIGH: 'danger', LOW: 'success', A: 'primary', B: 'warning' } as any)[t] || 'info'
const getBundleStatusType = (s: any) => ({ pending: 'info', in_work: 'warning', completed: 'success', returned: 'danger' } as any)[s] || 'info'
const getBundleStatusText = (s: any) => ({ pending: '待处理', in_work: '加工中', completed: '已完成', returned: '退回' } as any)[s] || '-'

// 打印/预览 HTML 生成
const makePrintHTML = (rows: Partial<CutOrder>[], layout: 'a4-1'|'a4-2'|'58', withPrintScript = false) => {
  if (!rows || rows.length === 0) return '<!doctype html><html><body>无数据</body></html>'
  const escape = (s: any) => (s ?? '').toString().replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  const header = (title: string, qrText?: string) => `
    <header class="header">
      <div class="brand">
        <img src="/logo.png" alt="Logo" onerror="this.style.display='none'" />
        <div class="brand-text">
          <div class="brand-name">裁床生产管理</div>
          <div class="brand-sub">Cutting Management System</div>
        </div>
      </div>
      <div class="title">${title}</div>
      <div class="qr">
        ${qrText ? `<img src="https://chart.googleapis.com/chart?cht=qr&chs=120x120&chl=${encodeURIComponent(qrText)}" alt="QR" onerror="this.style.display='none'" />` : ''}
        ${qrText ? `<div class="qr-text">${escape(qrText)}</div>` : ''}
      </div>
    </header>`
  const footer = () => `<footer class="footer">打印时间：${nowText()}</footer>`

  let pageRule = '@page { size: A4; margin: 10mm }'
  let sheetWidth = '100%'
  let qrSize = '120px'
  let fontBase = '12px'
  if (layout === 'a4-2') { sheetWidth = '48%'; qrSize = '96px'; fontBase = '11px' }
  if (layout === '58') { pageRule = '@page { size: 58mm auto; margin: 3mm }'; qrSize = '90px'; fontBase = '11px' }

  const sections = rows.map((r) => {
    const sizeRatio = r.sizeRatio ? Object.entries(r.sizeRatio).map(([size, ratio]) => ({ size, ratio: ratio as number, qty: Math.round((r.totalQuantity || 0) * (ratio as number) / 100) })) : []
    const segmentRows = (r as any).segmentPlan?.segments ?? []
    const qrContent = r.orderNo ? `CUT-${r.orderNo}` : ''
    return `
    <section class="sheet" style="width:${sheetWidth}">
      ${header('裁床单菲票', qrContent)}
      <table class="meta">
        <tr><th>裁床单号</th><td>${escape(r.orderNo ?? '')}</td><th>状态</th><td>${escape(getStatusText(r.status))}</td></tr>
        <tr><th>款号/名称</th><td>${escape(r.styleCode ?? '')} / ${escape(r.styleName ?? '')}</td><th>颜色</th><td>${escape(r.color ?? '')}</td></tr>
        <tr><th>床次</th><td>${escape(r.bedNo ?? '')}</td><th>裁剪日期</th><td>${escape(r.cuttingDate ?? '')}</td></tr>
        <tr><th>总层数</th><td>${escape((r.totalLayers ?? '').toString())}层</td><th>总数量</th><td>${escape(((r.totalQuantity||0).toLocaleString()))}件</td></tr>
        <tr><th>裁切方式</th><td>${r.cuttingType === 'segment' ? '分层段裁' : '平均裁'}</td><th>交期</th><td>${escape(r.deliveryDate ?? '')}</td></tr>
        <tr><th>优先级</th><td>${escape(getPriorityText(r.priority))}</td><th>生成时间</th><td>${nowText()}</td></tr>
      </table>
      ${sizeRatio.length ? `
      <h3 class="block-title">尺码比例</h3>
      <table class="list">
        <thead><tr><th>尺码</th><th>比例(%)</th><th>数量</th></tr></thead>
        <tbody>
          ${sizeRatio.map(s => `<tr><td>${escape(s.size)}</td><td>${escape(s.ratio)}</td><td>${escape(s.qty)}</td></tr>`).join('')}
        </tbody>
      </table>` : ''}
      ${segmentRows.length ? `
      <h3 class="block-title">分层段方案</h3>
      <table class="list">
        <thead><tr><th>层段</th><th>层范围</th><th>尺码比例</th></tr></thead>
        <tbody>
          ${segmentRows.map((s: any) => `<tr><td>${escape(s.segment)}</td><td>${escape(s.layerRange)}</td><td>${escape(s.sizeRatio)}</td></tr>`).join('')}
        </tbody>
      </table>` : ''}
      ${footer()}
    </section>`
  }).join('<div class="page-break"></div>')

  const html = `<!doctype html>
  <html><head><meta charset="utf-8"><title>裁床单菲票</title>
  <style>
    ${pageRule}
    *{box-sizing:border-box}
    body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial; padding:0; color:#222; margin:0}
    .content{padding:10px}
    .grid{display:flex; flex-wrap:wrap; gap:10px}
    .sheet{page-break-after: always; padding:12px 8px}
    .page-break{page-break-after: always}
    .header{display:flex; align-items:center; justify-content:space-between; margin-bottom:10px; border-bottom:1px solid #ddd; padding-bottom:6px}
    .brand{display:flex; align-items:center; gap:8px}
    .brand img{height:28px}
    .brand-name{font-weight:600}
    .brand-sub{font-size:11px; color:#666}
    .title{font-size:16px; font-weight:700}
    .qr{text-align:right}
    .qr img{width:${qrSize}; height:${qrSize}; display:block; margin-left:auto}
    .qr-text{font-size:10px; color:#666; margin-top:4px}
    h3.block-title{margin:12px 0 6px; font-size:14px}
    table{border-collapse:collapse; width:100%; margin-bottom:8px}
    th,td{border:1px solid #333; padding:6px 8px; font-size:${fontBase}}
    th{background:#f5f7fa}
    .meta th{width:100px; text-align:left}
    .footer{margin-top:8px; text-align:right; font-size:11px; color:#666}
    @media print {.sheet{page-break-inside: avoid}}
  </style></head>
  <body>
    <div class="content"><div class="grid">${sections}</div></div>
    ${withPrintScript ? '<script>window.onload=()=>{window.print(); setTimeout(()=>window.close(), 300)}<\/script>' : ''}
  </body></html>`
  return html
}

// 打印
const printOrders = (rows: Partial<CutOrder>[], layoutOverride?: 'a4-1'|'a4-2'|'58') => {
  if (!rows || rows.length === 0) return
  const layout = layoutOverride || printLayout.value
  const html = makePrintHTML(rows, layout, true)
  const win = window.open('', '_blank')
  if (win) { win.document.open(); win.document.write(html); win.document.close() } else { ElMessage.warning('浏览器阻止了弹窗，请允许后重试') }
}

// 预览
const writePreview = async () => {
  await nextTick()
  const iframe = previewIframe.value
  if (!iframe) return
  const doc = iframe.contentDocument || (iframe as any).document
  if (!doc) return
  const html = makePrintHTML(previewRows.value, previewLayout.value, false)
  doc.open(); doc.write(html); doc.close()
}
const handlePreview = (row: Partial<CutOrder>) => {
  previewRows.value = [row]
  previewLayout.value = printLayout.value
  previewVisible.value = true
  writePreview()
}
const refreshPreview = () => { if (previewVisible.value) writePreview() }
const handlePreviewPrint = () => { if (previewRows.value.length) printOrders(previewRows.value, previewLayout.value) }
watch(previewLayout, () => { if (previewVisible.value) writePreview() })

// 生成包（服务端）
const handleGenerateBundles = async (row: Partial<CutOrder>) => {
  try {
    await ElMessageBox.confirm(`确认为裁床单 ${row?.orderNo ?? ''} 生成包？`, '生成包', { type: 'warning' })
  } catch { return }
  try {
    const resp = await post('/api/production/bundles/generate', {
      cutOrderId: row.id,
      bundleSize: undefined,
      regenerate: false
    })
    if (resp.code === 0) {
      ElMessage.success(`生成包成功，共 ${resp.data?.length || 0} 包`)
      fetchData()
    } else {
      ElMessage.error(resp.message || '生成包失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '生成包异常')
  }
}

// 还原裁床（服务端）
const handleRestoreCut = async (row: Partial<CutOrder>) => {
  // 两步确认：先尝试保留包还原；如用户需要清包再二次确认
  try {
    await ElMessageBox.confirm('将把此裁床单还原为草稿状态，可选清除已生成包。\n继续？', '还原裁床', {
      type: 'warning',
      confirmButtonText: '还原（保留包）',
      cancelButtonText: '取消',
      distinguishCancelAndClose: true,
      showCancelButton: true,
      showClose: true
    })
    const resp = await post(`/api/production/cut-orders/${row.id}/restore`, { removeBundles: false })
    if (resp.code === 0) {
      ElMessage.success('已还原为草稿（保留包）')
      fetchData()
      return
    }
    ElMessage.error(resp.message || '还原失败')
  } catch (err: any) {
    if (err === 'cancel') return
  }
  // 二次确认：清包并还原
  try {
    await ElMessageBox.confirm('是否清除已生成的包并还原为草稿？此操作不可撤销。', '还原裁床（清除包）', { type: 'warning' })
  } catch { return }
  try {
    const resp2 = await post(`/api/production/cut-orders/${row.id}/restore`, { removeBundles: true })
    if (resp2.code === 0) {
      ElMessage.success('已还原为草稿并清除包')
      fetchData()
    } else {
      ElMessage.error(resp2.message || '还原失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '还原异常')
  }
}

// 打印入口（行/批量）
const handlePrint = (row: Partial<CutOrder>) => { printOrders([row]) }
const handleBatchPrint = () => { if (!selectedRows.value.length) return ElMessage.warning('请先选择要打印的裁床单'); printOrders(selectedRows.value) }

// 取消
const handleCancel = (row: Partial<CutOrder>) => {
  ElMessageBox.confirm(`确认取消裁床单 ${row?.orderNo} 吗？`, '取消提示', { type: 'warning' })
    .then(() => { ElMessage.success('已取消（演示）'); fetchData() })
}

// 其它工具
const isOverdue = (delivery?: string) => {
  if (!delivery) return false
  return dayjs(delivery).isBefore(dayjs(), 'day')
}
const showQrCode = (row: any) => { ElMessage.info(`显示包号 ${row?.bundleNo} 的二维码`) }

// 表格选择/分页
const handleSelectionChange = (rows: Partial<CutOrder>[]) => { selectedRows.value = rows }
const handleSizeChange = () => { fetchData() }
const handleCurrentChange = () => { fetchData() }

// 导出 CSV（优先导出所选，其次导出当前表格数据）
const handleExport = () => {
  const rows = selectedRows.value.length ? selectedRows.value : tableData.value
  if (!rows.length) {
    ElMessage.info('没有可导出的数据')
    return
  }
  const headers = ['裁床单号','款号','款式名称','颜色','床次','层数','裁切方式','总数量','包数','裁剪日期','交期','优先级','状态']
  const toText = (v: any) => (v ?? '').toString().replace(/"/g, '""')
  const lines: string[] = [headers.join(',')]
  rows.forEach(r => {
    const cols = [
      r.orderNo,
      r.styleCode,
      r.styleName,
      r.color,
      r.bedNo,
      r.totalLayers,
      r.cuttingType === 'segment' ? '分层段裁' : '平均裁',
      r.totalQuantity ?? 0,
      r.bundleCount ?? 0,
      r.cuttingDate,
      r.deliveryDate,
      getPriorityText(r.priority as any),
      getStatusText(r.status as any)
    ].map(toText).map(x => `"${x}"`)
    lines.push(cols.join(','))
  })
  const csv = '\uFEFF' + lines.join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = '裁床单.csv'
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

onMounted(() => { fetchData() })
</script>

<style lang="scss" scoped>
.text-danger { color: #f56c6c; }
.section-title { margin: 20px 0 10px; font-size: 16px; font-weight: 600; color: #303133; }
.table-toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.preview-toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.preview-container { border: 1px solid #e5e7eb; border-radius: 6px; overflow: hidden; }
.preview-iframe { width: 100%; height: calc(100vh - 220px); background: #fff; border: 0; }
</style>
// 清理误插入的重复函数（占位标记，编辑器将删除该段）




