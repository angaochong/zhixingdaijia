<template>
	<view>
		<map
			id="map"
			scale="15"
			:longitude="longitude"
			:latitude="latitude"
			:enable-poi="true"
			class="map"
			:style="mapStyle"
			:polyline="polyline"
			:markers="markers"
			@longpress="showHandle"
		>
			<!-- <image class="location" src="../static/workbench/location.png" @tap="returnLocationHandle()" /> -->
		</map>
		<view class="panel" v-show="infoStatus">
			<view class="info">
				<view class="label">剩余</view>
				<view class="value">{{ distance }}公里</view>
				<view class="label">，预计</view>
				<view class="value">{{ duration }}分钟</view>
				<view class="more" @tap="moreHandle">订单详情</view>
			</view>
			<view class="opt">
				<button class="cancel-btn" :style="cancelStyle">取消订单</button>
				<!-- 订单状态是已接单或到达待机点可以点击 -->
				<button class="confirm-btn" v-if="status == 2||status == 3" @tap="driverArriviedHandle">司机到达</button>
			</view>
		</view>
	</view>
</template>

<script>
let QQMapWX = require('../../lib/qqmap-wx-jssdk.min.js');
let qqmapsdk;
export default {
	data() {
		return {
			orderId: null,
			status: null,
			mode: null,
			cancelStyle: '',
			map: null,
			mapStyle: '',
			startLatitude: 0,
			startLongitude: 0,
			endLatitude: 0,
			endLongitude: 0,
			latitude: 0,
			longitude: 0,
			targetLatitude: 0,
			targetLongitude: 0,
			distance: 0,
			duration: 0,
			polyline: [],
			markers: [],
			timer: null,
			infoStatus: true,
			messageTimer: null
		};
	},
	methods: {
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
		
		calculateLine() {
		    let that = this;
		    if (that.latitude == 0 || that.longitude == 0) {
		        return;
		    }
		    qqmapsdk.direction({
		        mode: that.mode,
		        from: {
		            latitude: that.latitude,
		            longitude: that.longitude
		        },
				// 当前行驶路线的终点
		        to: {
		            latitude: that.targetLatitude,
		            longitude: that.targetLongitude
		        },
		        success: function(resp) {
		            let route = resp.result.routes[0];
		            let distance = route.distance;
		            let duration = route.duration;
		            let polyline = route.polyline;
		            that.distance = Math.ceil((distance / 1000) * 10) / 10;
		            that.duration = duration;
		
		            let points = that.formatPolyline(polyline);
		
		            that.polyline = [
		                {
		                    points: points,
		                    width: 6,
		                    color: '#05B473',
		                    arrowLine: true
		                }
		            ];
		            that.markers = [
		                {
		                    id: 1,
		                    latitude: that.latitude,
		                    longitude: that.longitude,
		                    width: 35,
		                    height: 35,
		                    anchor: {
		                        x: 0.5,
		                        y: 0.5
		                    },
		                    iconPath: '../../static/move/driver-icon.png'
		                }
		            ];
		        },
		        fail: function(error) {
		            console.log(error);
		        }
		    });
		},
		
		// 封装根据当前订单状态分析函数，计算最佳路线
		analyse(){
			let that = this;
			// 如果是已结单的状态，证明是第一阶段：司机赶往代驾点
			if(that.status == 2){
				let data = {
					orderId: that.orderId
				};
				that.ajax(that.url.searchOrderLocationCache, "POST", data, function(resp){
					let result = resp.data.result;
					if(result.hasOwnProperty('latitude') && result.hasOwnProperty('longitude')){
						// 把司机的定位作为当前行驶路线的起点
						that.latitude = result.latitude;
						that.longitude = result.longitude;
						// 重新计算最佳路线
						that.calculateLine();
					}
				}, false)
			}else{
				// 否则的话就是第二阶段：订单的起点到订单的终点
				that.calculateLine();
			}
		},
		
		// 乘客点击司机到达
		driverArriviedHandle(){
			let that = this;
			uni.showModal({
				title: '提示消息',
				content: '确定司机已经到达代驾点',
				success: function(resp){
					if(resp.confirm){
						let data = {
							orderId: that.orderId
						};
						that.ajax(that.url.confirmArriveStartPlace, "POST", data, function(resp){
							if(resp.data.result){
								uni.showToast({
									icon: 'success',
									title: '状态更新成功'
								});
								that.status = 4;
								that.mode = 'driving';
								that.targetLatitude = that.endLatitude;
								that.targetLongitude = that.endLongitude;
							}
						})
					}
				}
			})
		},
		
	},
	onLoad: function(options) {
		let that = this;
		that.orderId = options.orderId;
		qqmapsdk = new QQMapWX({
			key: that.tencent.map.key
		});
		let windowHeight = uni.getSystemInfoSync().windowHeight;
		that.mapStyle = `height:${windowHeight}px`;
	},
	onShow: function() {
		let that = this;
		// 在这里根据订单的状态设置代驾的起点和终点坐标
		// 这里排除 status = 2 ，因为这是第一阶段，analyse()函数中判断过了
		uni.$on('updateLocation', function(location){
			if(location != null && that.status != 2){
				// 不是第一阶段时，起点就用乘客自己的坐标
				that.latitude = location.latitude;
				that.longitude = location.longitude;
			}
		});
		
		let data = {
			orderId: that.orderId
		};
		
		that.ajax(that.url.searchOrderForMoveById, "POST", data, function(resp){
			let result = resp.data.result;
			// 这里是设置订单的起点坐标 和 终点坐标
			that.status = result.status;
			let startPlaceLocation = JSON.parse(result.startPlaceLocation);
			that.startLatitude = startPlaceLocation.latitude;
			that.startLongitude = startPlaceLocation.longitude;
			let endPlaceLocation = JSON.parse(result.endPlaceLocation);
			that.endLatitude = endPlaceLocation.latitude;
			that.endLongitude = endPlaceLocation.longitude;
			
			
			let status = result.status;
			// 如果status状态是 已结单，司机已到达，当前行驶路线的终点是订单的起点
			if(status == 2 || status == 3){
				that.targetLatitude = that.startLatitude;
				that.targetLongitude = that.startLongitude;
				that.mode = 'bicycling';
			}else if(status == 4){
				// 如果状态为开始代驾，当前行驶路线的终点是订单的终点
				that.targetLatitude = that.endLatitude;
				that.targetLongitude = that.endLongitude;
				that.mode = 'driving';
			}
			// 计算最佳路线
			that.analyse();
			// 定时器六秒计算一次最佳路线
			that.timer = setInterval(function(){
				that.analyse();
			}, 6000);
			
			if(status == 4 || status == 5 || status == 6){
				that.messageTimer = setInterval(function(){
					that.ajax(that.url.receiveBillMessage, "POST", {}, function(resp){
						if(resp.data.result == "您有代驾订单待支付"){
							uni.redirectTo({
								url: `../order/order?orderId=${that.orderId}`
							})
						}
					}, false);
				});
			}
		}, 5000);
		
	},
	onHide: function() {
		let that =  this;
		uni.$off('updateLocation');
		clearInterval(that.timer);
		that.timer = null;
		clearInterval(that.messageTimer);
		that.messageTimer = null;
	}
};
</script>

<style lang="less">
@import url('move.less');
</style>
