const DEFAULT_API = "http://127.0.0.1:8080";

function getApi() {
  try {
    return wx.getStorageSync("dg_api") || DEFAULT_API;
  } catch (e) {
    return DEFAULT_API;
  }
}

function getUserId() {
  let id = "";
  try {
    id = wx.getStorageSync("dg_uid");
  } catch (e) {}
  if (!id) {
    id = "wx" + Date.now().toString(16).slice(-8);
    try { wx.setStorageSync("dg_uid", id); } catch (e) {}
  }
  return id;
}

function request(path, method, body) {
  return new Promise(function (resolve, reject) {
    wx.request({
      url: getApi() + path,
      method: method || "GET",
      data: body || {},
      header: {
        "Content-Type": "application/json",
        "X-User-Id": getUserId()
      },
      success: function (res) {
        if (res.data && res.data.code === 0) {
          resolve(res.data.data);
        } else {
          reject(new Error((res.data && res.data.message) || "请求失败"));
        }
      },
      fail: function (err) {
        reject(new Error(err.errMsg || "网络失败"));
      }
    });
  });
}

module.exports = { request, getApi, getUserId };
