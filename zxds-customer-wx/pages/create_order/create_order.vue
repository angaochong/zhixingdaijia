<template>
	<view>
		<view class="address-container">
			<view class="from">
				<text>{{ from.address }}</text>
			</view>
			<view class="dashed-line"></view>
			<view class="to">
				<text>{{ to.address }}</text>
			</view>
		</view>
		<map
			id="map"
			:longitude="from.longitude"
			:latitude="from.latitude"
			:style="contentStyle"
			scale="12"
			:enable-traffic="false"
			:show-location="true"
			class="map"
			:polyline="polyline"
			:markers="markers"
		></map>

		<view class="panel">
			<view class="row">
				<view class="info">
					<view class="label">全程</view>
					<view class="value">
						<text>{{ distance }}</text>
						公里
					</view>
					<view class="label">，预计</view>
					<view class="value">
						<text>{{ duration }}</text>
						分钟
					</view>
				</view>
				<view class="opt" @tap="chooseCarHandle" v-if="!showCar">选择车辆</view>
				<view class="opt" @tap="chooseCarHandle" v-if="showCar">{{ carPlate }}</view>
			</view>
			<button class="btn" @tap="createOrderHandle">确定下单</button>
		</view>
		<u-popup v-model="showPopup" mode="center" width="600" height="350" :mask-close-able="false">
			<view class="popup-title">您的订单正在等待司机接单</view>
			<view class="count-down">
				<u-count-down
					ref="uCountDown"
					:timestamp="timestamp"
					:autoplay="false"
					separator="zh"
					:show-hours="false"
					:show-border="true"
					bg-color="#DDF0FF"
					separator-color="#0083F3"
					border-color="#0D90FF"
					color="#0D90FF"
					font-size="32"
					@end="countEndHandle"
					@change="countChangeHandle"
				></u-count-down>
			</view>
			<button class="btn" @tap="cancelHandle">取消订单</button>
		</u-popup>
	</view>
</template>

<script>
let QQMapWX = require('../../lib/qqmap-wx-jssdk.min.js');
let qqmapsdk;
export default {
	data() {
		return {
			from: {
				address: '',
				longitude: 0,
				latitude: 0
			},
			to: {
				address: '',
				longitude: 0,
				latitude: 0
			},
			contentStyle: '',
			windowHeight: 0,
			distance: 0,
			duration: 0,
			polyline: [],
			markers: [],
			infoStatus: true,
			carId: null,
			carPlate: null,
			carType: null,
			showCar: false,
			showPopup: false,
			timestamp: 60,
			orderId: null,
			map: null
		};
	},
	methods: {
		// 处理路线的折线坐标算法
		formatPolyline(polyline) {
			let coors = polyline;
			let pl = [];
			//坐标解压（返回的点串坐标，通过前向差分进行压缩）
			const kr = 1000000;
			for (let i = 2; i < coors.length; i++) {
				coors[i] = Number(coors[i - 2]) + Number(coors[i]) / kr;
			}
			//将解压后的坐标放入点串数组pl中
			for (let i = 0; i < coors.length; i += 2) {
				pl.push({
					longitude: coors[i + 1],
					latitude: coors[i]
				});
			}
			return pl;
		},
		// 计算路线
		calculateLine(ref) {
		    // 调用腾讯地图API的路线规划服务（驾车模式）
		    qqmapsdk.direction({
		        mode: 'driving', // 交通方式：驾车
		        from: { // 起点坐标
		            latitude: ref.from.latitude,
		            longitude: ref.from.longitude
		        },
		        to: { // 终点坐标
		            latitude: ref.to.latitude,
		            longitude: ref.to.longitude
		        },
		        success: function(resp) {
		            // 校验API返回状态
		            if (resp.status != 0) {
		                uni.showToast({
		                    icon: 'error',
		                    title: resp.message
		                });
		                return;
		            }
		            
		            // 获取第一条路线数据
		            let route = resp.result.routes[0];
		            let distance = route.distance; // 路线距离（米）
		            let duration = route.duration; // 路线时间（分钟）
		            let polyline = route.polyline; // 路线坐标点（加密字符串）
		            
		            // 处理距离数据：米转公里并保留一位小数
		            ref.distance = Math.ceil((distance / 1000) * 10) / 10;
		            ref.duration = duration; // 存储预计时间
		            
		            // 将加密的折线坐标解析为坐标点数组
		            let points = ref.formatPolyline(polyline);
		            
		            // 设置地图折线（路线）显示配置
		            ref.polyline = [
		                {
		                    points: points, // 路线坐标点数组
		                    width: 6,       // 路线宽度
		                    color: '#05B473', // 路线颜色
		                    arrowLine: true  // 显示方向箭头
		                }
		            ];
		            
		            // 设置地图标记点（起点和终点）
		            ref.markers = [
		                {
		                    id: 1, // 起点标记（随便写，保证不重复就行。）
		                    latitude: ref.from.latitude,
		                    longitude: ref.from.longitude,
		                    width: 25, // 图标宽度
		                    height: 35, // 图标高度
		                    anchor: { // 图标的中心对准坐标
		                        x: 0.5,
		                        y: 0.5
		                    },
		                    iconPath: 'https://mapapi.qq.com/web/lbs/javascriptGL/demo/img/start.png' // 起点图标
		                },
		                {
		                    id: 2, // 终点标记
		                    latitude: ref.to.latitude,
		                    longitude: ref.to.longitude,
		                    width: 25,
		                    height: 35,
		                    anchor: {
		                        x: 0.5,
		                        y: 0.5
		                    },
		                    iconPath: 'https://mapapi.qq.com/web/lbs/javascriptGL/demo/img/end.png' // 终点图标
		                }
		            ];
		        }
		    });
		},
		
		chooseCarHandle(){
			uni.navigateTo({
				url: '../car_list/car_list'
			})
		},
		
		createOrderHandle(){
			let that = this;
			if(that.carType == null || that.carPlate == null){
				uni.showToast({
					icon: "error",
					title: "没有设置代驾车辆"
				});
				return;
			}
			
			uni.showLoading({
				title: "下单中，请稍后"
			});
			
			setTimeout(function(){
				uni.hideLoading();
			}, 60000);
			
			let data = {
				startPlace: that.from.address,
				startPlaceLatitude: that.from.latitude,
				startPlaceLongitude: that.from.longitude,
				endPlace: that.to.address,
				endPlaceLatitude: that.to.latitude,
				endPlaceLongitude: that.to.longitude,
				favourFee: '0.0',
				carPlate: that.carPlate,
				carType: that.carType
			};
			that.ajax(that.url.createNewOrder, "POST", data, function(resp){
				uni.hideLoading();
				if(resp.data.result.count > 0){
					uni.showToast({
						icon: 'success',
						title: '订单创建成功'
					});
					// 订单创建成功就启动倒计时弹窗
					setTimeout(function(){
						that.orderId = resp.data.result.orderId;
						that.showPopup = true;
						that.timestamp = 60;
						that.$refs.uCountDown.start();
					});
				}else{
					uni.showToast({
						icon: 'none',
						title: '没有适合接单的司机'
					});
				}
			});
		},
		
		// 封装查询订单订单状态的函数
		searchOrderStatus(ref){
			let data = {
				orderId: ref.orderId
			};
			ref.ajax(ref.url.searchOrderStatus, "POST", data, function(resp){
				if(resp.data.result ==2){
					ref.showPopup = false;
					ref.timestamp = null;
					uni.showToast({
						icon: 'success',
						title: '司机已接单'
					});
					uni.vibrateShort({});
					// 接单成功后延迟三秒跳转到司乘同显页面
					setTimeout(function(){
						uni.redirectTo({
							url: '../move/move?orderId=' + ref.orderId
						})
					}, 3000);
				}else if(resp.data.result == 0){
					ref.showPopup = false;
					ref.timestamp = null;
					uni.showToast({
						icon: "success",
						title: "订单已经关闭"
					})
				}
			}, false);
		},
		
		// 封装关闭没有司机接单的订单
		deleteUnAcceptOrder(ref){
			ref.showPopup = false;
			ref.timestamp = null;
			let data = {
				orderId: ref.orderId
			};
			ref.ajax(ref.url.deleteUnAcceptOrder, "POST", data, function(resp){
				let result = resp.data.result;
				if(result == '订单取消成功'){
					uni.showToast({
						icon: 'success',
						title: '订单取消成功'
					});
					// 删除成功后跳回tab页
					setTimeout(function(){
						uni.switchTab({
							url: '../workbench/workbench'
						});
					}, 3000);
				}else{
					ref.searchOrderStatus(ref);
				}
			});
		},
		
		// 编写倒计时组件的两个回调函数
		// 秒数发生变化后执行,每五秒轮询订单状态
		countChangeHandle(s){
			let that = this;
			if(s != 0 && s % 5 == 0){
				that.searchOrderStatus(that);
			}
		},
		
		// 倒计时结束后执行,删除未接单的订单
		countEndHandle: function(){
			let that = this;
			that.deleteUnAcceptOrder(that);
		},
		
		// 手动关闭订单按钮（就是调用删除订单函数）
		cancelHandle(){
			let that = this;
			that.deleteUnAcceptOrder(that);
		}
	},
	onLoad: function(options) {
		let that = this;
		//设置地图控件的高度适配屏幕高度
		let windowHeight = uni.getSystemInfoSync().windowHeight;
		that.windowHeight = windowHeight;
		that.contentStyle = `height:${that.windowHeight}px;`;
		
		// 将from和to取出
		that.from = uni.getStorageSync("from");
		that.to = uni.getStorageSync("to");
		// 创建sdk对象，可以调用腾讯地图的 api
		qqmapsdk = new QQMapWX({
			key: that.tencent.map.key
			
		});
		// 初始化地图，可以操作页面上<map> 组件
		that.map = uni.createMapContext('map');
		
		// TODO 判断options 里面有没有车型和车牌
		// 用户选好车辆从 car_list.vue跳转过来时携带查询参数，才会更新模型层数据
		if(options.hasOwnProperty('showCar')){
			that.showCar = options.showCar;
			that.carId = options.carId;
			that.carPlate = options.carPlate;
			that.carType = options.carType;
		}
		
		// 页面加载时判断路由中是否存在展示倒计时弹窗的变量showPopup
		if(options.hasOwnProperty('showPopup')){
			that.showPopup = true;
			that.timestamp = 60;
			that.orderId = options.orderId;
			// 启动计时器
			that.$refs.uCountDown.start();
		}
		
	},
	onShow: function() {
		let that = this;
		// 页面加载时就计算路线
		that.calculateLine(that);
	}
};
</script>

<style lang="less">
@import url('create_order.less');
</style>
