import App from './App'


// #ifndef VUE3
import Vue from 'vue'
Vue.config.productionTip = false
App.mpType = 'app'
const app = new Vue({
	...App
})

//注册uView组件
import uView from 'uview-ui';
Vue.use(uView);


app.$mount()
// #endif

// #ifdef VUE3
import {
	createSSRApp
} from 'vue'
export function createApp() {
	const app = createSSRApp(App)
	return {
		app
	}
}
// #endif


let baseUrl = "http://192.168.43.5:8201/zxds-driver"

Vue.prototype.url = {
	// 新司机注册
	registerNewDriver: `${baseUrl}/driver/registerNewDriver`,
	// 识别并上传证件到腾讯云对象存储COS
	recognizeAndUpload: `${baseUrl}/ocr/recognizeAndUpload`,
	// 庚辛斯基认证状态为审核中
	updateDriverAuth: `${baseUrl}/driver/updateDriverAuth`,
	// 采集人脸入库的URL。
	createDriverFaceModel: `${baseUrl}/driver/createDriverFaceModel`,
	// 这里把这个也提前写上吧：人脸识别的URL。
	verificateDriverFace: `${baseUrl}/driver/recognition/verificateDriverFace`,
	// 登录
	login: `${baseUrl}/driver/login`,
	// 退出
	logout: `${baseUrl}/driver/logout`,
	// 查询司机基本信息（个人中心展示用的）
	searchDriverBaseInfo: `${baseUrl}/driver/searchDriverBaseInfo`,
	// 查询司机工作台的信息
	searchWorkbenchData: `${baseUrl}/driver/searchWorkbenchData`,
	// 查询司机实名认证信息
	 searchDriverAuth: `${baseUrl}/driver/searchDriverAuth`,
	 // 司机开始接单
	 startWork: `${baseUrl}/driver/startWork`,
	 // 司机停止接单
	 stopWork: `${baseUrl}/driver/stopWork`,
	 // 接收消息
	 receiveNewOrderMessage: `${baseUrl}/message/order/new/receiveNewOrderMessage`,
	 // 司机接单
	 acceptNewOrder: `${baseUrl}/order/acceptNewOrder`,
	 // 司乘同显页面展示数据
	 searchDriverExecuteOrder: `${baseUrl}/order/searchDriverExecuteOrder`,
	 // 查询司机正在执行的订单数据和客户数据
	 searchDriverCurrentOrder: `${baseUrl}/order/searchDriverCurrentOrder`,
	 // 跳转到司乘同显页面后查询页面信息
	 searchOrderForMoveById: `${baseUrl}/order/searchOrderForMoveById`,
	 // 司机点击到达代驾点
	 arriveStartPlace: `${baseUrl}/order/arriveStartPlace`,
	 // 开始代驾
	 startDriving: `${baseUrl}/order/startDriving`,
	 // 上传监控录音文件
	 uploadRecordFile: `${baseUrl}/monitoring/uploadRecordFile`,
	 // 更新订单状态
	 updateOrderStatus: `${baseUrl}/order/updateOrderStatus`,
	 // 更新账单费用
	 updateBillFee: `${baseUrl}/order/updateBillFee`,
	 // 预览代驾账单
	 searchReviewDriverOrderBill: `${baseUrl}/order/searchReviewDriverOrderBill`,
	 // 查询订单状态
	 searchOrderStatus: `${baseUrl}/order/searchOrderStatus`,
	 // 主动查询支付状态并更新支付单
	 updateOrderAboutPayment: `${baseUrl}/order/updateOrderAboutPayment`,
}

Vue.prototype.tencent = {
	map: {
		referer: "智行代驾",
		key: "22NBZ-DTJ3Z-YGPX2-7HTA5-WM6YQ-U4F3U"
	}
}


Vue.prototype.ajax = function(url, method, data, fun, load) {
	let timer = null
	if (load == true || load == undefined) {
		uni.showLoading({
			title: "执行中"
		})
		timer = setTimeout(function() {
			uni.hideLoading()
		}, 60 * 1000)
	}

	uni.request({
		"url": url,
		"method": method,
		"header": {
			token: uni.getStorageSync("token")
		},
		"data": data,
		success: function(resp) {
			if (load == true || load == undefined) {
				clearTimeout(timer)
				uni.hideLoading()
			}
			if (resp.statusCode == 401) {
				uni.redirectTo({
					url: "/pages/login/login.vue"
				})
			} else if (resp.statusCode == 200 && resp.data.code == 200) {
				let data = resp.data
				if (data.hasOwnProperty("token")) {
					let token = data.token
					uni.setStorageSync("token", token)
				}
				fun(resp)
			} else if (resp.data.error=="该微信无法注册") {
				uni.showToast({
					icon:"none",
					title:"该微信无法注册"
				})
			} else {
				uni.showToast({
					icon: "none",
					title: "执行异常"
				})
				console.error(resp.data)
			}
		},
		fail: function(error) {
			if (load == true || load == undefined) {
				clearTimeout(timer)
				uni.hideLoading()
			}
		}
	})
}


Vue.prototype.refreshMessage = function(that) {
	uni.request({
		"url": that.url.refreshMessage,
		"method": "POST",
		"header": {
			token: uni.getStorageSync("token")
		},
		"data": {
			identity: 'driver'
		},
		success: function(resp) {

			if (resp.statusCode == 401) {
				uni.redirectTo({
					url: "/pages/login/login.vue"
				})
			} else if (resp.statusCode == 200 && resp.data.code == 200) {
				uni.$emit("updateMessageService", true)
				let result = resp.data.result
				let lastRows = result.lastRows
				let unreadRows = result.unreadRows
				if (lastRows > 0) {
					uni.$emit("showMessageTip", lastRows)
				}
			} else {
				console.error(resp.data)
				//在工作台页面触发更新消息服务状态，显示服务可用或者不可用
				uni.$emit("updateMessageService", false)
			}
			console.log("刷新消息")
		},
		fail: function(error) {
			//在工作台页面触发更新消息服务状态，显示服务可用或者不可用
			uni.$emit("updateMessageService", false)
		}
	})
}

Vue.prototype.uploadCos = function(url, path, module, fun) {
	uni.uploadFile({
		url: url,
		filePath: path,
		name: "file",
		header: {
			token: uni.getStorageSync("token")
		},
		formData: {
			"module": module
		},
		success: function(resp) {
			let data = JSON.parse(resp.data)
			if (resp.statusCode == 401) {
				uni.redirectTo({
					url: "/pages/login/login.vue"
				})
			} else if (resp.statusCode == 200 && data.code == 200) {
				fun(resp)
			} else {
				uni.showToast({
					icon: "none",
					title: data.error
				})
			}
		}
	})
}

Vue.prototype.upload = function(url, path, data, fun) {
	uni.uploadFile({
		url: url,
		filePath: path,
		name: "file",
		header: {
			token: uni.getStorageSync("token")
		},
		formData: data,
		success: function(resp) {
			let data = JSON.parse(resp.data)
			if (resp.statusCode == 401) {
				uni.redirectTo({
					url: "/pages/login/login.vue"
				})
			} else if (resp.statusCode == 200 && data.code == 200) {
				fun(resp)
			} else {
				uni.showToast({
					icon: "none",
					title: data.error
				})
			}
		}
	})
}

Vue.prototype.toPage = function(url) {
	uni.navigateTo({
		url: url
	})
}


Vue.prototype.checkNull = function(data, name) {
	if (data == null) {
		this.$refs.uToast.show({
			title: name + "不能为空",
			type: 'error'
		})
		return true
	}
	return false
}

Vue.prototype.checkBlank = function(data, name) {
	if (data == null || data == "") {
		this.$refs.uToast.show({
			title: name + "不能为空",
			type: 'error'
		})
		return true
	}
	return false
}

Vue.prototype.checkValidName = function(data, name) {
	if (data == null || data == "") {
		this.$refs.uToast.show({
			title: name + "不能为空",
			type: 'error'
		})
		return false
	} else if (!/^[\u4e00-\u9fa5]{2,15}$/.test(data)) {
		this.$refs.uToast.show({
			title: name + "不正确",
			type: 'error'
		})
		return false
	}
	return true
}
Vue.prototype.checkValidTel = function(data, name) {
	if (data == null || data == "") {
		this.$refs.uToast.show({
			title: name + "不能为空",
			type: 'error'
		})
		return false
	} else if (!/^1[0-9]{10}$/.test(data)) {
		this.$refs.uToast.show({
			title: name + "不正确",
			type: 'error'
		})
		return false
	}
	return true
}
Vue.prototype.checkValidEmail = function(data, name) {
	if (data == null || data == "") {
		this.$refs.uToast.show({
			title: name + "不能为空",
			type: 'error'
		})
		return false
	} else if (!/^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/.test(data)) {
		this.$refs.uToast.show({
			title: name + "不正确",
			type: 'error'
		})
		return false
	}
	return true
}

Vue.prototype.checkValidAddress = function(data, name) {

	if (data == null || data == "") {
		this.$refs.uToast.show({
			title: name + "不能为空",
			type: 'error'
		})
		return false
	} else if (!/^[0-9a-zA-Z\u4e00-\u9fa5\-]{6,50}$/.test(data)) {
		this.$refs.uToast.show({
			title: name + "不正确",
			type: 'error'
		})
		return false
	}
	return true
}

Vue.prototype.checkValidFee = function(data, name) {

	if (data == null || data == "") {
		this.$refs.uToast.show({
			title: name + "不能为空",
			type: 'error'
		})
		return false
	} else if (!/^[1-9]\d*\.\d{1,2}$|^0\.\d{1,2}$|^[1-9]\d*$/.test(data)) {
		this.$refs.uToast.show({
			title: name + "不正确",
			type: 'error'
		})
		return false
	}
	return true
}


Vue.prototype.changeNumber = function(value) {
	let newValue = ['', ''];
	let fr = 1000;
	const ad = 1;
	let num = 3;
	const fm = 1;
	while (value / fr >= 1) {
		fr *= 10;
		num += 1;
	}
	if (num <= 4) {
		// 千
		newValue[1] = '千';
		newValue[0] = parseInt(value / 1000) + '';
	} else if (num <= 8) {
		// 万
		const text1 = parseInt(num - 4) / 3 > 1 ? '千万' : '万';
		const fm = '万' === text1 ? 10000 : 10000000;
		newValue[1] = text1;
		newValue[0] = value / fm + '';
	} else if (num <= 16) {
		// 亿
		let text1 = (num - 8) / 3 > 1 ? '千亿' : '亿';
		text1 = (num - 8) / 4 > 1 ? '万亿' : text1;
		text1 = (num - 8) / 7 > 1 ? '千万亿' : text1;
		// tslint:disable-next-line:no-shadowed-variable
		let fm = 1;
		if ('亿' === text1) {
			fm = 100000000;
		} else if ('千亿' === text1) {
			fm = 100000000000;
		} else if ('万亿' === text1) {
			fm = 1000000000000;
		} else if ('千万亿' === text1) {
			fm = 1000000000000000;
		}
		newValue[1] = text1;
		newValue[0] = parseInt(value / fm) + '';
	}
	if (value < 1000) {
		newValue[1] = '';
		newValue[0] = value + '';
	}
	let temp = Math.floor((newValue[0]) * 100) / 100
	return temp + newValue[1];
}
