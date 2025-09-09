const storage = require('../utils/storage');

Component({
  data: {
    tabs: [],
    current: 0
  },
  lifetimes: {
    attached() {
      this.initTabs();
      // 延迟到下一个事件循环，避免在页面尚未入栈时读取 route
      setTimeout(() => {
        this.updateCurrentByRoute();
      }, 0);
    }
  },
  methods: {
    initTabs() {
      // 根据角色决定显示哪些 tab
      let role = 'worker';
      try {
        const settings = storage.getSettings && storage.getSettings();
        if (settings && settings.role) role = settings.role;
      } catch (e) {}

      const workerTabs = [
        { pagePath: '/pages/scan/scan', text: '扫码' },
        { pagePath: '/pages/worker/records/index', text: '工单' },
        { pagePath: '/pages/worker/salary/index', text: '工资' }
      ];
      const managerTabs = [
        { pagePath: '/pages/index/index', text: '工作台' },
        { pagePath: '/pages/scan/scan', text: '扫码' }
      ];
      this.setData({ tabs: role === 'manager' ? managerTabs : workerTabs });
    },
    updateCurrentByRoute() {
      const pages = getCurrentPages && getCurrentPages();
      if (!pages || !pages.length) return;
      const last = pages[pages.length - 1];
      // 兼容不同基础库下的路由字段
      const route = (last && (last.route || last.__route__)) || '';
      const currentRoute = route.startsWith('/') ? route : '/' + route;
      const idx = this.data.tabs.findIndex(t => t.pagePath === currentRoute);
      if (idx >= 0) this.setData({ current: idx });
    },
    onTabTap(e) {
      const path = e.currentTarget.dataset.path;
      wx.switchTab({ url: path });
      // 导航后更新当前选中项（可能在页面 onShow 中再次调用，以确保一致）
      setTimeout(() => this.updateCurrentByRoute(), 0);
    }
  }
});