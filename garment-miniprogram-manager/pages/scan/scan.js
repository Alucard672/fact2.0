// pages/scan/scan.js - 管理端扫码页面
const { productionAPI } = require('../../utils/api');
const storage = require('../../utils/storage');

Page({
  data: {
    role: 'worker',
    currentMode: 'receive', // receive, inspect, query
    scanTitle: '扫码收包',
    scanDesc: '扫描包上的二维码进行收包操作',
    manualCode: '',
    scanResult: null,
    showInspectForm: false,
    inspectData: {
      qualifiedQuantity: 0,
      defectQuantity: 0,
      qualityScore: 5,
      needRepair: false,
      remark: ''
    },
    scanHistory: [],
    actionButtonText: '确认收包',
    lastScanCode: '',
    // 新增：交互中状态，供WXML禁用/loading绑定
    isProcessing: false,
    isSubmitting: false
  },

  // 统一解析扫码载荷：支持 JSON、URL、前缀(BUNDLE-/CUT-) 及纯码
  parseScanPayload(raw) {
    if (!raw && raw !== 0) return { type: 'UNKNOWN', code: '' }
    let s = String(raw).trim()

    // JSON { type:'BUNDLE'|'CUT', code:'xxx' }
    try {
      const obj = JSON.parse(s)
      if (obj && typeof obj === 'object') {
        const t = String(obj.type || obj.kind || '').toUpperCase()
        const c = obj.code || obj.bundleNo || obj.bundle || obj.id
        if (c) return { type: t === 'CUT' ? 'CUT' : 'BUNDLE', code: this.normalizeCode(c) }
      }
    } catch (e) {}

    // URL 参数或路径
    try {
      const u = new URL(s)
      const p = u.searchParams
      const c = p.get('code') || p.get('bundle') || p.get('bundleNo') || p.get('sn')
      if (c) {
        const tt = String(p.get('type') || p.get('kind') || '').toUpperCase()
        return { type: tt === 'CUT' ? 'CUT' : 'BUNDLE', code: this.normalizeCode(c) }
      }
      const path = u.pathname || ''
      let m = path.match(/\/(bundle|bundles|BUNDLE)[\/\-]([A-Za-z0-9_\-]+)/)
      if (m) return { type: 'BUNDLE', code: this.normalizeCode(m[2]) }
      m = path.match(/\/(cut|cuts|CUT)[\/\-]([A-Za-z0-9_\-]+)/)
      if (m) return { type: 'CUT', code: this.normalizeCode(m[2]) }
    } catch (e) {}

    // 前缀
    const up = s.toUpperCase()
    let m = up.match(/^BUNDLE[\s:_-]*([A-Z0-9_\-]+)$/i)
    if (m) return { type: 'BUNDLE', code: this.normalizeCode(m[1]) }
    m = up.match(/^CUT[\s:_-]*([A-Z0-9_\-]+)$/i)
    if (m) return { type: 'CUT', code: this.normalizeCode(m[1]) }

    // 纯码默认按 BUNDLE
    if (/^[A-Za-z0-9_\-]+$/.test(s)) {
      return { type: 'BUNDLE', code: this.normalizeCode(s) }
    }

    return { type: 'UNKNOWN', code: '' }
  },

  normalizeCode(code) {
    return String(code).trim()
  },

  onLoad(options) {
    console.log('管理端扫码页面加载', options);
    // 读取身份
    try {
      const settings = storage.getSettings ? storage.getSettings() : {};
      const role = (settings && settings.role) || 'worker';
      this.setData({ role });
    } catch (e) {}
    
    // 从参数中获取模式
    if (options.mode) {
      this.switchMode({ currentTarget: { dataset: { mode: options.mode } } });
    }
    
    // 进入时确保模式与角色一致（工人不可处于质检）
    this.ensureModeByRole();

    this.loadScanHistory();
  },

  onShow() {
    // 设置自定义tabbar高亮（在原有逻辑前）
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      // 在 worker 与 manager 两套 tabs 中，scan 一般处于索引1或0，交给自定义tabbar自行匹配更稳妥
      this.getTabBar().updateCurrentByRoute && this.getTabBar().updateCurrentByRoute();
    }
    console.log('管理端扫码页面显示');
  },

  ensureModeByRole() {
    if (this.data.role !== 'manager' && this.data.currentMode === 'inspect') {
      this.setData({ currentMode: 'receive', scanTitle: '扫码收包', scanDesc: '扫描包上的二维码进行收包操作', actionButtonText: '确认收包' });
    }
  },

  // 切换扫码模式
  switchMode(e) {
    const mode = e.currentTarget.dataset.mode;
    // 工人不可切到质检
    if (this.data.role !== 'manager' && mode === 'inspect') {
      wx.showToast({ title: '该模式仅管理员可用', icon: 'none' });
      return;
    }

    let title, desc, buttonText;
    
    switch (mode) {
      case 'receive':
        title = '扫码收包';
        desc = '扫描包上的二维码进行收包操作';
        buttonText = '确认收包';
        break;
      case 'inspect':
        title = '质量检查';
        desc = '扫描包上的二维码进行质量检查';
        buttonText = '开始质检';
        break;
      case 'query':
        title = '包信息查询';
        desc = '扫描包上的二维码查看详细信息';
        buttonText = '查看详情';
        break;
    }
    
    this.setData({
      currentMode: mode,
      scanTitle: title,
      scanDesc: desc,
      actionButtonText: buttonText,
      scanResult: null,
      showInspectForm: false,
      manualCode: ''
    });
  },

  // 开始扫码
  startScan() {
    wx.scanCode({
      success: (res) => {
        console.log('扫码结果：', res);
        const parsed = this.parseScanPayload(res.result)
        if (parsed.type === 'CUT') {
          wx.showToast({ title: '识别到裁床单二维码，请在PC端处理', icon: 'none' })
          return
        }
        if (parsed.code) {
          this.handleScanResult(parsed.code);
        } else {
          wx.showToast({ title: '无法识别的二维码', icon: 'none' })
        }
      },
      fail: (error) => {
        console.error('扫码失败：', error);
        wx.showToast({
          title: '扫码失败',
          icon: 'none'
        });
      }
    });
  },

  // 手动输入
  onManualInput(e) {
    this.setData({
      manualCode: e.detail.value
    });
  },

  // 处理手动输入的代码
  handleManualCode() {
    const raw = this.data.manualCode.trim();
    if (raw) {
      const parsed = this.parseScanPayload(raw)
      if (parsed.type === 'CUT') {
        wx.showToast({ title: '识别到裁床单二维码，请在PC端处理', icon: 'none' })
        return
      }
      if (parsed.code) {
        this.handleScanResult(parsed.code);
      } else {
        wx.showToast({ title: '无法识别的二维码', icon: 'none' })
      }
    }
  },

  // 处理扫码结果
  async handleScanResult(code) {
    console.log('处理扫码结果：', code, '模式：', this.data.currentMode);
    
    wx.showLoading({ title: '处理中...' });
    
    try {
      let result;
      const normalized = this.normalizeCode(code)
      
      switch (this.data.currentMode) {
        case 'receive':
          result = await this.handleReceive(normalized);
          break;
        case 'inspect':
          result = await this.handleInspect(normalized);
          break;
        case 'query':
          result = await this.handleQuery(normalized);
          break;
      }
      
      this.setData({
        scanResult: result,
        manualCode: '',
        lastScanCode: normalized
      });
      
      // 保存扫码历史
      this.saveScanHistory(normalized, result);
      
    } catch (error) {
      console.error('处理扫码结果失败：', error);
      this.setData({
        scanResult: {
          success: false,
          message: error.message || '处理失败'
        }
      });
    } finally {
      wx.hideLoading();
    }
  },

  // 处理收包
  async handleReceive(code) {
    try {
      // 扫码阶段仅获取包详情用于确认，不直接执行收包
      const bundleInfo = await productionAPI.getBundleByQrCode(code);
      return {
        success: true,
        data: bundleInfo,
        message: '已获取包信息'
      };
    } catch (error) {
      return {
        success: false,
        message: error.message || '获取包信息失败'
      };
    }
  },

  // 处理质检
  async handleInspect(code) {
    try {
      // 先获取包信息
      const bundleInfo = await productionAPI.getBundleByQrCode(code);
      
      // 初始化质检数据
      this.setData({
        inspectData: {
          qualifiedQuantity: bundleInfo.quantity,
          defectQuantity: 0,
          qualityScore: 5,
          needRepair: false,
          remark: ''
        }
      });
      
      return {
        success: true,
        data: bundleInfo,
        message: '获取包信息成功'
      };
    } catch (error) {
      return {
        success: false,
        message: error.message || '获取包信息失败'
      };
    }
  },

  // 处理查询
  async handleQuery(code) {
    try {
      const bundleInfo = await productionAPI.getBundleByQrCode(code);
      return {
        success: true,
        data: bundleInfo,
        message: '查询成功'
      };
    } catch (error) {
      return {
        success: false,
        message: error.message || '查询失败'
      };
    }
  },

  // 确认操作
  confirmAction() {
    switch (this.data.currentMode) {
      case 'receive':
        this.confirmReceive();
        break;
      case 'inspect':
        this.showInspectForm();
        break;
      case 'query':
        this.viewBundleDetail();
        break;
    }
  },

  // 确认收包
  async confirmReceive() {
    try {
      if (this.data.isProcessing) return;
      this.setData({ isProcessing: true });
      wx.showLoading({ title: '处理中...' });
      const bundle = this.data.scanResult && this.data.scanResult.data;
      if (!bundle) {
        throw new Error('缺少包信息');
      }
      const qrCode = this.data.lastScanCode;
      if (!qrCode) {
        throw new Error('缺少二维码信息');
      }
      const processId = bundle.currentProcessId;
      if (!processId) {
        throw new Error('当前包未设置工序，无法收包');
      }
      await productionAPI.scanTake(qrCode, processId, '扫码收包');
      wx.hideLoading();
      wx.showToast({ title: '收包成功', icon: 'success' });
      // 写入历史（最终确认）
      this.saveScanHistory(qrCode, { success: true, data: { bundleNo: bundle.bundleNo, processId }, message: '收包成功' });
      // 重置状态
      this.setData({ scanResult: null });
    } catch (error) {
      wx.hideLoading();
      wx.showToast({ title: error.message || '收包失败', icon: 'none' });
    } finally {
      this.setData({ isProcessing: false });
    }
  },

  // 显示质检表单
  showInspectForm() {
    this.setData({
      showInspectForm: true
    });
  },

  // 查看包详情
  viewBundleDetail() {
    const bundleData = this.data.scanResult && this.data.scanResult.data;
    if (!bundleData) {
      wx.showToast({ title: '暂无包信息', icon: 'none' });
      return;
    }
    // 先写入历史（查询）
    try {
      const code = this.data.lastScanCode || (bundleData && bundleData.qrCode) || (bundleData && bundleData.bundleNo) || '';
      this.saveScanHistory(code, { success: true, data: { bundleNo: bundleData.bundleNo, id: bundleData.id }, message: '查询成功' });
    } catch (e) {}

    // 优先尝试跳转到详情页，如页面不存在则降级为弹窗查看
    const url = `/pages/bundle-detail/bundle-detail?bundleId=${bundleData.id}`;
    wx.navigateTo({
      url,
      fail: () => {
        wx.showModal({
          title: '包详情',
          content: JSON.stringify({
            包号: bundleData.bundleNo,
            工序: bundleData.currentProcessName || '',
            数量: bundleData.quantity,
            状态: bundleData.status || ''
          }, null, 2),
          showCancel: false
        });
      }
    });
  },

  // 质检表单输入
  onInspectInput(e) {
    const field = e.currentTarget.dataset.field;
    const value = e.detail.value;
    
    this.setData({
      [`inspectData.${field}`]: value
    });
  },

  // 质量评分改变
  onQualityScoreChange(e) {
    this.setData({
      'inspectData.qualityScore': e.detail.value
    });
  },

  // 返修开关改变
  onRepairSwitchChange(e) {
    this.setData({
      'inspectData.needRepair': e.detail.value
    });
  },

  // 提交质检
  async submitInspect() {
    const { inspectData, scanResult, lastScanCode } = this.data;

    if (!scanResult || !scanResult.data) {
      wx.showToast({ title: '请先扫码获取包信息', icon: 'none' });
      return;
    }
    if (this.data.isSubmitting) return;

    const bundle = scanResult.data;

    // 基本校验
    const q = parseInt(inspectData.qualifiedQuantity, 10) || 0;
    const d = parseInt(inspectData.defectQuantity, 10) || 0;
    if (q < 0 || d < 0) {
      wx.showToast({ title: '数量不能为负数', icon: 'none' });
      return;
    }
    if (q + d !== bundle.quantity) {
      wx.showToast({ title: '合格+次品 应等于 包总数量', icon: 'none' });
      return;
    }
    if (!!inspectData.needRepair && d === 0) {
      wx.showToast({ title: '需要返修时，次品数量应大于0', icon: 'none' });
      return;
    }
    const score = Number(inspectData.qualityScore);
    if (isNaN(score) || score < 1 || score > 5) {
      wx.showToast({ title: '质量评分应在1-5', icon: 'none' });
      return;
    }

    try {
      this.setData({ isSubmitting: true });
      wx.showLoading({ title: '提交中...' });
      const payload = {
        qualifiedQuantity: q,
        defectQuantity: d,
        qualityScore: score,
        needRepair: !!inspectData.needRepair,
        remark: inspectData.remark || ''
      };
      await productionAPI.submitInspectionByQrCode(lastScanCode, payload);
      wx.hideLoading();
      wx.showToast({ title: '提交成功', icon: 'success' });
      // 写入历史（最终确认）
      this.saveScanHistory(lastScanCode, { success: true, data: { bundleNo: bundle.bundleNo, ...payload }, message: '质检提交成功' });
      // 重置
      this.setData({ scanResult: null, showInspectForm: false });
    } catch (error) {
      wx.hideLoading();
      wx.showToast({ title: error.message || '提交失败', icon: 'none' });
    } finally {
      this.setData({ isSubmitting: false });
    }
  },

  // 取消质检
  cancelInspect() {
    this.setData({
      showInspectForm: false
    });
  },

  // 取消操作（保留占位）
  cancelAction() {
    this.setData({
      showInspectForm: false
    });
  },

  // 重新扫码
  retryScan() {
    this.setData({
      scanResult: null,
      showInspectForm: false
    });
    this.startScan();
  },

  // 扫码历史记录
  saveScanHistory(code, result) {
    try {
      const history = wx.getStorageSync('scanHistory') || [];
      const now = Date.now();
      const displayTime = new Date(now).toLocaleString();
      const actionText = this.getActionText();
      const bundleNo = result && result.success && result.data && result.data.bundleNo ? result.data.bundleNo : code;
      const entry = {
        // 新结构
        time: now,
        displayTime,
        mode: this.data.currentMode,
        code,
        success: !!(result && result.success),
        message: (result && result.message) || '',
        data: result && result.data ? result.data : null,
        // 兼容WXML旧结构字段
        id: now,
        bundleNo,
        actionText,
        scanTime: displayTime
      };
      history.unshift(entry);
      wx.setStorageSync('scanHistory', history.slice(0, 50));
      this.setData({ scanHistory: history.slice(0, 20) });
    } catch (e) {}
  },

  getActionText() {
    const map = {
      receive: '收包',
      inspect: '质检',
      query: '查看'
    };
    return map[this.data.currentMode] || '操作';
  },

  loadScanHistory() {
    try {
      const raw = wx.getStorageSync('scanHistory') || [];
      const normalized = raw.map(item => {
        const displayTime = item.displayTime || (item.time ? new Date(item.time).toLocaleString() : '');
        const actionText = item.actionText || (item.mode === 'receive' ? '收包' : item.mode === 'inspect' ? '质检' : '查看');
        const bundleNo = item.bundleNo || (item.data && item.data.bundleNo) || item.code;
        return {
          // 保留原字段
          ...item,
          // 兼容补齐
          id: item.id || item.time || Date.now(),
          displayTime,
          actionText,
          bundleNo,
          scanTime: item.scanTime || displayTime
        };
      });
      this.setData({ scanHistory: normalized.slice(0, 20) });
    } catch (e) {}
  },

  viewHistoryDetail(e) {
    const ds = e.currentTarget && e.currentTarget.dataset ? e.currentTarget.dataset : {};
    const item = ds.item != null ? ds.item : this.data.scanHistory[ds.index];
    if (!item) return;
    wx.showModal({
      title: '历史详情',
      content: JSON.stringify(item, null, 2),
      showCancel: false
    });
  },

  clearHistory() {
    try {
      wx.removeStorageSync('scanHistory');
      this.setData({ scanHistory: [] });
      wx.showToast({ title: '已清空', icon: 'none' });
    } catch (e) {}
  },

  onPullDownRefresh() {
    wx.stopPullDownRefresh();
  },

  onShareAppMessage() {
    return {
      title: '扫码管理',
      path: '/pages/scan/scan'
    };
  }
});




