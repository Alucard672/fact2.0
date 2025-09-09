<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">扎包管理</h1>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="扎包编号">
          <el-input v-model="searchForm.bundleCode" placeholder="请输入扎包编号" clearable />
        </el-form-item>
        <el-form-item label="裁床单号">
          <el-input v-model="searchForm.cutOrderNo" placeholder="请输入裁床单号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待领取" value="pending" />
            <el-option label="生产中" value="in_work" />
            <el-option label="已完成" value="completed" />
            <el-option label="已返修" value="returned" />
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
        <el-button type="primary" @click="handleGenerate">
          <el-icon><Plus /></el-icon>
          生成扎包
        </el-button>
        <el-button type="success" @click="handleBatchPrint" :disabled="selectedRows.length === 0">
          <el-icon><Printer /></el-icon>
          批量打印
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-select v-model="printLayout" size="small" style="width: 160px; margin-right: 8px" placeholder="打印布局">
          <el-option label="A4 三列(默认)" value="a4-3" />
          <el-option label="A4 两列" value="a4-2" />
          <el-option label="58mm 工位贴纸" value="58" />
        </el-select>
        <span class="total-count">共 {{ pagination.total }} 条</span>
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
      <el-table-column prop="bundleCode" label="扎包编号" width="150" />
      <el-table-column prop="cutOrderNo" label="裁床单号" width="150" />
      <el-table-column prop="styleName" label="款式" width="120" />
      <el-table-column prop="size" label="尺码" width="80" />
      <el-table-column prop="quantity" label="数量" width="80" />
      <el-table-column prop="processName" label="当前工序" width="120" />
      <el-table-column prop="workerName" label="当前员工" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button link type="primary" @click="handlePrint(row)">打印</el-button>
          <el-button link type="warning" @click="handleTrack(row)">跟踪</el-button>
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

    <!-- 生成扎包对话框 -->
    <el-dialog
      v-model="generateDialogVisible"
      title="生成扎包"
      width="600px"
    >
      <el-form :model="generateForm" label-width="100px">
        <el-form-item label="裁床单" required>
          <el-select v-model="generateForm.cutOrderId" placeholder="请选择裁床单" style="width: 100%">
            <el-option
              v-for="order in cutOrderList"
              :key="order.id"
              :label="order.orderNo"
              :value="order.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分包策略" required>
          <el-radio-group v-model="generateForm.strategy">
            <el-radio label="auto">智能分包</el-radio>
            <el-radio label="manual">手动分包</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="每包数量" v-if="generateForm.strategy === 'manual'">
          <el-input-number v-model="generateForm.bundleSize" :min="1" :max="200" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="generateDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirmGenerate" :loading="generateLoading">
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
import { Plus, Printer, Refresh } from '@element-plus/icons-vue'

// 打印布局
const printLayout = ref<'a4-3'|'a4-2'|'58'>('a4-3')

// 类型定义
type BundleStatus = 'pending' | 'in_work' | 'completed' | 'returned'
interface BundleRecord {
  id: number
  bundleCode: string
  cutOrderId: number
  cutOrderNo: string
  styleName: string
  size: string
  quantity: number
  processName: string
  workerName: string
  status: BundleStatus
  createdAt: string
}
interface CutOrderOption {
  id: number
  orderNo: string
  styleName: string
  totalQuantity: number
  sizes?: Array<{ size: string; quantity: number }>
}

// 本地存储键
const BUNDLE_KEY = 'local_bundles_v1'
const CUTORDER_KEY = 'local_cut_orders_v1'

// 本地存储工具
const loadAllBundles = (): BundleRecord[] => {
  try {
    const raw = localStorage.getItem(BUNDLE_KEY)
    return raw ? (JSON.parse(raw) as BundleRecord[]) : []
  } catch {
    return []
  }
}
const saveAllBundles = (list: BundleRecord[]) => {
  localStorage.setItem(BUNDLE_KEY, JSON.stringify(list))
}
const ensureSeedCutOrders = (): CutOrderOption[] => {
  try {
    const raw = localStorage.getItem(CUTORDER_KEY)
    if (raw) return JSON.parse(raw) as CutOrderOption[]
  } catch {}
  const seed: CutOrderOption[] = [
    { id: 101, orderNo: 'CO20241201001', styleName: '春季T恤', totalQuantity: 120, sizes: [
      { size: 'S', quantity: 20 }, { size: 'M', quantity: 40 }, { size: 'L', quantity: 40 }, { size: 'XL', quantity: 20 }
    ] },
    { id: 102, orderNo: 'CO20241201002', styleName: '休闲卫衣', totalQuantity: 80, sizes: [
      { size: 'M', quantity: 30 }, { size: 'L', quantity: 30 }, { size: 'XL', quantity: 20 }
    ] }
  ]
  localStorage.setItem(CUTORDER_KEY, JSON.stringify(seed))
  return seed
}

// 状态
const loading = ref(false)
const generateLoading = ref(false)
const generateDialogVisible = ref(false)

// 搜索表单
const searchForm = reactive<{ bundleCode: string; cutOrderNo: string; status: '' | BundleStatus }>({
  bundleCode: '',
  cutOrderNo: '',
  status: ''
})

// 表格数据
const tableData = ref<BundleRecord[]>([])
const selectedRows = ref<BundleRecord[]>([])
const cutOrderList = ref<CutOrderOption[]>([])

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 生成扎包表单
const generateForm = reactive<{ cutOrderId: number | null; strategy: 'auto' | 'manual'; bundleSize: number }>({
  cutOrderId: null,
  strategy: 'auto',
  bundleSize: 10
})

// 状态类型
const getStatusType = (status: BundleStatus): 'warning' | 'primary' | 'success' | 'danger' | 'info' => {
  const types: Record<BundleStatus, 'warning' | 'primary' | 'success' | 'danger'> = {
    pending: 'warning',
    in_work: 'primary',
    completed: 'success',
    returned: 'danger'
  }
  return types[status] ?? 'info'
}

// 状态文本
const getStatusText = (status: BundleStatus) => {
  const texts: Record<BundleStatus, string> = {
    pending: '待领取',
    in_work: '生产中',
    completed: '已完成',
    returned: '已返修'
  }
  return texts[status] || status
}

// 过滤与分页
const applyFilter = (list: BundleRecord[]) => {
  return list.filter((x) => {
    const byCode = !searchForm.bundleCode || x.bundleCode.includes(searchForm.bundleCode.trim())
    const byOrder = !searchForm.cutOrderNo || x.cutOrderNo.includes(searchForm.cutOrderNo.trim())
    const byStatus = !searchForm.status || x.status === searchForm.status
    return byCode && byOrder && byStatus
  })
}
const applyPagination = (list: BundleRecord[]) => {
  pagination.total = list.length
  const start = (pagination.page - 1) * pagination.size
  return list.slice(start, start + pagination.size)
}

// 获取数据（本地）
const fetchData = async () => {
  loading.value = true
  try {
    const all = loadAllBundles()
    // 默认最新在前
    const sorted = [...all].sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1))
    const filtered = applyFilter(sorted)
    tableData.value = applyPagination(filtered)
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
  Object.assign(searchForm, { bundleCode: '', cutOrderNo: '', status: '' })
  handleSearch()
}

// 生成扎包
const handleGenerate = () => {
  cutOrderList.value = ensureSeedCutOrders()
  if (!generateForm.cutOrderId && cutOrderList.value.length > 0) {
    generateForm.cutOrderId = cutOrderList.value[0].id
  }
  generateDialogVisible.value = true
}

// 生成逻辑
const genBundleCode = (seq: number) => {
  const date = new Date()
  const y = date.getFullYear().toString().padStart(4, '0')
  const m = (date.getMonth() + 1).toString().padStart(2, '0')
  const d = date.getDate().toString().padStart(2, '0')
  return `B${y}${m}${d}${seq.toString().padStart(3, '0')}`
}
const nowText = () => {
  const d = new Date()
  const p = (n: number) => n.toString().padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

const handleConfirmGenerate = async () => {
  if (!generateForm.cutOrderId) {
    ElMessage.warning('请选择裁床单')
    return
  }
  generateLoading.value = true
  try {
    const all = loadAllBundles()
    const order = ensureSeedCutOrders().find((o) => o.id === generateForm.cutOrderId)!
    const sizes = order.sizes && order.sizes.length > 0 ? order.sizes : [{ size: 'M', quantity: order.totalQuantity }]

    let nextSeq = all.length + 1
    const newBundles: BundleRecord[] = []

    const pushBundle = (size: string, qty: number) => {
      newBundles.push({
        id: Date.now() + Math.floor(Math.random() * 10000),
        bundleCode: genBundleCode(nextSeq++),
        cutOrderId: order.id,
        cutOrderNo: order.orderNo,
        styleName: order.styleName,
        size,
        quantity: qty,
        processName: '前道工序',
        workerName: '-',
        status: 'pending',
        createdAt: nowText()
      })
    }

    const targetPer = generateForm.strategy === 'manual' ? Math.max(1, Math.floor(generateForm.bundleSize)) : 10

    for (const s of sizes) {
      let remain = s.quantity
      while (remain > 0) {
        const take = Math.min(targetPer, remain)
        pushBundle(s.size, take)
        remain -= take
      }
    }

    const merged = [...newBundles, ...all]
    saveAllBundles(merged)
    ElMessage.success(`成功生成 ${newBundles.length} 个扎包`)
    generateDialogVisible.value = false
    fetchData()
  } catch (error) {
    ElMessage.error('生成失败')
  } finally {
    generateLoading.value = false
  }
}

// 打印实现
const printRows = (rows: BundleRecord[]) => {
  if (!rows || rows.length === 0) return
  const escape = (s: any) => (s ?? '').toString().replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  const header = (title: string) => `
    <header class="header">
      <div class="brand">
        <img src="/logo.png" alt="Logo" onerror="this.style.display='none'" />
        <div class="brand-text">
          <div class="brand-name">裁床生产管理</div>
          <div class="brand-sub">Cutting Management System</div>
        </div>
      </div>
      <div class="title">${title}</div>
      <div class="print-time">打印时间：${nowText()}</div>
    </header>`

  const cards = rows.map(r => {
    const qrText = `BUNDLE-${r.bundleCode}`
    return `<div class="label">
      <div class="label-head">
        <div class="code">${escape(r.bundleCode)}</div>
        <div class="qr">
          <img src="https://chart.googleapis.com/chart?cht=qr&chs=120x120&chl=${encodeURIComponent(qrText)}" alt="QR" onerror="this.style.display='none'" />
          <div class="qr-text">${escape(qrText)}</div>
        </div>
      </div>
      <div class="row"><span class="k">裁床单：</span><span class="v">${escape(r.cutOrderNo)}</span></div>
      <div class="row"><span class="k">款式：</span><span class="v">${escape(r.styleName)}</span></div>
      <div class="row two">
        <div><span class="k">尺码：</span><span class="v">${escape(r.size)}</span></div>
        <div><span class="k">数量：</span><span class="v">${escape(r.quantity)}</span></div>
      </div>
      <div class="row two">
        <div><span class="k">工序：</span><span class="v">${escape(r.processName)}</span></div>
        <div><span class="k">状态：</span><span class="v">${escape(getStatusText(r.status))}</span></div>
      </div>
      <div class="row"><span class="k">创建：</span><span class="v">${escape(r.createdAt)}</span></div>
    </div>`
  }).join('')

  // 根据布局设置尺寸
  const layout = printLayout.value
  let pageRule = '@page { size: A4; margin: 10mm }'
  let labelWidth = '32.3%'
  let qrSize = '96px'
  let minHeight = '150px'
  let gap = '8px'
  let padding = '8px'
  let fontBase = '12px'
  if (layout === 'a4-2') {
    labelWidth = '49%'
    qrSize = '120px'
    minHeight = '180px'
  } else if (layout === '58') {
    pageRule = '@page { size: 58mm auto; margin: 3mm }'
    labelWidth = '100%'
    qrSize = '96px'
    minHeight = '40mm'
    gap = '4px'
    padding = '6px'
    fontBase = '11px'
  }

  const html = `<!doctype html>
  <html><head><meta charset="utf-8"><title>扎包打印</title>
  <style>
    ${pageRule}
    *{box-sizing: border-box}
    body{font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial; color:#222; padding:0; margin:0}
    .container{padding:10px}
    .header{display:flex; align-items:center; justify-content:space-between; margin-bottom:10px; border-bottom:1px solid #ddd; padding-bottom:6px}
    .brand{display:flex; align-items:center; gap:8px}
    .brand img{height:28px}
    .brand-name{font-weight:600}
    .brand-sub{font-size:11px; color:#666}
    .title{font-size:16px; font-weight:700}
    .print-time{font-size:11px; color:#666}
    .grid{display:flex; flex-wrap:wrap; gap:${gap}}
    .label{border:1px solid #333; padding:${padding}; width:${labelWidth}; min-height:${minHeight}}
    .label-head{display:flex; align-items:center; justify-content:space-between; gap:6px; margin-bottom:6px}
    .code{font-size:16px; font-weight:700}
    .qr{text-align:right}
    .qr img{width:${qrSize}; height:${qrSize}; display:block; margin-left:auto}
    .qr-text{font-size:10px; color:#666; margin-top:2px}
    .row{display:flex; gap:6px; margin:3px 0; font-size:${fontBase}}
    .row.two{display:flex; justify-content:space-between}
    .k{color:#555}
    .v{font-weight:600}
    @media print {.label{page-break-inside: avoid}}
  </style></head>
  <body>
    <div class="container">
      ${header('扎包标签')}
      <div class="grid">${cards}</div>
    </div>
    <script>window.onload=()=>{window.print(); setTimeout(()=>window.close(), 300)}<\/script>
  </body></html>`

  const win = window.open('', '_blank')
  if (win) {
    win.document.open()
    win.document.write(html)
    win.document.close()
  } else {
    ElMessage.warning('浏览器阻止了弹窗，请允许后重试')
  }
}

// 批量打印
const handleBatchPrint = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择需要打印的记录')
    return
  }
  printRows(selectedRows.value)
}

// 查看
const handleView = (row: BundleRecord) => {
  const content = `
    <div style="line-height:1.8">
      <div><b>扎包编号：</b>${row.bundleCode}</div>
      <div><b>裁床单号：</b>${row.cutOrderNo}</div>
      <div><b>款式：</b>${row.styleName}</div>
      <div><b>尺码：</b>${row.size}</div>
      <div><b>数量：</b>${row.quantity}</div>
      <div><b>当前工序：</b>${row.processName}</div>
      <div><b>当前员工：</b>${row.workerName}</div>
      <div><b>状态：</b>${getStatusText(row.status)}</div>
      <div><b>创建时间：</b>${row.createdAt}</div>
    </div>`
  ElMessageBox.alert(content, '扎包详情（本地模拟）', { dangerouslyUseHTMLString: true })
}

// 打印
const handlePrint = (row: BundleRecord) => {
  printRows([row])
}

// 跟踪
const handleTrack = (row: BundleRecord) => {
  const content = `
    <div style="line-height:1.8">
      <div><b>扎包编号：</b>${row.bundleCode}</div>
      <div style="margin:8px 0"><b>流转记录（模拟）：</b></div>
      <ol style="margin:0 0 0 20px; padding:0">
        <li>创建于 ${row.createdAt}</li>
        <li>分配至车间A（待领取）</li>
        <li>进入生产（如状态为生产中）</li>
        <li>完成质检（如状态为已完成）</li>
      </ol>
    </div>`
  ElMessageBox.alert(content, '生产跟踪（本地模拟）', { dangerouslyUseHTMLString: true })
}

// 表格选择变化
const handleSelectionChange = (selection: BundleRecord[]) => {
  selectedRows.value = selection
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

onMounted(() => {
  fetchData()
})
</script>




