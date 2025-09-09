// pages/index/index.js - 管理端首页
const { productionAPI, authAPI } = require('../../utils/api');
const storage = require('../../utils/storage');

Page({
  data: {
    userInfo: {},
    currentTime: '',
    currentDate: '',
    todayStats: {
      totalProduction: 0,
      activeWorkers: 0,
      completionRate: 0,
      qualityRate: 0,
      productionChange: 0,
      completionChange: 0,
      qualityChange: 0,
      totalWorkers: 0
    },
    workshopList: [],
    pendingList: [],
    notifications: [],
    refreshing: false,
    autoRefreshTimer: null
  },

  onLoad() {
    console.log('管理端首页加载');
    this.initPage();
  },

  onShow() {
    console.log('管理端首页显示');
    // 设置自定义tabbar高亮
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ current: 0 });
    }
    this.refreshData();
    this.startAutoRefresh();
  },

  onHide() {
    console.log('管理端首页隐藏');
    this.stopAutoRefresh();
  },

  onUnload() {
    this.stopAutoRefresh();
  },

  // 初始化页面
  initPage() {
    this.updateTime();
    this.loadUserInfo();
    this.loadCachedData();
    this.refreshData();
  },

  // 更新时间显示
  updateTime() {
    const now = new Date();
    const timeStr = now.toTimeString().substring(0, 8);
    const dateStr = now.toLocaleDateString('zh-CN', {
      month: 'long',
      day: 'numeric',
      weekday: 'long'
    });
    
    this.setData({
      currentTime: timeStr,
      currentDate: dateStr
    });
    
    // 每秒更新时间
    setTimeout(() => {
      this.updateTime();
    }, 1000);
  },

  // 加载用户信息
  loadUserInfo() {
    const userInfo = storage.getUserInfo();
    if (userInfo) {
      this.setData({ userInfo });
    } else {
      this.getUserInfo();
    }
  },

  // 获取用户信息
  async getUserInfo() {
    try {
      const userInfo = await authAPI.getUserInfo();
      this.setData({ userInfo });
      storage.setUserInfo(userInfo);
    } catch (error) {
      console.error('获取用户信息失败：', error);
    }
  },

  // 加载缓存数据
  loadCachedData() {
    const todayStats = storage.getCacheData('todayStats');
    const workshopList = storage.getCacheData('workshopList');
    const pendingList = storage.getCacheData('pendingList');
    
    if (todayStats) this.setData({ todayStats });
    if (workshopList) this.setData({ workshopList });
    if (pendingList) this.setData({ pendingList });
  },

  // 刷新数据
  async refreshData() {
    if (this.data.refreshing) return;
    
    this.setData({ refreshing: true });
    
    try {
      await Promise.all([
        this.loadTodayStats(),
        this.loadWorkshopStatus(),
        this.loadPendingItems()
      ]);
    } catch (error) {
      console.error('刷新数据失败：', error);
      wx.showToast({
        title: '刷新失败',
        icon: 'none'
      });
    } finally {
      this.setData({ refreshing: false });
    }
  },

  // 加载今日统计
  async loadTodayStats() {
    try {
      const stats = await productionAPI.getTodayOverview();
      this.setData({ todayStats: stats });
      storage.setCacheData('todayStats', stats, 60000); // 缓存1分钟
    } catch (error) {
      console.error('加载今日统计失败：', error);
    }
  },

  // 加载车间状态
  async loadWorkshopStatus() {
    try {
      const workshopList = await productionAPI.getWorkshopStatus();
      this.setData({ workshopList });
      storage.setCacheData('workshopList', workshopList, 30000); // 缓存30秒
    } catch (error) {
      console.error('加载车间状态失败：', error);
    }
  },

  // 加载待处理事项
  async loadPendingItems() {
    try {
      const pendingList = await productionAPI.getPendingApprovals();
      this.setData({ pendingList: pendingList.slice(0, 5) }); // 只显示前5个
      storage.setCacheData('pendingList', pendingList, 120000); // 缓存2分钟
    } catch (error) {
      console.error('加载待处理事项失败：', error);
    }
  },

  // 开始自动刷新
  startAutoRefresh() {
    const settings = storage.getSettings();
    if (settings.autoRefresh) {
      this.data.autoRefreshTimer = setInterval(() => {
        this.refreshData();
      }, settings.refreshInterval);
    }
  },

  // 停止自动刷新
  stopAutoRefresh() {
    if (this.data.autoRefreshTimer) {
      clearInterval(this.data.autoRefreshTimer);
      this.data.autoRefreshTimer = null;
    }
  },

  // 刷新车间状态
  refreshWorkshopStatus() {
    this.loadWorkshopStatus();
  },

  // 处理待处理事项点击
  handlePendingItem(e) {
    const item = e.currentTarget.dataset.item;
    console.log('处理待处理事项：', item);
    
    // 根据类型跳转到相应页面
    switch (item.type) {
      case 'approval':
        wx.navigateTo({
          url: `/pages/approval/approval?id=${item.id}`
        });
        break;
      case 'quality':
        wx.navigateTo({
          url: `/pages/scan/scan?mode=quality&bundleId=${item.bundleId}`
        });
        break;
      default:
        wx.showToast({
          title: '功能开发中',
          icon: 'none'
        });
    }
  },

  // 查看全部待处理事项
  viewAllPending() {
    wx.navigateTo({
      url: '/pages/approval/approval'
    });
  },

  // 导航到扫码页面
  navigateToScan() {
    wx.switchTab({
      url: '/pages/scan/scan'
    });
  },

  // 导航到统计页面
  navigateToStats() {
    wx.switchTab({
      url: '/pages/stats/stats'
    });
  },

  // 导航到审批页面
  navigateToApproval() {
    wx.switchTab({
      url: '/pages/approval/approval'
    });
  },

  // 导出报表
  exportReport() {
    wx.showActionSheet({
      itemList: ['今日报表', '本周报表', '本月报表', '自定义报表'],
      success: (res) => {
        const reportTypes = ['daily', 'weekly', 'monthly', 'custom'];
        const reportType = reportTypes[res.tapIndex];
        
        if (reportType === 'custom') {
          wx.navigateTo({
            url: '/pages/stats/stats?tab=export'
          });
        } else {
          this.doExportReport(reportType);
        }
      }
    });
  },

  // 执行导出报表
  async doExportReport(type) {
    try {
      wx.showLoading({ title: '生成中...' });
      
      // 这里应该调用实际的导出API
      await new Promise(resolve => setTimeout(resolve, 2000));
      
      wx.hideLoading();
      wx.showToast({
        title: '报表已生成',
        icon: 'success'
      });
    } catch (error) {
      wx.hideLoading();
      wx.showToast({
        title: '导出失败',
        icon: 'none'
      });
    }
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.refreshData().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  // 分享
  onShareAppMessage() {
    return {
      title: '服装生产管理系统',
      path: '/pages/index/index'
    };
  }
});




