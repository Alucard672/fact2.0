// utils/api.js - 管理端API请求封装（兼容现有页面调用）

// 安全获取 App 实例，避免在 App 初始化前 getApp() 为 undefined 报错
function getAppSafe() {
  try { return getApp(); } catch (e) { return null; }
}

const DEFAULT_BASE_URL = 'http://127.0.0.1:8080';
const DEFAULT_AUTH_BASE_URL = 'http://127.0.0.1:8082';

// 基础请求方法：统一返回后端 data 字段（当 code===200）
function request(options) {
  return new Promise((resolve, reject) => {
    const {
      url,
      method = 'GET',
      data = {},
      header = {},
      showLoading = false,
      loadingText = '加载中...'
    } = options;

    const app = getAppSafe();

    if (showLoading) {
      if (app && app.showLoading) app.showLoading(loadingText);
      else wx.showLoading({ title: loadingText, mask: true });
    }

    // 读取登录/多租户上下文
    let token = '';
    let tenantId = '';
    let role = '';
    try {
      token = (app && app.globalData && app.globalData.token) || wx.getStorageSync('token') || '';
      tenantId = wx.getStorageSync('tenantId') || '';
      const userInfo = wx.getStorageSync('userInfo') || {};
      role = userInfo.role || (wx.getStorageSync('settings') || {}).role || '';
    } catch (e) {}

    // 允许传入绝对URL（以 http 开头时不再拼接 baseUrl）
    const baseUrl = (app && app.globalData && app.globalData.baseUrl) ? app.globalData.baseUrl : DEFAULT_BASE_URL;
    const finalUrl = /^https?:\/\//.test(url) ? url : `${baseUrl}${url}`;

    const defaultHeaders = { 'Content-Type': 'application/json' };
    if (token) defaultHeaders['Authorization'] = `Bearer ${token}`;
    if (tenantId) defaultHeaders['X-Tenant-Id'] = tenantId;
    if (role) defaultHeaders['X-User-Role'] = role;

    wx.request({
      url: finalUrl,
      method,
      data,
      header: { ...defaultHeaders, ...header },
      success: (res) => {
        if (showLoading) {
          if (app && app.hideLoading) app.hideLoading(); else wx.hideLoading();
        }
        const { statusCode, data } = res || {};
        if (statusCode === 200) {
          // 约定：后端返回 { code, data, message }
          if (data && (data.code === 200 || data.code === 0)) {
            resolve(data.data);
          } else if (data && data.code === 401) {
            // 登录过期处理
            if (app && app.showError) app.showError('登录已过期');
            else wx.showToast({ title: '登录已过期', icon: 'none', duration: 2500 });
            if (app && app.logout) app.logout();
            else wx.reLaunch({ url: '/pages/login/index' });
            reject(new Error('登录已过期'));
          } else {
            const msg = (data && (data.message || data.msg)) || '请求失败';
            if (app && app.showError) app.showError(msg);
            else wx.showToast({ title: msg, icon: 'none', duration: 2500 });
            reject(new Error(msg));
          }
        } else if (statusCode === 401) {
          if (app && app.showError) app.showError('未授权或登录过期');
          else wx.showToast({ title: '未授权或登录过期', icon: 'none', duration: 2500 });
          if (app && app.logout) app.logout();
          else wx.reLaunch({ url: '/pages/login/index' });
          reject(new Error('未授权'));
        } else {
          if (app && app.showError) app.showError('网络请求失败');
          else wx.showToast({ title: '网络请求失败', icon: 'none', duration: 2500 });
          reject(new Error('网络请求失败'));
        }
      },
      fail: (error) => {
        if (showLoading) {
          if (app && app.hideLoading) app.hideLoading(); else wx.hideLoading();
        }
        if (app && app.showError) app.showError('网络连接失败');
        else wx.showToast({ title: '网络连接失败', icon: 'none', duration: 2500 });
        reject(error);
      }
    });
  });
}

// 认证相关API（保持与现有页面兼容的函数签名）
const authAPI = {
  // 微信 code 登录（现有页面传入的是 code 字符串）
  wechatLogin(input) {
    const app = getAppSafe();
    const authBase = (app && app.globalData && app.globalData.authBaseUrl) ? app.globalData.authBaseUrl : DEFAULT_AUTH_BASE_URL;
    const code = (typeof input === 'string') ? input : (input && input.code);
    return request({
      url: `${authBase}/api/auth/wechat-login`,
      method: 'POST',
      data: { code },
      showLoading: true,
      loadingText: '登录中...'
    });
  },

  // 微信手机号授权登录（现有页面传入 { code, encryptedData, iv }）
  wechatPhoneLogin({ code, encryptedData, iv }) {
    const app = getAppSafe();
    const authBase = (app && app.globalData && app.globalData.authBaseUrl) ? app.globalData.authBaseUrl : DEFAULT_AUTH_BASE_URL;
    return request({
      url: `${authBase}/api/auth/wechat-phone-login`,
      method: 'POST',
      data: { code, encryptedData, iv },
      showLoading: true,
      loadingText: '登录中...'
    });
  },

  // 账号密码登录：兼容 (username, password) 或 ({ username, password })
  accountLogin(arg1, arg2) {
    const app = getAppSafe();
    const authBase = (app && app.globalData && app.globalData.authBaseUrl) ? app.globalData.authBaseUrl : DEFAULT_AUTH_BASE_URL;
    const payload = (typeof arg1 === 'object') ? (arg1 || {}) : { username: arg1, password: arg2 };
    return request({
      url: `${authBase}/api/auth/login`,
      method: 'POST',
      data: payload,
      showLoading: true,
      loadingText: '登录中...'
    });
  },

  // 获取用户信息（保持旧接口命名）
  getUserInfo() {
    const app = getAppSafe();
    const authBase = (app && app.globalData && app.globalData.authBaseUrl) ? app.globalData.authBaseUrl : DEFAULT_AUTH_BASE_URL;
    return request({ url: `${authBase}/api/auth/user-info`, method: 'GET' });
  }
};

// 生产管理API（覆盖被页面实际使用到的方法）
const productionAPI = {
  // 获取今日生产概况
  getTodayOverview() {
    return request({ url: '/api/production/today-overview', method: 'GET' });
  },
  // 获取车间生产状态
  getWorkshopStatus() {
    return request({ url: '/api/production/workshop-status', method: 'GET' });
  },
  // 获取待审批事项
  getPendingApprovals() {
    return request({ url: '/api/production/pending-approvals', method: 'GET' });
  },
  // 领工（扫码收包）
  scanTake(qrCode, processId, remark = '') {
    return request({
      url: '/api/production/flows/scan-take',
      method: 'POST',
      data: { qrCode, processId, remark },
      showLoading: true,
      loadingText: '处理中...'
    });
  },
  // 按二维码获取包详情
  getBundleByQrCode(qrCode) {
    const code = encodeURIComponent(qrCode || '');
    return request({ url: `/api/production/bundles/qr-code/${code}`, method: 'GET' });
  },
  // 提交质检（扫码交工）
  submitInspectionByQrCode(qrCode, payload = {}) {
    const data = {
      qrCode,
      quantityOk: payload.qualifiedQuantity || 0,
      quantityNg: payload.defectQuantity || 0,
      qualityScore: payload.qualityScore || 0,
      needRepair: !!payload.needRepair,
      remark: payload.remark || ''
    };
    return request({
      url: '/api/production/flows/scan-submit',
      method: 'POST',
      data,
      showLoading: true,
      loadingText: '提交中...'
    });
  }
};

module.exports = {
  request,
  authAPI,
  productionAPI,
};




