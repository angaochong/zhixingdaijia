<script>
export default {
	onLaunch: function() {
		// 用来缓存GPS定位，达到20个之后向服务器发送一次ajax
		let gps = [];
		
		// 设置屏幕常亮
		uni.setKeepScreenOn({
			keepScreenOn: true
		});
		
		// 开启GPS后台刷新，和绑定事件，这两个是必备的
		uni.startLocationUpdate({
			success(resp){
				console.log("开启定位成功");
			},
			fail(resp){
				console.log("开启定位失败");
			}
		});
		
		// 绑定事件GPS发生变化就发送请求把坐标提交给后端
		wx.onLocationChange(function(resp){
			let latitude = resp.latitude;
			let longitude = resp.longitude;
			// 速度（单位：m/s）后面用
			let speed = resp.speed;
			let location = {latitude: latitude, longitude: longitude};
			
			// 发送请求要根据司机的工作状态
			let workStatus = uni.getStorageSync("workStatus");
			
			// 上传司机的GPS定位信息给后端
			let baseUrl = 'http://192.168.43.5:8201/zxds-driver';
			if(workStatus == '开始接单'){
				// 只在每分钟的前10秒上报定位信息，减小服务器压力
				/* let current = new Date();
				if(current.getSeconds() > 10){
					return;
				} */
				
				// 也要提交司机的设置信息
				let settings = uni.getStorageSync("settings");
				// 这里也先写死
				settings = {
					orderDistance: 0,	// 司机不挑单
					rangeDistance: 5,	// 司机只接5公里以内的单子
					orientation: ''		// 司机不设置定向接单
				};
				let orderDistance = settings.orderDistance;
				let rangeDistance = settings.rangeDistance;
				let orientation = settings.orientation;
				
				// 这六个属性获取到，就可以发送ajax
				uni.request({
					url: `${baseUrl}/driver/location/updateLocationCache`,
					method: 'POST',
					header: {
						token: uni.getStorageSync("token")
					},
					data: {
						latitude: latitude,
						longitude: longitude,
						orderDistance: orderDistance,
						rangeDistance: rangeDistance,
						orientateLongitude: orientation != '' ? orientation.longitude : null,
						orientateLatitude: orientation != '' ? orientation.latitude : null
					},
					success(resp){
						if(resp.statusCode == 401){
							uni.redirectTo({
								url: '/pages/login/login'
							})
						}else if(resp.statusCode == 200 && resp.data.code == 200){
							let data = resp.data;
							if(data.hasOwnProperty('token')){
								let token = data.token;
								uni.setStorageSync('token', token);
							}
							console.log("更新定位成功");
						}else{
							console.log("更新GPS定位信息失败", resp.data);
						}
					},
					fail(error){
						console.log("更新GPS定位信息失败", error);
					}
				});
			}else if(workStatus = "接客户"){
				let executeOrder = uni.getStorageSync('executeOrder');
				let orderId = executeOrder.id;
				let data = {
					orderId: orderId,
					latitude: latitude,
					longitude: longitude
				};
				uni.request({
					url: `${baseUrl}/driver/location/updateOrderLocationCache`,
					method: "POST",
					header: {
						token: uni.getStorageSync('token')
					},
					data: data,
					success(resp) {
						if(resp.statusCode == 401){
							uni.redirectTo({
								url: 'pages/login/login'
							});
						}else if(resp.statusCode == 200 && resp.data.code == 200){
							let data = resp.data;
							if(data.hasOwnProperty('token')){
								let token = data.token;
								uni.setStorageSync('token', token);
							}
							console.log('订单定位更新成功');
						}else{
							console.log('订单定位更新失败');
						}
					},
					fail(error) {
						console.log('订单定位更新失败', error);
					}
				});
			}else if(workStatus == "开始代驾"){
				// 每凑够20个定位就上传一次，减小服务器压力
				if(gps.length == 20){
					uni.request({
						url: `${baseUrl}/order/gps/insertOrderGps`,
						method: "POST",
						header: {
							token: uni.getStorageSync("token")
						},
						data: {
							list: gps
						},
						success: function(resp){
							if(resp.statusCode == 401){
								uni.redirectTo({
									url: 'pages/login/login'
								});
							}else if(resp.statusCode == 200 && resp.data.code == 200){
								let data = resp.data;
								console.log("上传GPS成功");
							}else{
								console.log("保存GPS定位失败", resp.data);
							}
							gps.length = 0;
						},
						fail: function(error){
							console.error('保存GPS定位失败', error);
						}
					});
				}
			}
			
			// 触发自定义事件（通知应用内的其他组件位置变化了）
			uni.$emit('updateLocation', location);
		});
	},
	onShow: function() {
		console.log('App Show');
	},
	onHide: function() {
		console.log('App Hide');
	}
};
</script>

<style lang="scss">
@import 'uview-ui/index.scss';
</style>
