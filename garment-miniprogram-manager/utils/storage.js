// utils/storage.js - 管理端本地存储工具

const STORAGE_KEYS = {
  TOKEN: 'manager_token',
  USER_INFO: 'manager_userInfo',
  SETTINGS: 'manager_settings',
  CACHE_DATA: 'manager_cache_data',
  WORKSHOP_CONFIG: 'manager_workshop_config',
  APPROVAL_CONFIG: 'manager_approval_config'
};

class Storage {
  // 设置数据
  set(key, value) {
    try {
      wx.setStorageSync(key, value);
      return true;
    } catch (error) {
      console.error('存储数据失败：', error);
      return false;
    }
  }

  // 获取数据
  get(key, defaultValue = null) {
    try {
      const value = wx.getStorageSync(key);
      return value !== '' ? value : defaultValue;
    } catch (error) {
      console.error('获取数据失败：', error);
      return defaultValue;
    }
  }

  // 删除数据
  remove(key) {
    try {
      wx.removeStorageSync(key);
      return true;
    } catch (error) {
      console.error('删除数据失败：', error);
      return false;
    }
  }

  // 清空所有数据
  clear() {
    try {
      wx.clearStorageSync();
      return true;
    } catch (error) {
      console.error('清空数据失败：', error);
      return false;
    }
  }

  // Token相关
  setToken(token) {
    return this.set(STORAGE_KEYS.TOKEN, token);
  }

  getToken() {
    return this.get(STORAGE_KEYS.TOKEN);
  }

  removeToken() {
    return this.remove(STORAGE_KEYS.TOKEN);
  }

  // 用户信息相关
  setUserInfo(userInfo) {
    return this.set(STORAGE_KEYS.USER_INFO, userInfo);
  }

  getUserInfo() {
    return this.get(STORAGE_KEYS.USER_INFO);
  }

  removeUserInfo() {
    return this.remove(STORAGE_KEYS.USER_INFO);
  }

  // 设置相关
  setSettings(settings) {
    const currentSettings = this.getSettings();
    const newSettings = { ...currentSettings, ...settings };
    return this.set(STORAGE_KEYS.SETTINGS, newSettings);
  }

  getSettings() {
    return this.get(STORAGE_KEYS.SETTINGS, {
      autoRefresh: true,
      refreshInterval: 30000, // 30秒
      soundEnabled: true,
      vibrationEnabled: true,
      showNotifications: true,
      defaultWorkshop: null,
      defaultDateRange: 'today'
    });
  }

  // 缓存数据相关
  setCacheData(key, data, expireTime = 300000) { // 默认5分钟过期
    const cacheItem = {
      data,
      timestamp: Date.now(),
      expireTime
    };
    
    const cacheData = this.get(STORAGE_KEYS.CACHE_DATA, {});
    cacheData[key] = cacheItem;
    
    return this.set(STORAGE_KEYS.CACHE_DATA, cacheData);
  }

  getCacheData(key) {
    const cacheData = this.get(STORAGE_KEYS.CACHE_DATA, {});
    const cacheItem = cacheData[key];
    
    if (!cacheItem) {
      return null;
    }
    
    // 检查是否过期
    if (Date.now() - cacheItem.timestamp > cacheItem.expireTime) {
      delete cacheData[key];
      this.set(STORAGE_KEYS.CACHE_DATA, cacheData);
      return null;
    }
    
    return cacheItem.data;
  }

  // 清除过期缓存
  clearExpiredCache() {
    const cacheData = this.get(STORAGE_KEYS.CACHE_DATA, {});
    const now = Date.now();
    
    Object.keys(cacheData).forEach(key => {
      const cacheItem = cacheData[key];
      if (now - cacheItem.timestamp > cacheItem.expireTime) {
        delete cacheData[key];
      }
    });
    
    return this.set(STORAGE_KEYS.CACHE_DATA, cacheData);
  }

  // 车间配置相关
  setWorkshopConfig(config) {
    return this.set(STORAGE_KEYS.WORKSHOP_CONFIG, config);
  }

  getWorkshopConfig() {
    return this.get(STORAGE_KEYS.WORKSHOP_CONFIG, {
      selectedWorkshops: [],
      displayMode: 'grid', // grid, list
      sortBy: 'efficiency', // efficiency, production, quality
      showInactive: false
    });
  }

  // 审批配置相关
  setApprovalConfig(config) {
    return this.set(STORAGE_KEYS.APPROVAL_CONFIG, config);
  }

  getApprovalConfig() {
    return this.get(STORAGE_KEYS.APPROVAL_CONFIG, {
      autoRefresh: true,
      showPriority: true,
      groupBy: 'type', // type, workshop, date
      filterStatus: 'pending' // pending, all, approved, rejected
    });
  }

  // 获取存储信息
  getStorageInfo() {
    try {
      return wx.getStorageInfoSync();
    } catch (error) {
      console.error('获取存储信息失败：', error);
      return null;
    }
  }
}

// 创建单例
const storage = new Storage();

module.exports = storage;




