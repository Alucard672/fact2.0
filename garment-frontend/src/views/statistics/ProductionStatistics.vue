<template>
  <div class="page-container">
    <!-- 查询条件 -->
    <div class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="统计维度">
          <el-radio-group v-model="filterForm.dimension" @change="handleDimensionChange">
            <el-radio-button label="day">按日</el-radio-button>
            <el-radio-button label="week">按周</el-radio-button>
            <el-radio-button label="month">按月</el-radio-button>
            <el-radio-button label="quarter">按季度</el-radio-button>
            <el-radio-button label="year">按年</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            :type="datePickerType"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            :shortcuts="dateShortcuts"
          />
        </el-form-item>
        <el-form-item label="车间">
          <el-select
            v-model="filterForm.workshopIds"
            multiple
            placeholder="全部车间"
            clearable
            collapse-tags
            collapse-tags-tooltip
          >
            <el-option
              v-for="item in workshopList"
              :key="item.id"
              :label="item.workshopName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="款式">
          <el-select
            v-model="filterForm.styleIds"
            multiple
            placeholder="全部款式"
            clearable
            collapse-tags
            collapse-tags-tooltip
            filterable
          >
            <el-option
              v-for="item in styleList"
              :key="item.id"
              :label="`${item.styleCode} - ${item.styleName}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleExport">导出报表</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 核心指标卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="metric-card">
          <div class="metric-header">
            <span class="metric-title">总产量</span>
            <el-icon class="metric-icon blue"><Goods /></el-icon>
          </div>
          <div class="metric-value">{{ metrics.totalProduction.toLocaleString() }}</div>
          <div class="metric-footer">
            <span>较上期</span>
            <span :class="metrics.productionTrend > 0 ? 'trend-up' : 'trend-down'">
              {{ metrics.productionTrend > 0 ? '+' : '' }}{{ metrics.productionTrend }}%
              <el-icon v-if="metrics.productionTrend > 0"><Top /></el-icon>
              <el-icon v-else><Bottom /></el-icon>
            </span>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <div class="metric-card">
          <div class="metric-header">
            <span class="metric-title">合格率</span>
            <el-icon class="metric-icon green"><CircleCheck /></el-icon>
          </div>
          <div class="metric-value">{{ metrics.qualityRate }}%</div>
          <div class="metric-footer">
            <span>较上期</span>
            <span :class="metrics.qualityTrend > 0 ? 'trend-up' : 'trend-down'">
              {{ metrics.qualityTrend > 0 ? '+' : '' }}{{ metrics.qualityTrend }}%
              <el-icon v-if="metrics.qualityTrend > 0"><Top /></el-icon>
              <el-icon v-else><Bottom /></el-icon>
            </span>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <div class="metric-card">
          <div class="metric-header">
            <span class="metric-title">平均效率</span>
            <el-icon class="metric-icon orange"><TrendCharts /></el-icon>
          </div>
          <div class="metric-value">{{ metrics.avgEfficiency }}%</div>
          <div class="metric-footer">
            <span>目标值</span>
            <span class="target">85%</span>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <div class="metric-card">
          <div class="metric-header">
            <span class="metric-title">返修率</span>
            <el-icon class="metric-icon red"><Warning /></el-icon>
          </div>
          <div class="metric-value">{{ metrics.repairRate }}%</div>
          <div class="metric-footer">
            <span>较上期</span>
            <span :class="metrics.repairTrend < 0 ? 'trend-up' : 'trend-down'">
              {{ metrics.repairTrend > 0 ? '+' : '' }}{{ metrics.repairTrend }}%
              <el-icon v-if="metrics.repairTrend < 0"><Bottom /></el-icon>
              <el-icon v-else><Top /></el-icon>
            </span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <!-- 产量趋势图 -->
      <el-col :span="24">
        <div class="chart-card">
          <div class="chart-header">
            <h3 class="chart-title">产量趋势图</h3>
            <el-radio-group v-model="chartType" size="small">
              <el-radio-button label="line">折线图</el-radio-button>
              <el-radio-button label="bar">柱状图</el-radio-button>
              <el-radio-button label="area">面积图</el-radio-button>
            </el-radio-group>
          </div>
          <div class="chart-body" id="production-chart" style="height: 400px;">
            <!-- ECharts图表区域 -->
            <div class="chart-placeholder">产量趋势图表</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 车间产量分布 -->
      <el-col :xs="24" :md="12">
        <div class="chart-card">
          <div class="chart-header">
            <h3 class="chart-title">车间产量分布</h3>
          </div>
          <div class="chart-body" id="workshop-pie" style="height: 350px;">
            <!-- 饼图 -->
            <div class="chart-placeholder">车间产量饼图</div>
          </div>
        </div>
      </el-col>
      
      <!-- 款式产量TOP10 -->
      <el-col :xs="24" :md="12">
        <div class="chart-card">
          <div class="chart-header">
            <h3 class="chart-title">款式产量 TOP10</h3>
          </div>
          <div class="chart-body">
            <el-table :data="styleTopList" height="350">
              <el-table-column type="index" width="50" label="排名" />
              <el-table-column prop="styleCode" label="款号" width="100" />
              <el-table-column prop="styleName" label="款式名称" min-width="150" />
              <el-table-column prop="quantity" label="产量" width="100" align="right">
                <template #default="{ row }">
                  {{ row.quantity.toLocaleString() }}
                </template>
              </el-table-column>
              <el-table-column label="占比" width="120">
                <template #default="{ row }">
                  <el-progress :percentage="row.percentage" :stroke-width="6" />
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 工序完成情况 -->
      <el-col :span="24">
        <div class="chart-card">
          <div class="chart-header">
            <h3 class="chart-title">工序完成情况</h3>
          </div>
          <div class="chart-body" id="process-bar" style="height: 350px;">
            <!-- 横向柱状图 -->
            <div class="chart-placeholder">工序完成情况柱状图</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 明细数据表格 -->
    <div class="detail-card">
      <div class="detail-header">
        <h3 class="detail-title">生产明细数据</h3>
        <el-button size="small" @click="toggleDetail">
          {{ showDetail ? '收起' : '展开' }}
          <el-icon v-if="showDetail"><ArrowUp /></el-icon>
          <el-icon v-else><ArrowDown /></el-icon>
        </el-button>
      </div>
      
      <el-collapse-transition>
        <div v-show="showDetail">
          <el-table :data="detailData" border show-summary :summary-method="getSummaries">
            <el-table-column prop="date" label="日期" width="110" fixed="left" />
            <el-table-column prop="workshopName" label="车间" width="100" />
            <el-table-column prop="styleName" label="款式" min-width="150" />
            <el-table-column prop="processName" label="工序" width="100" />
            <el-table-column prop="production" label="产量" width="100" align="right">
              <template #default="{ row }">
                {{ row.production.toLocaleString() }}
              </template>
            </el-table-column>
            <el-table-column prop="qualifiedQty" label="合格数" width="100" align="right">
              <template #default="{ row }">
                {{ row.qualifiedQty.toLocaleString() }}
              </template>
            </el-table-column>
            <el-table-column prop="defectQty" label="次品数" width="100" align="right">
              <template #default="{ row }">
                {{ row.defectQty.toLocaleString() }}
              </template>
            </el-table-column>
            <el-table-column prop="qualityRate" label="合格率" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getQualityType(row.qualityRate)">
                  {{ row.qualityRate }}%
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="efficiency" label="效率" width="100" align="center">
              <template #default="{ row }">
                <el-progress
                  :percentage="row.efficiency"
                  :color="getEfficiencyColor(row.efficiency)"
                  :stroke-width="6"
                />
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-collapse-transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Goods, CircleCheck, TrendCharts, Warning,
  Top, Bottom, ArrowUp, ArrowDown
} from '@element-plus/icons-vue'

// 查询表单
const filterForm = reactive({
  dimension: 'day',
  dateRange: [],
  workshopIds: [],
  styleIds: []
})

// 指标数据
const metrics = ref({
  totalProduction: 156800,
  productionTrend: 12.5,
  qualityRate: 96.8,
  qualityTrend: 2.3,
  avgEfficiency: 82.5,
  repairRate: 3.2,
  repairTrend: -1.5
})

// 图表类型
const chartType = ref('line')
const showDetail = ref(false)

// 款式TOP列表
const styleTopList = ref([
  { styleCode: 'ST001', styleName: '经典T恤', quantity: 15800, percentage: 25.3 },
  { styleCode: 'ST002', styleName: '休闲裤', quantity: 12600, percentage: 20.2 },
  { styleCode: 'ST003', styleName: '连衣裙', quantity: 10200, percentage: 16.3 },
  { styleCode: 'ST004', styleName: '衬衫', quantity: 8900, percentage: 14.2 },
  { styleCode: 'ST005', styleName: '外套', quantity: 6500, percentage: 10.4 }
])

// 明细数据
const detailData = ref([
  {
    date: '2024-01-15',
    workshopName: '一车间',
    styleName: '经典T恤',
    processName: '缝合',
    production: 580,
    qualifiedQty: 560,
    defectQty: 20,
    qualityRate: 96.6,
    efficiency: 85
  },
  {
    date: '2024-01-15',
    workshopName: '二车间',
    styleName: '休闲裤',
    processName: '裁剪',
    production: 420,
    qualifiedQty: 415,
    defectQty: 5,
    qualityRate: 98.8,
    efficiency: 92
  }
])

// 车间列表
const workshopList = ref([
  { id: 1, workshopName: '一车间' },
  { id: 2, workshopName: '二车间' },
  { id: 3, workshopName: '三车间' }
])

// 款式列表
const styleList = ref([
  { id: 1, styleCode: 'ST001', styleName: '经典T恤' },
  { id: 2, styleCode: 'ST002', styleName: '休闲裤' },
  { id: 3, styleCode: 'ST003', styleName: '连衣裙' }
])

// 日期快捷选项
const dateShortcuts = computed(() => {
  const shortcuts = []
  
  if (filterForm.dimension === 'day') {
    shortcuts.push(
      { text: '今天', value: () => [new Date(), new Date()] },
      { text: '昨天', value: () => { const d = new Date(); d.setDate(d.getDate() - 1); return [d, d] } },
      { text: '最近7天', value: () => { const end = new Date(); const start = new Date(); start.setDate(start.getDate() - 6); return [start, end] } },
      { text: '最近30天', value: () => { const end = new Date(); const start = new Date(); start.setDate(start.getDate() - 29); return [start, end] } }
    )
  } else if (filterForm.dimension === 'month') {
    shortcuts.push(
      { text: '本月', value: () => { const now = new Date(); return [new Date(now.getFullYear(), now.getMonth(), 1), now] } },
      { text: '上月', value: () => { const now = new Date(); const start = new Date(now.getFullYear(), now.getMonth() - 1, 1); const end = new Date(now.getFullYear(), now.getMonth(), 0); return [start, end] } },
      { text: '最近3个月', value: () => { const end = new Date(); const start = new Date(); start.setMonth(start.getMonth() - 2); return [start, end] } }
    )
  }
  
  return shortcuts
})

// 日期选择器类型
const datePickerType = computed(() => {
  const types: Record<string, string> = {
    day: 'daterange',
    week: 'weekrange',
    month: 'monthrange',
    quarter: 'daterange',
    year: 'yearrange'
  }
  return types[filterForm.dimension] || 'daterange'
})

// 维度变化
const handleDimensionChange = () => {
  filterForm.dateRange = []
}

// 查询
const handleQuery = () => {
  ElMessage.success('查询成功')
  // 调用API获取数据
  loadChartData()
}

// 重置
const handleReset = () => {
  Object.assign(filterForm, {
    dimension: 'day',
    dateRange: [],
    workshopIds: [],
    styleIds: []
  })
  handleQuery()
}

// 导出
const handleExport = () => {
  ElMessage.info('导出报表功能开发中')
}

// 切换明细
const toggleDetail = () => {
  showDetail.value = !showDetail.value
}

// 加载图表数据
const loadChartData = () => {
  // 这里应该集成ECharts
  console.log('加载图表数据')
}

// 获取质量类型
const getQualityType = (rate: number) => {
  if (rate >= 98) return 'success'
  if (rate >= 95) return 'warning'
  return 'danger'
}

// 获取效率颜色
const getEfficiencyColor = (efficiency: number) => {
  if (efficiency >= 90) return '#67c23a'
  if (efficiency >= 80) return '#e6a23c'
  if (efficiency >= 70) return '#f56c6c'
  return '#909399'
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
    
    if (['production', 'qualifiedQty', 'defectQty'].includes(column.property)) {
      const values = data.map((item: any) => Number(item[column.property]))
      const total = values.reduce((prev: number, curr: number) => prev + curr, 0)
      sums[index] = total.toLocaleString()
    } else if (column.property === 'qualityRate') {
      const totalQualified = data.reduce((sum: number, item: any) => sum + item.qualifiedQty, 0)
      const totalProduction = data.reduce((sum: number, item: any) => sum + item.production, 0)
      const rate = totalProduction > 0 ? (totalQualified / totalProduction * 100).toFixed(1) : '0'
      sums[index] = `${rate}%`
    } else {
      sums[index] = ''
    }
  })
  
  return sums
}

// 初始化
onMounted(() => {
  // 设置默认日期范围
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 6)
  filterForm.dateRange = [start, end]
  
  // 加载数据
  handleQuery()
})
</script>

<style lang="scss" scoped>
.filter-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

// 指标卡片
.stats-cards {
  margin-bottom: 20px;
  
  .metric-card {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    
    .metric-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .metric-title {
        font-size: 14px;
        color: #909399;
      }
      
      .metric-icon {
        font-size: 24px;
        
        &.blue { color: #1890ff; }
        &.green { color: #52c41a; }
        &.orange { color: #faad14; }
        &.red { color: #ff4d4f; }
      }
    }
    
    .metric-value {
      font-size: 32px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 12px;
    }
    
    .metric-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 12px;
      color: #909399;
      
      .trend-up { color: #52c41a; }
      .trend-down { color: #ff4d4f; }
      .target { color: #1890ff; }
    }
  }
}

// 图表卡片
.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  
  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .chart-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
  }
  
  .chart-body {
    .chart-placeholder {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100%;
      color: #909399;
      background: #f5f7fa;
      border-radius: 4px;
    }
  }
}

// 明细卡片
.detail-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-top: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  
  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .detail-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
  }
}
</style>




