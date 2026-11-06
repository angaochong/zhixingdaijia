<template>
	<view>
		<map id="map" :longitude="longitude" :latitude="latitude" :style="contentStyle" scale="15" :enable-traffic="false" :show-location="true" :enable-poi="true" class="map">
			<cover-image class="location" src="../../static/workbench/location.png" @tap="returnLocationHandle()"></cover-image>
		</map>
		<view class="panel">
			<view class="from" @tap="chooseLocationHandle('from')">
				<text>{{ from.address }}</text>
			</view>
			<view class="dashed-line"></view>
			<view class="to" @tap="chooseLocationHandle('to')">
				<text>{{ to.address }}</text>
			</view>
		</view>
	</view>
</template>

<script>
const chooseLocation = requirePlugin('chooseLocation');
let QQMapWX = require('../../lib/qqmap-wx-jssdk.min.js');

export default {
	data() {
		return {
			from: {
				address: '',
				longitude: 0,
				latitude: 0
			},
			to: {
				address: '输入你的目的地',
				longitude: 0,
				latitude: 0
			},
			longitude: 116.802972,
			latitude: 40.00052,
			contentStyle: '',
			windowHeight: 0,
			map: null,
			// 这是一个标志，为null表示用户没有手动选点
			flag: null
		};
	},
	methods: {
		// 点击回位图标
		returnLocationHandle() {
		    this.map.moveToLocation();
		},
		
		// 用户点击选择起点或终点时回调函数
		chooseLocationHandle(flag){
			let that = this;
			let key = that.tencent.map.key;
			let referer = that.tencent.map.referer;
			// 将当前经纬度传给地图选点插件
			let latitude = that.latitude;
			let longitude = that.longitude;
			that.flag = flag;
			let data = JSON.stringify({
				latitude: latitude,
				longitude: longitude
			});
			// 跳转到地图选点页面,用户在页面中选点后，自动跳回到当前工作台页面
			uni.navigateTo({
				url: `plugin://chooseLocation/index?key=${key}&referer=${referer}&location=${data}`
			})
		}
	},
	onShow: function() {
	    let that = this;
	    // 想要操作地图的第一件事：获取地图上下文对象
	    that.map = uni.createMapContext('map');
	    // QQMapWX 是腾讯地图官方为微信小程序和网页提供的 JavaScript SDK
	    // 用来在小程序里快速调用地图服务（如定位、搜索、路线规划、地址解析等）
	    let qqmapsdk = new QQMapWX({
	        key: that.tencent.map.key
	    });
	    // 绑定 updateLocation 事件。只要事件发生，回调函数执行，拿到最新location，更新经纬度变量值，达到更新地图位置。
	    uni.$on('updateLocation', function(location) {
	        console.log(location);
	        // 当用户手动选点时，flag不会为空。也就是当用户手动选点时，不走以下的逻辑，因为一旦走以下逻辑，自动定位的信息会将用户手动选点覆盖掉。
	      	if(that.flag!=null){
					    return
				  }
	        // 获取最新经度维度
	        let latitude = location.latitude;
	        let longitude = location.longitude;
	        // 将经度维度值赋值给变量
	        that.latitude = latitude;
	        that.longitude = longitude;
	        // 将经度维度设置到上车点
	        that.from.latitude = latitude;
	        that.from.longitude = longitude;
	      
	        //把坐标解析成地址（逆向解析，根据坐标得出地名）
	        qqmapsdk.reverseGeocoder({
	            location: {
	                latitude: latitude,
	                longitude: longitude
	            },
	            success: function(resp) {
	                //console.log(resp);
	                that.from.address = resp.result.address;
	            },
	            fail: function(error) {
	                console.log(error);
	            }
	        });
	    });
		
		// 选完点后，跳回到工作台页面，使用chooseLocation可以获取到用户选的点
		let location = chooseLocation.getLocation();
		if(location != null){
			let place = location.name;
			let latitude = location.latitude;
			let longitude = location.longitude;
			
			// flag决定了用户选的时起点from还是终点to
			if(that.flag == 'from'){
				that.from.address = place;
				that.from.latitude = latitude;
				that.from.longitude = longitude;
			}else{
				that.to.address = place;
				that.to.latitude = latitude;
				that.to.longitude = longitude;
				
				// TOTO 将来用户选号终点后应立即跳转到创建订单页面
				uni.setStorageSync("from", that.from);
				uni.setStorageSync("to", that.to);
				uni.navigateTo({
					url: '../create_order/create_order'
				})
			}
		}
		
	},
	onHide: function() {
	    // 当工作台页面隐藏的时候，记得解除事件绑定：意思就是工作台页面既然隐藏起来了，就没必要再浪费手机电量去更新地图位置了。
	    uni.$off('updateLocation');
		// 页面隐藏时，清除用户地图选点的结果,要不然下次打开时会有残留
		chooseLocation.setLocation(null);
	},

	onLoad: function() {
		//在小程序/uni-app的页面加载时获取设备窗口高度，并将其设置为页面内容区域的高度，用于实现全屏高度的布局
		let that = this;
		let windowHeight = uni.getSystemInfoSync().windowHeight;
		that.windowHeight = windowHeight;
		that.contentStyle = `height:${that.windowHeight}px;`;
		
		// 页面加载时，查询乘客当前的订单状态，如果有未接单的订单跳转到create_order.vue页面
		// 如果有未完成的订单，跳转到move.vue页面
		that.ajax(that.url.hasCustomerCurrentOrder, "POST", null, function(resp){
			let result = resp.data.result;
			let hasCustomerUnAcceptOrder = result.hasCustomerUnAcceptOrder;
			let hasCustomerUnFinishedOrder = result.hasCustomerUnFinishedOrder;
			
			if(hasCustomerUnAcceptOrder){
				let json = result.unAcceptOrder;
				let carType = json.carType;
				let carPlate = json.carPlate;
				let startPlaceLocation = JSON.parse(json.startPlaceLocation);
				let endPlaceLocation= JSON.parse(json.endPlaceLocation);
				let from = {
					address: json.startPlace,
					latitude: startPlaceLocation.latitude,
					longitude: startPlaceLocation.longitude
				};
				let to = {
					address: json.endPlace,
					latitude: endPlaceLocation.latitude,
					longitude: endPlaceLocation.longitude
				};
				uni.setStorageSync('from', from);
				uni.setStorageSync('to', to);
				uni.showModal({
					title: '提示消息',
					content: '您有一个订单等待司机接单，现在将跳转到等待接单页面',
					showCancel: false, 
					success(resp) {
						if(resp.confirm){
							uni.navigateTo({
								url: `../create_order/create_order?showPopup=true&orderId=${json.id}&showCar=true&carType=${carType}&carPlate=${carPlate}`
							})
						}
					}
				})
			}else if(hasCustomerUnFinishedOrder){
				console.log(result.unFinishedOrder);
				uni.showModal({
					title: '提示消息',
					content: '您有一个正在执行的定订单，现在将跳转到司乘同显画面',
					showCancel: false,
					success: function(resp) {
						if (resp.confirm) {
							uni.navigateTo({
								url: '../move/move?orderId=' + result.unFinishedOrder
							});
						}
					}
				});
			}
		}, false);
	},
	onUnload: function() {
		// 页面销毁时，也清除一下
		chooseLocation.setLocation(null);
	}
};
</script>

<style lang="less">
@import url('workbench.less');
</style>
