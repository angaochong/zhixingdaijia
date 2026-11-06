<template>
	<view>
		<view class="face-container">
			<camera device-position="front" flash="off" class="camera" @error="error" v-if="showCamera">
				<cover-image src="../static/face_camera/bg.png" class="bg"></cover-image>
			</camera>
			<view class="image-container" v-if="showImage">
				<image mode="widthFix" class="photo" :src="photoPath"></image>
				<view class="cover"></view>
			</view>
		</view>
		<view class="desc">
			<block v-if="mode == 'verificate'">
				<image src="../static/face_camera/tips.png" mode="widthFix" class="tips"></image>
				<text>请把面部放在圆圈内</text>
				<text>拍摄脸部来确认身份</text>
			</block>
			<block v-if="mode == 'create'">
				<image src="../static/face_camera/face.png" mode="widthFix" class="face"></image>
				<text>请把完整面部放在圆圈内</text>
				<text>拍摄脸部来保存身份识别数据</text>
			</block>
		</view>
		<button class="btn" @tap="confirmHandle">{{ mode == 'create' ? '录入面部信息' : '身份核实' }}</button>
		<u-toast ref="uToast"></u-toast>
	</view>
</template>

<script>
import { pathToBase64 } from 'image-tools';
let dayjs = require('dayjs');
export default {
	data() {
		return {
			mode: 'verificate',
			photoPath: '',
			showCamera: true,
			showImage: false,
			audio: null
		};
	},
	methods: {
		// 通过路由中的 mode 值，来动态执行按钮中的功能
		confirmHandle(){
			let that = this;
			
			that.audio.stop();
			// 创建摄像头实例
			let cameraContext = uni.createCameraContext();
			// 实现拍照功能
			cameraContext.takePhoto({
				quality: "high",
				success(resp) {
					// 获取照片路径并赋值给模型层
					that.photoPath = resp.tempImagePath;
					// 显示照片，隐藏摄像头（通过控制模型层的两个数据的值来动态显示图片/相机拍照按钮）
					that.showCamera = false;
					that.showImage = true;
					//照片转为base64格式字符串
					pathToBase64(that.photoPath).then((base64String) => {
						// base64String 就是照片编码后的字符串
						let url;
						// 通过mode值，动态设置url，来执行对应功能
						if(that.mode === "create"){
							url = that.url.createDriverFaceModel;
						}else{
							url = that.url.verificateDriverFace;
						}
						
						uni.request({
							url: url,
							method: "POST",
							data:{
								photo: base64String
							},
							header: {
								token: uni.getStorageSync("token")
							},
							success(resp){
								if(that.mode === "create"){
									if(resp.data.error){
										that.$refs.uToast.show({
											title: resp.data.error,
											type: "error",
											callback: () => {
												// 重新采集照片
												setTimeout(function(){
													that.showCamera = true;
													that.showImage = false;
												}, 1500);
											}
										});
										// 后端返回的正好是boolean
									}else if(resp.data.result){
										that.$refs.uToast.show({
											title: "人脸采集成功",
											type: "success",
											callback(){
												// 跳转到工作台页面(tab)
												uni.switchTab({
													url: '../../pages/workbench/workbench'
												})
											}
										})
										// 司机人脸采集失败
									}else{
										that.$refs.uToast.show({
											title: resp.data.error,
											type: "error",
											callback: () => {
												// 重新采集照片
												setTimeout(function(){
													that.showCamera = true;
													that.showImage = false;
												}, 1500);
											}
										});
									}
								}else{
									// TOTO 人脸核验业务
								}
							},
							fail: (error) => {
								// 网络异常
								that.$refs.uToast.show({
									title: "网络异常",
									type: "error",
									callback: ()=>{
										setTimeout(function(){
											that.showCamera = true;
											that.showImage = false;
										}, 1500);
									}
								});
							}
						})
					})
				}
			})
		}
	},
	onLoad: function(options) {
		let that = this;
		// 接收路由中的mode赋值到当前模型层mode
		that.mode = options.mode;
		// 创建音乐播放器对象
		let audio = uni.createInnerAudioContext();
		// 赋值给模型层audio
		that.audio = audio;
		audio.src = '../../static/voice/voice_5.mp3';
		audio.play();
	},
	onHide:function(){
		let that = this;
		
		// 这是页面离开/隐藏 时执行的生命周期函数
		// 如果audio 还不为空，那就停止播放
		if(that.audio != null){
			that.audio.stop();
		}
	}
};
</script>

<style lang="less">
@import url('face_camera.less');
</style>
