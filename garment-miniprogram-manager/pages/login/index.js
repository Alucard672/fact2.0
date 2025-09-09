const { authAPI } = require('../../utils/api');
const storage = require('../../utils/storage');

Page({
  data: {
    loggingIn: false
  },

  onLoad() {
    // 若已登录则自动跳转
    try {
      const token = wx.getStorageSync('token');
      const userInfo = wx.getStorageSync('userInfo');
      if (token && userInfo) {
        this.redirectByRole(userInfo.role);
      }
    } catch (e) {}
  },

  redirectByRole(role) {
    // 保存角色到本地，供自定义 tab 使用
    try {
      const settings = storage.getSettings ? storage.getSettings() : {};
      storage.setSettings && storage.setSettings({ ...settings, role });
    } catch (e) {}

    if (role === 'worker') {
      wx.switchTab({ url: '/pages/scan/scan' });
    } else {
      wx.switchTab({ url: '/pages/index/index' });
    }
  },

  async onGetPhoneNumber(e) {
    if (this.data.loggingIn) return;
    if (!e || !e.detail) return;

    const { errMsg, encryptedData, iv } = e.detail;
    if (!errMsg || !errMsg.includes('ok')) {
      wx.showToast({ title: '未授权手机号', icon: 'none' });
      return;
    }

    this.setData({ loggingIn: true });
    try {
      const loginRes = await wx.login();
      if (!loginRes || !loginRes.code) throw new Error('获取微信code失败');

      const data = await authAPI.wechatPhoneLogin({
        code: loginRes.code,
        encryptedData,
        iv
      });

      if (!data || !data.accessToken) throw new Error('登录返回异常');

      // 写入登录态
      const app = getApp();
      app.setLogin && app.setLogin(data.accessToken, data.user || {});

      this.redirectByRole((data.user && data.user.role) || 'worker');
    } catch (error) {
      console.error('手机号登录失败', error);
      wx.showToast({ title: error.message || '登录失败', icon: 'none' });
    } finally {
      this.setData({ loggingIn: false });
    }
  }
});