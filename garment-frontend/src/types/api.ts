/**
 * API响应类型定义
 */

// 基础响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  success: boolean
  timestamp?: number
}

// 分页响应类型
export interface PageResponse<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 分页请求参数
export interface PageParams {
  current?: number
  size?: number
  sortField?: string
  sortOrder?: 'asc' | 'desc'
}

// 用户信息
export interface UserInfo {
  id: number
  username?: string
  phone: string
  name: string
  avatar?: string
  gender?: 'male' | 'female' | 'unknown'
  currentTenantId?: number
  isSystemAdmin?: boolean
  roles?: string[]
  permissions?: string[]
}

// 登录相关
export interface LoginRequest {
  username?: string
  password?: string
  phone?: string
  smsCode?: string
  tenantCode?: string
}

export interface LoginResult {
  userInfo: UserInfo
  tokenInfo: TokenInfo
  tenantInfo?: TenantInfo
}

export interface TokenInfo {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

// 租户相关
export interface TenantInfo {
  id: number
  tenantCode: string
  companyName: string
  companyType: 'factory' | 'workshop' | 'trading'
  contactPerson: string
  contactPhone: string
  contactEmail?: string
  address?: string
  logoUrl?: string
  subscriptionPlan: 'trial' | 'basic' | 'standard' | 'premium'
  subscriptionEnd?: string
  maxUsers: number
  status: 'pending' | 'active' | 'suspended' | 'expired'
}

// 款式相关
export interface Style {
  id: number
  tenantId: number
  styleCode: string
  styleName: string
  customerId?: number
  customerName?: string
  season?: string
  category?: string
  colors?: string[]
  sizes?: string[]
  sizeRatio?: Record<string, number>
  unitPrice?: number
  description?: string
  imageUrls?: string[]
  techSpec?: string
  status: 'active' | 'inactive'
  createdAt: string
  updatedAt: string
}

// 工序相关
export interface Process {
  id: number
  tenantId: number
  processCode: string
  processName: string
  category?: string
  unit: string
  defaultPrice?: number
  difficultyLevel?: 'easy' | 'medium' | 'hard'
  qualityStandard?: string
  sortOrder?: number
  status: 'active' | 'inactive'
}

// 车间相关
export interface Workshop {
  id: number
  tenantId: number
  workshopCode: string
  workshopName: string
  managerId?: number
  managerName?: string
  description?: string
  sortOrder?: number
  status: 'active' | 'inactive'
}

// 员工相关
export interface Employee {
  id: number
  tenantId: number
  userId?: number
  employeeNo: string
  name: string
  phone: string
  gender?: 'male' | 'female' | 'unknown'
  workshopId?: number
  workshopName?: string
  department?: string
  position?: string
  role: 'owner' | 'admin' | 'manager' | 'supervisor' | 'worker'
  status: 'pending' | 'active' | 'suspended'
  joinedAt?: string
}

// 裁床订单相关
export interface CutOrder {
  id: number
  tenantId: number
  orderNo: string
  styleId: number
  styleName?: string
  styleCode?: string
  color: string
  bedNo?: string
  totalLayers: number
  cuttingType: 'average' | 'segment'
  sizeRatio: Record<string, number>
  segmentPlan?: any
  bundleRules: any
  totalQuantity: number
  bundleCount?: number
  cuttingDate?: string
  deliveryDate?: string
  priority?: 'low' | 'medium' | 'high' | 'urgent'
  remark?: string
  status: 'draft' | 'confirmed' | 'cutting' | 'completed' | 'cancelled'
  createdBy: number
  createdAt: string
}

// 包相关
export interface Bundle {
  id: number
  tenantId: number
  cutOrderId: number
  bundleNo: string
  size: string
  color: string
  quantity: number
  layerFrom?: number
  layerTo?: number
  segmentTag?: 'HIGH' | 'MID' | 'LOW'
  qrCode: string
  barcode?: string
  status: 'pending' | 'in_work' | 'completed' | 'returned' | 'shipped'
  currentProcessId?: number
  currentWorkerId?: number
  progress?: any
  completionRate?: number
  qualityGrade?: 'A' | 'B' | 'C' | 'D'
  defectCount?: number
  printedAt?: string
  startedAt?: string
  completedAt?: string
}

// 生产流水相关
export interface ProductionFlow {
  id: number
  tenantId: number
  bundleId: number
  bundleNo?: string
  processId: number
  processName?: string
  workerId: number
  workerName?: string
  action: 'take' | 'submit' | 'return' | 'repair' | 'cancel'
  quantityOk?: number
  quantityNg?: number
  unitPrice?: number
  amount?: number
  workHours?: number
  efficiency?: number
  qualityScore?: number
  scanTime: string
  location?: string
  remark?: string
}

// 计件工资相关
export interface PieceworkRecord {
  id: number
  tenantId: number
  workerId: number
  workerName?: string
  bundleId: number
  bundleNo?: string
  processId: number
  processName?: string
  quantity: number
  unitPrice: number
  amount: number
  bonus?: number
  penalty?: number
  finalAmount: number
  periodId?: number
  settlementStatus: 'pending' | 'settled' | 'locked'
  workDate: string
}

// 结算期相关
export interface PayrollPeriod {
  id: number
  tenantId: number
  periodName: string
  fromDate: string
  toDate: string
  totalAmount: number
  workerCount: number
  recordCount: number
  status: 'draft' | 'confirmed' | 'locked' | 'paid'
  lockedBy?: number
  lockedAt?: string
  createdBy: number
  createdAt: string
}

// 统计相关
export interface DashboardStats {
  todayProduction: number
  yesterdayProduction: number
  monthProduction: number
  lastMonthProduction: number
  yearProduction: number
  todayWorkers: number
  todayDefects: number
  todayEfficiency: number
  workshopRanking: WorkshopRank[]
}

export interface WorkshopRank {
  workshopId: number
  workshopName: string
  production: number
  efficiency: number
  quality: number
  rank: number
}




