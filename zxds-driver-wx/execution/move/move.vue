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
			<!-- <cover-image class="location" src="../static/workbench/location.png" @tap="returnLocationHandle()" /> -->
			<cover-view class="information-container" v-show="infoStatus">
				<cover-image src="../static/move/close-icon.png" class="close-icon" @tap="hideHandle"></cover-image>
				<cover-view class="info">
					<cover-view class="label">剩余</cover-view>
					<cover-view class="value">{{ distance }}公里</cover-view>
					<cover-view class="label">，预计</cover-view>
					<cover-view class="value">{{ duration }}分钟</cover-view>
				</cover-view>
			</cover-view>
		</map>
	</view>
</template>

<script>
let QQMapWX = require('../../lib/qqmap-wx-jssdk.min.js');
let qqmapsdk;
export default {
	data() {
	    return {
	        orderId: null,
	        status: null, // 订单状态：如果是2，则司机当前行进的终点是代驾的起点，如果是3，则司机当前行进的终点是代驾的终点。
	        mode: null, // 出行模式
	        map: null, // 存放地图上下文对象
	        mapStyle: '',
	      
	        startLatitude: 0, // 代驾的起点坐标
	        startLongitude: 0,
	      
	        endLatitude: 0, // 代驾的终点坐标
	        endLongitude: 0,
	      
	        latitude: 0, // 当前司机的定位
	        longitude: 0,
	      
	        targetLatitude: 0, // 当前司机要行进的终点坐标
	        targetLongitude: 0,
	      
	        distance: 0, // 当前司机到当前行进的终点的距离
	        duration: 0, // 当前司机到当前行进的终点的耗时
	        polyline: [], // 用来存放GPS坐标点
	        markers: [], // 用来存放地图路线上显示的图表
	        timer: null, // 定时器
	        infoStatus: true
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
		    //如果还没有获得到司机的定位，就不计算最佳线路
		    if (that.latitude == 0 || that.longitude == 0) {
		        return;
		    }
		    qqmapsdk.direction({
		        mode: that.mode, 
		        from: {
		            latitude: that.latitude,
		            longitude: that.longitude
		        },
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
		                    iconPath: '../static/move/driver-icon.png'
		                }
		            ];
		        },
		        fail: function(error) {
		            console.log(error);
		        }
		    });
		},
		
		hideHandle() {
		    this.infoStatus = false;
		},
		showHandle() {
		    this.infoStatus = true;
		}
	},
	onLoad: function(options) {
		let that = this;
		that.orderId = options.orderId;
		// 创建腾讯地图服务对象
		qqmapsdk = new QQMapWX({
			key: that.tencent.map.key
		});
		let windowHeight = uni.getSystemInfoSync().windowHeight;
		that.mapStyle = `height:${windowHeight}px`;
	},
	onShow: function() {
		let that = this;
		
		// 实时获取定位
		uni.$on('updateLocation', function(location){
			if(location != null){
				that.latitude = location.latitude;
				that.longitude = location.longitude;
			}
		});
		
		let data = {
			orderId: that.orderId
		};
		
		that.ajax(that.url.searchOrderForMoveById, "POST", data, function(resp){
			let result = resp.data.result;
			console.log(result);
			let startPlaceLocation = JSON.parse(result.startPlaceLocation);
			that.startLatitude = startPlaceLocation.latitude;
			that.startLongitude = startPlaceLocation.longitude;
			
			let endPlaceLocation = JSON.parse(result.endPlaceLocation);
			that.endLatitude = endPlaceLocation.latitude;
			that.endLongitude = endPlaceLocation.longitude;
			
			let status = result.status;
			// 已接单状态
			if(status == 2){
				// 当前的终点是定位到上车点
				that.targetLatitude = that.startLatitude;
				that.targetLongitude = that.startLongitude;
				that.mode = 'bicycling';
			}else if(status == 3 || status == 4){
				// 司机已到达或开始代驾
				// 当前的终点是订单的终点
				that.targetLatitude = that.endLatitude;
				that.targetLongitude = that.endLongitude;
				that.mode = 'driving';
			}
			// 计算最佳路线
			that.calculateLine();
			// 每六秒计算当前定位的最佳路线，距离，时间
			that.timer = setInterval(function(){
				that.calculateLine();
			}, 6000);
		})
	},
	onHide: function() {
		// 解绑定位更新
		let that = this;
		uni.$off('updateLocation');
		clearInterval(that.timer);
		that.timer = null;
	},
};
</script>

<style lang="less">
@import url('move.less');
</style>
