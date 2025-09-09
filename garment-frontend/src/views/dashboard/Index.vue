<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon blue">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.todayProduction.toLocaleString() }}</div>
            <div class="stat-label">今日生产</div>
            <div class="stat-trend">
              <span :class="stats.todayTrend > 0 ? 'up' : 'down'">
                {{ Math.abs(stats.todayTrend) }}%
                <el-icon v-if="stats.todayTrend > 0"><Top /></el-icon>
                <el-icon v-else><Bottom /></el-icon>
              </span>
              较昨日
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon green">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.activeWorkers }}</div>
            <div class="stat-label">在岗工人</div>
            <div class="stat-trend">
              总计 {{ stats.totalWorkers }} 人
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon orange">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.defectRate }}%</div>
            <div class="stat-label">次品率</div>
            <div class="stat-trend">
              <span :class="stats.defectTrend > 0 ? 'down' : 'up'">
                {{ Math.abs(stats.defectTrend) }}%
                <el-icon v-if="stats.defectTrend > 0"><Bottom /></el-icon>
                <el-icon v-else><Top /></el-icon>
              </span>
              较昨日
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon purple">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.efficiency }}%</div>
            <div class="stat-label">生产效率</div>
            <div class="stat-trend">
              目标 85%
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :md="16">
        <div class="card">
          <div class="card-header">
            <div class="card-title">生产趋势</div>
            <el-radio-group v-model="chartPeriod" size="small">
              <el-radio-button label="week">本周</el-radio-button>
              <el-radio-button label="month">本月</el-radio-button>
              <el-radio-button label="year">本年</el-radio-button>
            </el-radio-group>
          </div>
          <div class="card-body">
            <div id="production-chart" ref="chartEl" style="height: 350px;"></div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :md="8">
        <div class="card">
          <div class="card-header">
            <div class="card-title">车间排名</div>
            <el-link type="primary" @click="router.push('/statistics/workshop-ranking')">
              查看更多 <el-icon><ArrowRight /></el-icon>
            </el-link>
          </div>
          <div class="card-body">
            <div class="workshop-ranking">
              <div v-for="(item, index) in workshopRanking" :key="item.id" class="ranking-item">
                <div class="ranking-index" :class="`rank-${index + 1}`">
                  {{ index + 1 }}
                </div>
                <div class="ranking-info">
                  <div class="workshop-name">{{ item.name }}</div>
                  <el-progress :percentage="item.efficiency" :stroke-width="6" />
                </div>
                <div class="ranking-value">{{ item.production }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 生产订单和待办事项 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :xs="24" :md="12">
        <div class="card">
          <div class="card-header">
            <div class="card-title">最新订单</div>
            <el-link type="primary" @click="router.push('/production/cut-orders')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-link>
          </div>
          <div class="card-body">
            <el-table :data="recentOrders" stripe>
              <el-table-column prop="orderNo" label="订单号" width="120" />
              <el-table-column prop="styleName" label="款式" />
              <el-table-column prop="quantity" label="数量" width="80" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)" size="small">
                    {{ getStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :md="12">
        <div class="card">
          <div class="card-header">
            <div class="card-title">待办事项</div>
            <el-badge :value="todos.length" :max="99" />
          </div>
          <div class="card-body">
            <div class="todo-list">
              <div v-for="todo in todos" :key="todo.id" class="todo-item">
                <div class="todo-icon" :class="todo.type">
                  <el-icon><component :is="getTodoIcon(todo.type)" /></el-icon>
                </div>
                <div class="todo-content">
                  <div class="todo-title">{{ todo.title }}</div>
                  <div class="todo-time">{{ todo.time }}</div>
                </div>
                <el-button link type="primary" size="small">处理</el-button>
              </div>
              <el-empty v-if="todos.length === 0" description="暂无待办事项" />
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { 
  DataAnalysis, User, Warning, TrendCharts, 
  Top, Bottom, ArrowRight, Document, Printer, Tools, Money, Bell
} from '@element-plus/icons-vue'

const router = useRouter()

// 统计数据
const stats = ref({
  todayProduction: 12580,
  todayTrend: 15.3,
  activeWorkers: 126,
  totalWorkers: 150,
  defectRate: 2.3,
  defectTrend: -0.5,
  efficiency: 88.5
})

// 图表周期
const chartPeriod = ref('week')

// 车间排名
const workshopRanking = ref([
  { id: 1, name: '一车间', production: 4580, efficiency: 92 },
  { id: 2, name: '二车间', production: 4120, efficiency: 88 },
  { id: 3, name: '三车间', production: 3880, efficiency: 85 },
  { id: 4, name: '四车间', production: 3200, efficiency: 80 },
  { id: 5, name: '五车间', production: 2800, efficiency: 75 }
])

// 最新订单
const recentOrders = ref([
  { id: 1, orderNo: 'CUT202401001', styleName: '经典T恤-白色', quantity: 500, status: 'cutting' },
  { id: 2, orderNo: 'CUT202401002', styleName: '休闲裤-黑色', quantity: 300, status: 'confirmed' },
  { id: 3, orderNo: 'CUT202401003', styleName: '连衣裙-花色', quantity: 200, status: 'draft' },
  { id: 4, orderNo: 'CUT202401004', styleName: '衬衫-蓝色', quantity: 450, status: 'completed' },
  { id: 5, orderNo: 'CUT202401005', styleName: '外套-灰色', quantity: 150, status: 'cutting' }
])

// 待办事项
const todos = ref([
  { id: 1, type: 'order', title: '3个订单待确认', time: '10分钟前' },
  { id: 2, type: 'print', title: '5个打印任务待处理', time: '30分钟前' },
  { id: 3, type: 'repair', title: '2个返修申请待审批', time: '1小时前' },
  { id: 4, type: 'payroll', title: '本月工资结算待确认', time: '2小时前' }
])

// 获取状态类型
type TagType = 'success' | 'info' | 'warning' | 'danger'
const getStatusType = (status: string): TagType => {
  const types: Record<string, TagType> = {
    draft: 'info',
    confirmed: 'warning',
    cutting: 'warning',
    completed: 'success'
  }
  return types[status] ?? 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    draft: '草稿',
    confirmed: '已确认',
    cutting: '裁剪中',
    completed: '已完成'
  }
  return texts[status] || status
}

// 获取待办图标
const getTodoIcon = (type: string) => {
  const icons: Record<string, any> = {
    order: Document,
    print: Printer,
    repair: Tools,
    payroll: Money
  }
  return icons[type] || Bell
}

// 简易柱状图占位（内联 SVG）
const chartEl = ref<HTMLDivElement | null>(null)
const getChartData = (): number[] => {
  if (chartPeriod.value === 'week') return [520, 680, 450, 720, 610, 780, 560]
  if (chartPeriod.value === 'month') return [820, 760, 690, 880, 940, 1020, 980, 1100, 1060, 990, 1030, 1080]
  // year
  return [5200, 6100, 5800, 6400, 7000, 7600, 7300, 8000, 8200, 7900, 8500, 9000]
}

const renderChart = () => {
  const el = chartEl.value
  if (!el) return
  const data = getChartData()
  const width = el.clientWidth || 600
  const height = 350
  const padding = { left: 28, bottom: 22, right: 12, top: 10 }
  const innerW = width - padding.left - padding.right
  const innerH = height - padding.top - padding.bottom
  const maxVal = Math.max(...data, 1)
  const barGap = 8
  const barW = Math.max(6, Math.floor((innerW - barGap * (data.length - 1)) / data.length))

  const bars = data
    .map((v, i) => {
      const h = Math.round((v / maxVal) * innerH)
      const x = padding.left + i * (barW + barGap)
      const y = padding.top + (innerH - h)
      return `<rect x="${x}" y="${y}" width="${barW}" height="${h}" rx="3" fill="#409EFF" />`
    })
    .join('')

  const axisX = `<line x1="${padding.left}" y1="${height - padding.bottom}" x2="${width - padding.right}" y2="${height - padding.bottom}" stroke="#e4e7ed" stroke-width="1" />`
  const axisY = `<line x1="${padding.left}" y1="${padding.top}" x2="${padding.left}" y2="${height - padding.bottom}" stroke="#e4e7ed" stroke-width="1" />`

  const svg = `<svg width="${width}" height="${height}" viewBox="0 0 ${width} ${height}" role="img" aria-label="生产趋势占位图">${axisX}${axisY}${bars}</svg>`
  el.innerHTML = svg
}

// 初始化图表
const initCharts = () => {
  renderChart()
}

// 周期切换时重绘
watch(chartPeriod, () => {
  renderChart()
})

onMounted(() => {
  initCharts()
})

onUnmounted(() => {
  // 销毁图表实例/清理容器
  if (chartEl.value) chartEl.value.innerHTML = ''
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  // 统计卡片
  .stats-cards {
    margin-bottom: 20px;
    
    .stat-card {
      background: #fff;
      border-radius: 4px;
      padding: 20px;
      display: flex;
      align-items: center;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      
      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16px;
        
        .el-icon {
          font-size: 28px;
          color: #fff;
        }
        
        &.blue {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
        
        &.green {
          background: linear-gradient(135deg, #56ab2f 0%, #a8e063 100%);
        }
        
        &.orange {
          background: linear-gradient(135deg, #ff9a56 0%, #ff6a88 100%);
        }
        
        &.purple {
          background: linear-gradient(135deg, #8e2de2 0%, #4a00e0 100%);
        }
      }
      
      .stat-content {
        flex: 1;
        
        .stat-value {
          font-size: 28px;
          font-weight: 600;
          color: #303133;
          line-height: 1;
          margin-bottom: 8px;
        }
        
        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-bottom: 8px;
        }
        
        .stat-trend {
          font-size: 12px;
          color: #606266;
          
          span {
            font-weight: 500;
            margin-right: 4px;
            
            &.up {
              color: #67c23a;
            }
            
            &.down {
              color: #f56c6c;
            }
            
            .el-icon {
              vertical-align: -2px;
            }
          }
        }
      }
    }
  }
  
  // 图表行
  .charts-row {
    margin-bottom: 20px;
  }
  
  // 车间排名
  .workshop-ranking {
    .ranking-item {
      display: flex;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #ebeef5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .ranking-index {
        width: 24px;
        height: 24px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        font-weight: 600;
        margin-right: 12px;
        
        &.rank-1 {
          background: #ff9500;
          color: #fff;
        }
        
        &.rank-2 {
          background: #c0c0c0;
          color: #fff;
        }
        
        &.rank-3 {
          background: #cd7f32;
          color: #fff;
        }
      }
      
      .ranking-info {
        flex: 1;
        
        .workshop-name {
          font-size: 14px;
          color: #303133;
          margin-bottom: 4px;
        }
      }
      
      .ranking-value {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin-left: 12px;
      }
    }
  }
  
  // 待办事项
  .todo-list {
    .todo-item {
      display: flex;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #ebeef5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .todo-icon {
        width: 36px;
        height: 36px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12px;
        
        .el-icon {
          font-size: 20px;
          color: #fff;
        }
        
        &.order {
          background: #1890ff;
        }
        
        &.print {
          background: #52c41a;
        }
        
        &.repair {
          background: #faad14;
        }
        
        &.payroll {
          background: #722ed1;
        }
      }
      
      .todo-content {
        flex: 1;
        
        .todo-title {
          font-size: 14px;
          color: #303133;
          margin-bottom: 4px;
        }
        
        .todo-time {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}
</style>




