<template>
  <div class="page-container">
    <!-- 筛选条件 -->
    <div class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="统计周期">
          <el-radio-group v-model="filterForm.period" @change="handlePeriodChange">
            <el-radio-button label="today">今日</el-radio-button>
            <el-radio-button label="yesterday">昨日</el-radio-button>
            <el-radio-button label="week">本周</el-radio-button>
            <el-radio-button label="month">本月</el-radio-button>
            <el-radio-button label="custom">自定义</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="日期范围" v-if="filterForm.period === 'custom'">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="排序方式">
          <el-select v-model="filterForm.sortBy" @change="handleSortChange">
            <el-option label="综合排名" value="overall" />
            <el-option label="产量排名" value="production" />
            <el-option label="效率排名" value="efficiency" />
            <el-option label="质量排名" value="quality" />
            <el-option label="准时率排名" value="ontime" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 排名展示 -->
    <div class="ranking-display">
      <!-- TOP3展示 -->
      <div class="top3-container">
        <div class="top3-item silver" v-if="rankingData[1]">
          <div class="medal">🥈</div>
          <div class="rank-number">2</div>
          <div class="workshop-name">{{ rankingData[1].workshopName }}</div>
          <div class="score">{{ rankingData[1].overallScore }}分</div>
          <div class="metrics">
            <div class="metric-item">
              <span class="label">产量</span>
              <span class="value">{{ rankingData[1].production.toLocaleString() }}</span>
            </div>
            <div class="metric-item">
              <span class="label">效率</span>
              <span class="value">{{ rankingData[1].efficiency }}%</span>
            </div>
            <div class="metric-item">
              <span class="label">质量</span>
              <span class="value">{{ rankingData[1].quality }}%</span>
            </div>
          </div>
        </div>
        
        <div class="top3-item gold" v-if="rankingData[0]">
          <div class="medal">🥇</div>
          <div class="rank-number">1</div>
          <div class="workshop-name">{{ rankingData[0].workshopName }}</div>
          <div class="score">{{ rankingData[0].overallScore }}分</div>
          <div class="metrics">
            <div class="metric-item">
              <span class="label">产量</span>
              <span class="value">{{ rankingData[0].production.toLocaleString() }}</span>
            </div>
            <div class="metric-item">
              <span class="label">效率</span>
              <span class="value">{{ rankingData[0].efficiency }}%</span>
            </div>
            <div class="metric-item">
              <span class="label">质量</span>
              <span class="value">{{ rankingData[0].quality }}%</span>
            </div>
          </div>
        </div>
        
        <div class="top3-item bronze" v-if="rankingData[2]">
          <div class="medal">🥉</div>
          <div class="rank-number">3</div>
          <div class="workshop-name">{{ rankingData[2].workshopName }}</div>
          <div class="score">{{ rankingData[2].overallScore }}分</div>
          <div class="metrics">
            <div class="metric-item">
              <span class="label">产量</span>
              <span class="value">{{ rankingData[2].production.toLocaleString() }}</span>
            </div>
            <div class="metric-item">
              <span class="label">效率</span>
              <span class="value">{{ rankingData[2].efficiency }}%</span>
            </div>
            <div class="metric-item">
              <span class="label">质量</span>
              <span class="value">{{ rankingData[2].quality }}%</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 完整排名表格 -->
      <div class="ranking-table">
        <el-table
          :data="rankingData"
          stripe
          style="width: 100%"
        >
          <el-table-column label="排名" width="80" align="center">
            <template #default="{ $index }">
              <div class="rank-badge" :class="getRankClass($index + 1)">
                {{ $index + 1 }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="workshopName" label="车间名称" width="150" />
          <el-table-column prop="managerName" label="车间主管" width="120" />
          <el-table-column label="综合得分" width="150">
            <template #default="{ row }">
              <div class="score-display">
                <span class="score-value">{{ row.overallScore }}</span>
                <el-progress
                  :percentage="row.overallScore"
                  :color="getScoreColor(row.overallScore)"
                  :stroke-width="6"
                  :show-text="false"
                />
              </div>
            </template>
          </el-table-column>
          <el-table-column label="产量" width="150">
            <template #default="{ row }">
              <div class="data-item">
                <span class="data-value">{{ row.production.toLocaleString() }}件</span>
                <span class="data-rank">(第{{ row.productionRank }}名)</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="效率" width="150">
            <template #default="{ row }">
              <div class="data-item">
                <el-progress
                  :percentage="row.efficiency"
                  :color="getEfficiencyColor(row.efficiency)"
                />
                <span class="data-rank">(第{{ row.efficiencyRank }}名)</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="质量合格率" width="150">
            <template #default="{ row }">
              <div class="data-item">
                <el-tag :type="getQualityType(row.quality)">{{ row.quality }}%</el-tag>
                <span class="data-rank">(第{{ row.qualityRank }}名)</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="准时交货率" width="150">
            <template #default="{ row }">
              <div class="data-item">
                <span class="data-value">{{ row.onTimeRate }}%</span>
                <span class="data-rank">(第{{ row.onTimeRank }}名)</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="趋势" width="100" align="center">
            <template #default="{ row }">
              <div class="trend-indicator">
                <el-icon v-if="row.trend === 'up'" class="trend-up"><Top /></el-icon>
                <el-icon v-else-if="row.trend === 'down'" class="trend-down"><Bottom /></el-icon>
                <span v-else class="trend-stable">—</span>
                <span class="trend-value">{{ Math.abs(row.trendValue) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleViewDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 图表分析 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :md="12">
        <div class="chart-card">
          <div class="chart-header">
            <h3>各项指标对比雷达图</h3>
          </div>
          <div class="chart-body" id="radar-chart" style="height: 400px;">
            <svg width="100%" height="100%" viewBox="0 0 400 400" style="background: #fafafa;">
              <!-- 雷达图背景网格 -->
              <g transform="translate(200,200)">
                <!-- 同心圆 -->
                <circle cx="0" cy="0" r="160" fill="none" stroke="#e4e7ed" stroke-width="1"/>
                <circle cx="0" cy="0" r="120" fill="none" stroke="#e4e7ed" stroke-width="1"/>
                <circle cx="0" cy="0" r="80" fill="none" stroke="#e4e7ed" stroke-width="1"/>
                <circle cx="0" cy="0" r="40" fill="none" stroke="#e4e7ed" stroke-width="1"/>
                
                <!-- 坐标轴 -->
                <line x1="0" y1="-160" x2="0" y2="160" stroke="#e4e7ed" stroke-width="1"/>
                <line x1="-160" y1="0" x2="160" y2="0" stroke="#e4e7ed" stroke-width="1"/>
                <line x1="-113" y1="-113" x2="113" y2="113" stroke="#e4e7ed" stroke-width="1"/>
                <line x1="-113" y1="113" x2="113" y2="-113" stroke="#e4e7ed" stroke-width="1"/>
                
                <!-- 车间1数据 -->
                <polygon points="0,-130 95,95 -120,60 -80,-100 60,-140" 
                         fill="rgba(24,144,255,0.3)" stroke="#1890ff" stroke-width="2"/>
                
                <!-- 车间2数据 -->
                <polygon points="0,-110 85,85 -100,50 -60,-80 40,-120" 
                         fill="rgba(82,196,26,0.3)" stroke="#52c41a" stroke-width="2"/>
                
                <!-- 数据点 -->
                <circle cx="0" cy="-130" r="4" fill="#1890ff"/>
                <circle cx="95" cy="95" r="4" fill="#1890ff"/>
                <circle cx="-120" cy="60" r="4" fill="#1890ff"/>
                <circle cx="-80" cy="-100" r="4" fill="#1890ff"/>
                <circle cx="60" cy="-140" r="4" fill="#1890ff"/>
                
                <circle cx="0" cy="-110" r="4" fill="#52c41a"/>
                <circle cx="85" cy="85" r="4" fill="#52c41a"/>
                <circle cx="-100" cy="50" r="4" fill="#52c41a"/>
                <circle cx="-60" cy="-80" r="4" fill="#52c41a"/>
                <circle cx="40" cy="-120" r="4" fill="#52c41a"/>
                
                <!-- 指标标签 -->
                <text x="0" y="-180" text-anchor="middle" fill="#606266" font-size="12">产量</text>
                <text x="140" y="130" text-anchor="middle" fill="#606266" font-size="12">效率</text>
                <text x="-140" y="80" text-anchor="middle" fill="#606266" font-size="12">质量</text>
                <text x="-110" y="-130" text-anchor="middle" fill="#606266" font-size="12">准时率</text>
                <text x="90" y="-170" text-anchor="middle" fill="#606266" font-size="12">满意度</text>
              </g>
              
              <!-- 图例 -->
              <g transform="translate(30,350)">
                <rect x="0" y="0" width="12" height="12" fill="#1890ff"/>
                <text x="20" y="10" fill="#606266" font-size="12">三车间</text>
                <rect x="80" y="0" width="12" height="12" fill="#52c41a"/>
                <text x="100" y="10" fill="#606266" font-size="12">一车间</text>
              </g>
            </svg>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :md="12">
        <div class="chart-card">
          <div class="chart-header">
            <h3>历史排名趋势</h3>
          </div>
          <div class="chart-body" id="trend-chart" style="height: 400px;">
            <svg width="100%" height="100%" viewBox="0 0 400 400" style="background: #fafafa;">
              <!-- 坐标轴 -->
              <g transform="translate(50,350)">
                <!-- Y轴 -->
                <line x1="0" y1="0" x2="0" y2="-300" stroke="#909399" stroke-width="1"/>
                <!-- X轴 -->
                <line x1="0" y1="0" x2="300" y2="0" stroke="#909399" stroke-width="1"/>
                
                <!-- Y轴刻度 -->
                <g>
                  <line x1="-5" y1="-60" x2="5" y2="-60" stroke="#909399"/>
                  <text x="-15" y="-55" text-anchor="end" fill="#606266" font-size="12">4</text>
                  <line x1="-5" y1="-120" x2="5" y2="-120" stroke="#909399"/>
                  <text x="-15" y="-115" text-anchor="end" fill="#606266" font-size="12">3</text>
                  <line x1="-5" y1="-180" x2="5" y2="-180" stroke="#909399"/>
                  <text x="-15" y="-175" text-anchor="end" fill="#606266" font-size="12">2</text>
                  <line x1="-5" y1="-240" x2="5" y2="-240" stroke="#909399"/>
                  <text x="-15" y="-235" text-anchor="end" fill="#606266" font-size="12">1</text>
                </g>
                
                <!-- X轴刻度 -->
                <g>
                  <line x1="60" y1="-5" x2="60" y2="5" stroke="#909399"/>
                  <text x="60" y="20" text-anchor="middle" fill="#606266" font-size="12">周一</text>
                  <line x1="120" y1="-5" x2="120" y2="5" stroke="#909399"/>
                  <text x="120" y="20" text-anchor="middle" fill="#606266" font-size="12">周二</text>
                  <line x1="180" y1="-5" x2="180" y2="5" stroke="#909399"/>
                  <text x="180" y="20" text-anchor="middle" fill="#606266" font-size="12">周三</text>
                  <line x1="240" y1="-5" x2="240" y2="5" stroke="#909399"/>
                  <text x="240" y="20" text-anchor="middle" fill="#606266" font-size="12">周四</text>
                </g>
                
                <!-- 趋势线 - 三车间 -->
                <polyline points="60,-180 120,-240 180,-240 240,-240" 
                          fill="none" stroke="#1890ff" stroke-width="3"/>
                
                <!-- 趋势线 - 一车间 -->
                <polyline points="60,-240 120,-180 180,-180 240,-180" 
                          fill="none" stroke="#52c41a" stroke-width="3"/>
                
                <!-- 趋势线 - 二车间 -->
                <polyline points="60,-120 120,-120 180,-120 240,-120" 
                          fill="none" stroke="#faad14" stroke-width="3"/>
                
                <!-- 数据点 -->
                <circle cx="60" cy="-180" r="4" fill="#1890ff"/>
                <circle cx="120" cy="-240" r="4" fill="#1890ff"/>
                <circle cx="180" cy="-240" r="4" fill="#1890ff"/>
                <circle cx="240" cy="-240" r="4" fill="#1890ff"/>
                
                <circle cx="60" cy="-240" r="4" fill="#52c41a"/>
                <circle cx="120" cy="-180" r="4" fill="#52c41a"/>
                <circle cx="180" cy="-180" r="4" fill="#52c41a"/>
                <circle cx="240" cy="-180" r="4" fill="#52c41a"/>
                
                <circle cx="60" cy="-120" r="4" fill="#faad14"/>
                <circle cx="120" cy="-120" r="4" fill="#faad14"/>
                <circle cx="180" cy="-120" r="4" fill="#faad14"/>
                <circle cx="240" cy="-120" r="4" fill="#faad14"/>
              </g>
              
              <!-- 图例 -->
              <g transform="translate(30,380)">
                <rect x="0" y="0" width="12" height="12" fill="#1890ff"/>
                <text x="20" y="10" fill="#606266" font-size="12">三车间</text>
                <rect x="80" y="0" width="12" height="12" fill="#52c41a"/>
                <text x="100" y="10" fill="#606266" font-size="12">一车间</text>
                <rect x="160" y="0" width="12" height="12" fill="#faad14"/>
                <text x="180" y="10" fill="#606266" font-size="12">二车间</text>
              </g>
            </svg>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`${currentWorkshop?.workshopName} - 详细数据`"
      width="900px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="车间名称">{{ currentWorkshop?.workshopName }}</el-descriptions-item>
        <el-descriptions-item label="车间主管">{{ currentWorkshop?.managerName }}</el-descriptions-item>
        <el-descriptions-item label="工人数量">{{ currentWorkshop?.workerCount }}人</el-descriptions-item>
        <el-descriptions-item label="工位数量">{{ currentWorkshop?.workstationCount }}个</el-descriptions-item>
        <el-descriptions-item label="综合得分">
          <span class="score-highlight">{{ currentWorkshop?.overallScore }}分</span>
        </el-descriptions-item>
        <el-descriptions-item label="当前排名">第{{ currentWorkshop?.currentRank }}名</el-descriptions-item>
      </el-descriptions>

      <el-tabs v-model="activeTab" style="margin-top: 20px;">
        <el-tab-pane label="生产数据" name="production">
          <el-table :data="workshopProductionData" border>
            <el-table-column prop="date" label="日期" width="110" />
            <el-table-column prop="styleName" label="款式" />
            <el-table-column prop="production" label="产量" width="100" align="right" />
            <el-table-column prop="efficiency" label="效率" width="100">
              <template #default="{ row }">
                <el-progress :percentage="row.efficiency" />
              </template>
            </el-table-column>
            <el-table-column prop="quality" label="合格率" width="100">
              <template #default="{ row }">
                <el-tag :type="getQualityType(row.quality)">{{ row.quality }}%</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="员工排名" name="employees">
          <el-table :data="workshopEmployeeData" border>
            <el-table-column type="index" width="50" />
            <el-table-column prop="employeeNo" label="工号" width="100" />
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column prop="position" label="岗位" width="100" />
            <el-table-column prop="production" label="产量" width="100" align="right" />
            <el-table-column prop="efficiency" label="效率" width="100">
              <template #default="{ row }">
                {{ row.efficiency }}%
              </template>
            </el-table-column>
            <el-table-column prop="quality" label="合格率" width="100">
              <template #default="{ row }">
                {{ row.quality }}%
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Top, Bottom } from '@element-plus/icons-vue'

// 筛选表单
const filterForm = reactive({
  period: 'today',
  dateRange: [],
  sortBy: 'overall'
})

// 状态变量
const detailDialogVisible = ref(false)
const activeTab = ref('production')
const currentWorkshop = ref<any>(null)

// 排名数据
const rankingData = ref([
  {
    id: 1,
    workshopName: '三车间',
    managerName: '王主管',
    overallScore: 95.5,
    production: 2580,
    productionRank: 1,
    efficiency: 92.3,
    efficiencyRank: 1,
    quality: 98.5,
    qualityRank: 2,
    onTimeRate: 96.8,
    onTimeRank: 1,
    trend: 'up',
    trendValue: 2,
    workerCount: 42,
    workstationCount: 50,
    currentRank: 1
  },
  {
    id: 2,
    workshopName: '一车间',
    managerName: '张主管',
    overallScore: 88.2,
    production: 2150,
    productionRank: 2,
    efficiency: 85.6,
    efficiencyRank: 3,
    quality: 97.2,
    qualityRank: 3,
    onTimeRate: 94.5,
    onTimeRank: 2,
    trend: 'stable',
    trendValue: 0,
    workerCount: 35,
    workstationCount: 40,
    currentRank: 2
  },
  {
    id: 3,
    workshopName: '二车间',
    managerName: '李主管',
    overallScore: 86.8,
    production: 1980,
    productionRank: 3,
    efficiency: 88.2,
    efficiencyRank: 2,
    quality: 99.1,
    qualityRank: 1,
    onTimeRate: 92.3,
    onTimeRank: 3,
    trend: 'down',
    trendValue: 1,
    workerCount: 28,
    workstationCount: 35,
    currentRank: 3
  },
  {
    id: 4,
    workshopName: '四车间',
    managerName: '赵主管',
    overallScore: 82.5,
    production: 1650,
    productionRank: 4,
    efficiency: 80.3,
    efficiencyRank: 4,
    quality: 95.8,
    qualityRank: 4,
    onTimeRate: 89.2,
    onTimeRank: 4,
    trend: 'up',
    trendValue: 1,
    workerCount: 25,
    workstationCount: 30,
    currentRank: 4
  }
])

// 车间生产数据
const workshopProductionData = ref([
  { date: '2024-01-15', styleName: '经典T恤', production: 580, efficiency: 92, quality: 98.5 },
  { date: '2024-01-14', styleName: '休闲裤', production: 420, efficiency: 88, quality: 97.2 },
  { date: '2024-01-13', styleName: '连衣裙', production: 350, efficiency: 85, quality: 99.1 }
])

// 车间员工数据
const workshopEmployeeData = ref([
  { employeeNo: 'EMP001', name: '张三', position: '缝纫工', production: 120, efficiency: 95, quality: 99 },
  { employeeNo: 'EMP002', name: '李四', position: '裁剪工', production: 150, efficiency: 92, quality: 98 },
  { employeeNo: 'EMP003', name: '王五', position: '组长', production: 100, efficiency: 90, quality: 100 }
])

// 周期变化
const handlePeriodChange = () => {
  if (filterForm.period !== 'custom') {
    filterForm.dateRange = []
  }
  handleQuery()
}

// 排序变化
const handleSortChange = () => {
  // 根据排序方式重新排序数据
  handleQuery()
}

// 查询
const handleQuery = () => {
  ElMessage.success('数据已更新')
  // 调用API获取排名数据
}

// 重置
const handleReset = () => {
  Object.assign(filterForm, {
    period: 'today',
    dateRange: [],
    sortBy: 'overall'
  })
  handleQuery()
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 查看详情
const handleViewDetail = (row: any) => {
  currentWorkshop.value = row
  detailDialogVisible.value = true
}

// 获取排名样式
const getRankClass = (rank: number) => {
  if (rank === 1) return 'gold'
  if (rank === 2) return 'silver'
  if (rank === 3) return 'bronze'
  return ''
}

// 获取分数颜色
const getScoreColor = (score: number) => {
  if (score >= 90) return '#52c41a'
  if (score >= 80) return '#1890ff'
  if (score >= 70) return '#faad14'
  return '#ff4d4f'
}

// 获取效率颜色
const getEfficiencyColor = (efficiency: number) => {
  if (efficiency >= 90) return '#52c41a'
  if (efficiency >= 80) return '#1890ff'
  if (efficiency >= 70) return '#faad14'
  return '#ff4d4f'
}

// 获取质量类型
const getQualityType = (quality: number) => {
  if (quality >= 98) return 'success'
  if (quality >= 95) return 'warning'
  return 'danger'
}

// 初始化
onMounted(() => {
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

// TOP3展示
.top3-container {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 40px;
  margin-bottom: 40px;
  padding: 40px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  
  .top3-item {
    background: #fff;
    border-radius: 12px;
    padding: 30px;
    text-align: center;
    position: relative;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s;
    
    &:hover {
      transform: translateY(-10px);
    }
    
    &.gold {
      width: 280px;
      margin-bottom: 20px;
      
      .medal {
        font-size: 60px;
      }
      
      .rank-number {
        font-size: 48px;
      }
    }
    
    &.silver {
      width: 260px;
      
      .medal {
        font-size: 50px;
      }
      
      .rank-number {
        font-size: 40px;
      }
    }
    
    &.bronze {
      width: 260px;
      
      .medal {
        font-size: 50px;
      }
      
      .rank-number {
        font-size: 40px;
      }
    }
    
    .medal {
      position: absolute;
      top: -30px;
      left: 50%;
      transform: translateX(-50%);
    }
    
    .rank-number {
      font-weight: 700;
      color: #303133;
      margin: 20px 0 10px;
    }
    
    .workshop-name {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 10px;
    }
    
    .score {
      font-size: 28px;
      font-weight: 700;
      color: #1890ff;
      margin-bottom: 20px;
    }
    
    .metrics {
      display: flex;
      justify-content: space-around;
      
      .metric-item {
        display: flex;
        flex-direction: column;
        
        .label {
          font-size: 12px;
          color: #909399;
          margin-bottom: 4px;
        }
        
        .value {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
        }
      }
    }
  }
}

// 排名表格
.ranking-table {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  
  .rank-badge {
    display: inline-block;
    width: 32px;
    height: 32px;
    line-height: 32px;
    border-radius: 50%;
    font-weight: 600;
    
    &.gold {
      background: linear-gradient(135deg, #ffd700, #ffed4e);
      color: #fff;
    }
    
    &.silver {
      background: linear-gradient(135deg, #c0c0c0, #e8e8e8);
      color: #fff;
    }
    
    &.bronze {
      background: linear-gradient(135deg, #cd7f32, #e5a572);
      color: #fff;
    }
  }
  
  .score-display {
    .score-value {
      font-size: 18px;
      font-weight: 600;
      color: #1890ff;
      display: block;
      margin-bottom: 8px;
    }
  }
  
  .data-item {
    .data-value {
      font-weight: 500;
    }
    
    .data-rank {
      display: block;
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }
  }
  
  .trend-indicator {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .trend-up {
      color: #52c41a;
      font-size: 20px;
    }
    
    .trend-down {
      color: #ff4d4f;
      font-size: 20px;
    }
    
    .trend-stable {
      color: #909399;
      font-size: 20px;
    }
    
    .trend-value {
      font-size: 12px;
      color: #909399;
    }
  }
}

// 图表卡片
.chart-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  
  .chart-header {
    margin-bottom: 20px;
    
    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
  }
  
  .chart-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    background: #f5f7fa;
    border-radius: 4px;
    color: #909399;
  }
}

.score-highlight {
  font-size: 24px;
  font-weight: 700;
  color: #1890ff;
}
</style>




