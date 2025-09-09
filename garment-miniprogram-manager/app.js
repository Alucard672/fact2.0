// app.js - 小程序入口

const { authAPI } = require('./utils/api');

App({
  globalData: {
    // 业务网关（admin服务 8080）
    baseUrl: 'http://127.0.0.1:8080',
    // 认证服务（auth服务 8081）
    authBaseUrl: 'http://127.0.0.1:8082',

    token: '',
    userInfo: null,
    tenantId: '',
    username: '',
  },

  onLaunch() {
    // 可在此读取本地缓存覆盖默认 baseUrl
    try {
      const baseUrl = wx.getStorageSync('baseUrl');
      const authBaseUrl = wx.getStorageSync('authBaseUrl');
      if (baseUrl) this.globalData.baseUrl = baseUrl;
      if (authBaseUrl) this.globalData.authBaseUrl = authBaseUrl;

      const token = wx.getStorageSync('token');
      const userInfo = wx.getStorageSync('userInfo');
      const tenantId = wx.getStorageSync('tenantId');
      if (token) this.globalData.token = token;
      if (userInfo) this.globalData.userInfo = userInfo;
      if (tenantId) this.globalData.tenantId = tenantId;
    } catch (e) {}
  },

  // 显示加载
  showLoading(title = '加载中...') {
    wx.showLoading({ title, mask: true });
  },
  hideLoading() { wx.hideLoading(); },

  // 错误提示
  showError(msg) {
    wx.showToast({ title: msg, icon: 'none', duration: 2500 });
  },

  // 保存登录态
  setLogin(token, userInfo) {
    this.globalData.token = token;
    this.globalData.userInfo = userInfo;
    wx.setStorageSync('token', token);
    wx.setStorageSync('userInfo', userInfo);

    // 兼容 LoginResult.UserInfo: 优先使用 tenantId，其次 currentTenantId
    const tid = userInfo && (userInfo.tenantId || userInfo.currentTenantId);
    if (tid) {
      this.globalData.tenantId = tid;
      wx.setStorageSync('tenantId', tid);
    }
  },

  // 登出
  logout() {
    this.globalData.token = '';
    this.globalData.userInfo = null;
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
    wx.removeStorageSync('tenantId');
    wx.reLaunch({ url: '/pages/login/index' });
  },

  // 小程序登录流程，失败时走本地账号密码兜底
  async miniProgramLogin() {
    try {
      const loginRes = await wx.login();
      if (!loginRes || !loginRes.code) throw new Error('获取微信code失败');

      const data = await authAPI.wechatLogin(loginRes.code);

      if (!data || !data.accessToken) throw new Error('微信登录返回异常');

      // 设置登录态（使用 data.user 而非 data.userInfo）
      this.setLogin(data.accessToken, data.user || {});
      return true;
    } catch (err) {
      console.warn('微信登录失败，尝试本地账号密码兜底:', err);
      try {
        const username = wx.getStorageSync('devUsername') || 'admin';
        const password = wx.getStorageSync('devPassword') || 'Admin@123456';
        const data = await authAPI.accountLogin(username, password);
        if (!data || !data.accessToken) throw new Error('账号登录返回异常');
        this.setLogin(data.accessToken, data.user || {});
        return true;
      } catch (e) {
        this.showError('登录失败，请检查认证服务与配置');
        return false;
      }
    }
  }
});




