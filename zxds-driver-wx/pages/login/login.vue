<template>
	<view>
		<image src="../../static/login/top.png" mode="widthFix" class="top"></image>
		<image src="../../static/login/logo.png" mode="widthFix" class="logo"></image>
		<view class="desc">
			<text class="name">（司机端）</text>
			<text class="version">Ver20XX.201</text>
		</view>
		<button class="btn" @tap="login()">微信登陆</button>
		<view class="register-container">
			没有账号?
			<text class="link" @tap="toRegisterPage()">立即注册</text>
		</view>
		<text class="remark">本小程序仅限于智行代驾签约司机使用，新司机必须先注册，经由实名认证之后方可在本小程序中接单运营。</text>
		<u-toast ref="uToast" />
	</view>
</template>

<script>
export default {
	data() {
		return {};
	},
	methods: {
		toRegisterPage: function(){
			uni.navigateTo({
				url: "../register/register"
			});
		},
		
		login(){
			let that = this;
			
			uni.login({
				provider: "weixin",
				success(resp) {
					console.log("临时授权码：", resp.code);
					uni.request({
						url: that.url.login,
						method: "POST",
						data: {
							code: resp.code,
						},
						success(resp) {
							if(resp.data.error){
								that.$refs.uToast.show({
									title: resp.data.error,
									type: "error"
								});
							}else{
								let result = resp.data.result;
								if(result == null || !result.hasOwnProperty("token")){
									that.$refs.uToast.show({
										title: "请先注册",
										type: "error",
										callback: () => {
											//跳转到注册页面
											uni.navigateTo({
												url: '../register/register'
											});
										}
									});
								}else{
									// 司机登录成功
									that.$refs.uToast.show({
										title: "登录成功",
										type: "success",
										callback: () => {
											// 存token
											uni.setStorageSync("token", result.token);
											console.log("【重要】真机调试的token已存入：", result.token);
											// 存认状态
											uni.setStorageSync("realAuth", result.realAuth);
											// 司机登录时必须删除司机正在执行的订单
											uni.removeStorageSync("executeOrder");
											// 司机刚刚登录，存储司机的工作状态为 "停止接单"
											uni.setStorageSync("workStatus", "停止接单");
											// 如果 realAuth == 1 （未认证）则跳转到filling.vue
											if(result.realAuth == 1){
												uni.navigateTo({
													url: '../../identity/filling/filling?mode=create'
												});
											}else if(!result.archive){
												// 如果只是未人脸识别，问一下司机要不要人脸识别
												uni.showModal({
													title: "提示信息",
													content: "提示：司机不进行面部采集，将无法正常接单哦！",
													success(resp) {
														if(resp.confirm){
															uni.navigateTo({
																url: '../../identity/face_camera/face_camera?mode=create'
															});
														}else{
															uni.switchTab({
																url: '../workbench/workbench'
															});
														}
													}
												})
											}else{
												// 实名认证并且人脸识别了，就可以跳转到工作台页面
												uni.switchTab({
													url: '../workbench/workbench'
												});
											}
										}
									})
								}
							}
						}
					})
				},
			})
		}
	},
	onLoad: function(e) {
		
	}
};
</script>

<style lang="less">
@import url('login.less');
</style>
