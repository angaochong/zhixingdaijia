<template>
	<view class="page">
		<view class="notice">
			<u-icon name="info-circle-fill" color="#AC9146" size="30" top="3" />
			<text>准确填写个人信息，可享受每份代驾订单人身意外险</text>
		</view>
		<view class="credentials-container">
			<view class="credentials" v-for="(item, index) in 6" :key="index">
				<image :src="cardBackground[index]" class="bg"></image>
				<view class="cover">
					<image src="../static/filling/card.png" mode="widthFix" class="card"></image>
					<text class="desc">{{ ['身份证正面','身份证背面','手持身份证','驾驶证正面','驾驶证副本','手持驾驶证'][index] }}</text>
					<button class="camera" @tap="index===0 ? scanIdcardFront() : index===1 ? scanIdcardBack() : index===2 ? takePhoto('idcardHolding') : index===3 ? scanDrcardFront() : index===4 ? takePhoto('drcardBack') : takePhoto('drcardHolding')"></button>
				</view>
			</view>
		</view>

		<view class="title">个人信息</view>
		<view class="list">
			<u-cell-group border="false">
				<u-cell-item title="真实姓名" :value="idcard.name" :value-style="style" :arrow="false" />
				<u-cell-item title="性别" :value="idcard.sex" :value-style="style" :arrow="false" />
				<u-cell-item title="生日" :value="idcard.birthday" :value-style="style" :arrow="false" />
				<u-cell-item title="身份证号" :value="idcard.pid" :value-style="style" :arrow="false" />
				<u-cell-item title="身份证地址" :value="idcard.shortAddress" :value-style="style" @click="showAddressContent" />
				<u-cell-item title="身份证有效期" :value="idcard.expiration" :value-style="style" :arrow="false" />
			</u-cell-group>
		</view>
		<view class="title">联系方式</view>
		<view class="list">
			<u-cell-group border="false">
				<u-cell-item title="手机号码" :value="contact.tel" :value-style="style" @click="enterContent('手机号码', 'tel')" />
				<u-cell-item title="电子信箱" :value="contact.shortEmail" :value-style="style" @click="enterContent('电子信箱', 'email')" />
				<u-cell-item title="收信地址" :value="contact.shortMailAddress" :value-style="style" @click="enterContent('收信地址', 'mailAddress')" />
				<u-cell-item title="紧急联系人" :value="contact.contactName" :value-style="style" @click="enterContent('紧急联系人', 'contactName')" />
				<u-cell-item title="紧急联系人电话" :value="contact.contactTel" :value-style="style" @click="enterContent('紧急联系人电话', 'contactTel')" />
			</u-cell-group>
		</view>
		<view class="title">驾驶证</view>
		<view class="list">
			<u-cell-group border="false">
				<u-cell-item title="驾驶证类型" :value="drcard.carClass" :value-style="style" :arrow="false" />
				<u-cell-item title="驾驶证有效期" :value="drcard.validTo" :value-style="style" :arrow="false" />
				<u-cell-item title="初次领证日期" :value="drcard.issueDate" :value-style="style" :arrow="false" />
			</u-cell-group>
		</view>

		<button class="btn" @tap="save" :disabled="realAuth == 3">保存信息</button>
		<view class="remark">您每次提交实名信息之后，都需要工作人员严格审查，请等候1~3天，这期间您将无法接单，特此声明！</view>
		<u-toast ref="uToast" />
	</view>
</template>

<script>
export default {
	data() {
		return {
			mode: 'fill',
			style: { color: '#FF9900' },
			cardBackground: new Array(6).fill('../static/filling/credentials-bg.jpg'),
			idcard: {
				pid: '', 
				name: '', 
				sex: '', 
				address: '', 
				shortAddress: '',
				birthday: '', 
				expiration: '', 
				idcardFront: '', 
				idcardBack: '', 
				idcardHolding: ''
			},
			contact: {
				tel: '', 
				email: '', 
				shortEmail: '',
				mailAddress: '', 
				shortMailAddress: '',
				contactName: '', 
				contactTel: ''
			},
			drcard: {
				issueDate: '', 
				carClass: '', 
				validFrom: '', 
				validTo: '',
				drcardFront: '', 
				drcardBack: '', 
				drcardHolding: ''
			},
			realAuth: uni.getStorageSync('realAuth'),
		};
	},
	methods: {
		scanIdcardFront(){
			const that = this;
			
			uni.chooseImage({
				count: 1,
				success(chooseImageResp){
					// 图片在小程序端的地址
					let tempFilePath = chooseImageResp.tempFilePaths[0];
					uni.showLoading({
						title: "识别中"
					});
					uni.uploadFile({
						url: that.url.recognizeAndUpload,
						filePath: tempFilePath,
						name: "file",
						formData: {
							ocrType: 1,
							module: "driverAuth"
						},
						success: (uploadResp) => {
							uni.hideLoading();
							// 即使后端返回的是 json，这里也是字符串形式
							let json = JSON.parse(uploadResp.data);
							console.log(json.idCardFrontInfo);
							console.log("aaaa")
							
							that.idcard.pid = json.idCardFrontInfo.idNumber;
							that.idcard.name = json.idCardFrontInfo.name;
							that.idcard.sex = json.idCardFrontInfo.gender;
							that.idcard.address = json.idCardFrontInfo.address;
							that.idcard.shortAddress = json.idCardFrontInfo.address.substring(0, 10);
							that.idcard.birthday = json.idCardFrontInfo.birth;
							// 文件在cos存储桶的地址
							that.idcard.idcardFront = json.filePath;
							// 后端返回临时5分钟有效的tempUrl, 传递给这六个背景卡片数组[0]位置
							// 这是响应式的存放
							that.$set(that.cardBackground, 0, json.tempUrl);
							
							uni.showToast({
								title: "识别成功",
								type: "success"
							});
						}
					})
				}
			})
		},
		
		scanIdcardBack(){
			let that = this;
			
			uni.chooseImage({
				count: 1,
				//这个是选择图片成功的回调
				success: (chooseImgResp) => {
					uni.showLoading({
						tutle: "识别中"
					});
					// 上传文件发送请求
					uni.uploadFile({
						url: that.url.recognizeAndUpload,
						// 这是选择图片后在小程序中的图片文件
						filePath: chooseImgResp.tempFilePaths[0],
						name: "file",
						formData: {
							ocrType: 2,
							module: "driverAuth"
						},
						// 请求发送成功的回调
						success: (uploadResp) => {
							uni.hideLoading();
							
							let json = JSON.parse(uploadResp.data);
							console.log(json);
							
							that.idcard.expiration = json.idCardBackInfo.validPeriod;
							that.idcard.idcardBack = json.filePath;
							that.$set(that.cardBackground, 1, json.tempUrl);
							
							uni.show({
								title: "识别成功",
								type: "success"
							})
						}
					})
				}
			})
		},
		
		// 普通图片上传
		takePhoto(flag){
			let that = this;
			
			uni.chooseImage({
				count: 1,
				success: (chooseImgResp) => {
					uni.showLoading({
						title: "正在上传"
					});
					uni.uploadFile({
						url: that.url.recognizeAndUpload,
						filePath: chooseImgResp.tempFilePaths[0],
						name: "file",
						formData: {
							module: "driverAuth"
						},
						success: (uploadResp) => {
							uni.hideLoading();
							let json = JSON.parse(uploadResp.data);
							console.log(json);
							if(flag === "idcardHolding"){
								that.idcard.idcardHolding = json.filePath;
								that.$set(that.cardBackground, 2, json.tempUrl);
							}else if(flag === "drcardBack"){ // 驾驶证副本
									that.drcard.drcardBack = json.filePath;
									that.$set(that.cardBackground, 4, json.tempUrl);	
							}else if(flag === "drcardHolding"){
								that.drcard.drcardHolding = json.filePath;
								that.$set(that.cardBackground, 5, json.tempUrl);	
							}
							
							uni.showToast({
								title: "上传成功",
								type: "success"
							});
						}
					});
				}
			})
		},
		
		// 识别驾驶证正面
		scanDrcardFront(){
			const that = this;
			
			uni.chooseImage({
				count: 1,
				success: (chooseImgResp) => {
					uni.showLoading({
						title: "识别中"
					});
					uni.uploadFile({
						url: that.url.recognizeAndUpload,
						filePath: chooseImgResp.tempFilePaths[0],
						name: "file",
						formData: {
							ocrType: 3,
							module: "driverAuth"
						},
						success(uploadResp){
							uni.hideLoading();
							
							const json = JSON.parse(uploadResp.data);
							that.drcard.issueDate = json.drcardFrontInfo.issueDate;
							that.drcard.carClass = json.drcardFrontInfo.carClass;
							that.drcard.validFrom = json.drcardFrontInfo.validFrom;
							that.drcard.validTo = json.drcardFrontInfo.validTo;
							
							that.drcard.drcardFront = json.filePath;
							
							that.$set(that.cardBackground, 3, json.tempUrl);
							
							uni.showToast({
								title: "上传成功",
								type: "success"
							});
						}
					})
				}
			})
		},
		
		enterContent(title, key){
			let that = this;
			
			// 点击文本框展示模态窗口
			uni.showModal({
				title: title,
				editable: true,
				// 获取弹窗文本框中的初始值
				content: that.contact[key],
				success(resp) {
					if(resp.confirm){
						if(key == 'mailAddress'){
							that.contact['shortMailAddress'] = resp.content.substr(0, 15) + (resp.content.length > 15 ? '...' : '');
						}else if(key == 'email'){
							that.contact['shortEmail'] = resp.content.substr(0, 25) + (resp.content.length > 25 ? '...' : '');
						}
						// 把弹窗中文本框中的值赋值给模型层数据
						that.contact[key] = resp.content;
						console.log(that.contact[key])
					}
				}
			})
		},
		
		save(){
			let that = this;
			
			let idcardFront = that.idcard.idcardFront;
			let idcardBack = that.idcard.idcardBack;
			let idcardHolding = that.idcard.idcardHolding;
			let drcardFront = that.drcard.drcardFront;
			let drcardBack = that.drcard.drcardBack;
			let drcardHolding = that.drcard.drcardHolding;
			if(idcardFront == "" || idcardBack == "" 
			|| idcardHolding == "" || drcardFront == "" 
			|| drcardBack == "" || drcardHolding == ""){
				that.$refs.uToast.show({
					title: '证件上传不完整',
					type: 'error'
				});
			}else if(
				//执行前端验证
				that.checkValidTel(that.contact.tel, '手机号码') &&
				that.checkValidEmail(that.contact.email, '电子信箱') &&
				that.checkValidAddress(that.contact.mailAddress, '收信地址') &&
				that.checkValidName(that.contact.contactName, '联系人') &&
				that.checkValidTel(that.contact.contactTel, '联系人电话')){
					
				// 全部校验通过后，发送请求，保存实名信息
				uni.showModal({
					title: '提示信息',
					content: '确认提交实名资料',
					success(resp) {
						if(resp.confirm){
							
							let data = {
								pid: that.idcard.pid,
								name: that.idcard.name,
								sex: that.idcard.sex,
								birthday: that.idcard.birthday,
								tel: that.contact.tel,
								email: that.contact.email,
								mailAddress: that.contact.mailAddress,
								contactName: that.contact.contactName,
								contactTel: that.contact.contactTel,
								idcardAddress: that.idcard.address,
								idcardFront: idcardFront,
								idcardBack: idcardBack,
								idcardHolding: idcardHolding,
								idcardExpiration: that.idcard.expiration,
								drcardType: that.drcard.carClass,
								drcardExpiration: that.drcard.validTo,
								drcardIssueDate: that.drcard.issueDate,
								drcardFront: drcardFront,
								drcardBack: drcardBack,
								drcardHolding: drcardHolding
							};
							
							// 发送请求
							uni.request({
								url: that.url.updateDriverAuth,
								method: "POST",
								data: data,
								header: {
									token: uni.getStorageSync("token")
								},
								success(resp) {
									if(resp.data.error){
										that.$refs.uToast.show({
											title: resp.data.error,
											type: "error"
										});
									}else{
										let rows = resp.data.rows;
										if(rows == 1){
											// 如果提交成功就色湖之认证状态为审核中
											uni.setStorageSync("realAuth", 3);
											// 模型层数据改为3后，保存信息按钮就失效了
											that.realAuth = 3;
											
											that.$refs.uToast.show({
												title: "提交成功",
												type: "success",
												callback: () => {
													// mode是上个路由跳转过来的参数，如果是create就证明是新司机
													// 新司机和老司机跳转的页面是不一样的
													if(that.mode == "create"){
														uni.redirectTo({
															url: '../face_camera/face_camera'
															
														})
													}else{
														// 老司机要跳转到工作台页面（tab页面）
														uni.switchTab({
															url: '../../pages/workbench/workbench'
														})
													}
												}
											});
										}else{
											that.$refs.uToast.show({
												title: "提交实名资料失败",
												type: "error"
											});
										}
									}
								},
								fail(error) {
									that.$refs.uToast.show({
										title: "网络异常",
										type: "error"
									});
								}
							})
						}
					}
				})
			}
		},
		
		showAddressContent(){
			let that = this;
			
			if(that.idcard.address.length > 0){
				uni.showModal({
					title: "身份证地址",
					content: that.idcard.address,
					showCancel: false
				});
			}
		}
		
		
	},
	onLoad: function(options) {
		let that = this;
		that.mode = options.mode;
		
		if(uni.getStorageSync('realAuth') == 1){
			uni.showModal({
				title: '提示信息',
				content: '新注册的代驾司机请填写实名认证信息，并且上传相关证件照片',
				showCancel: false
			});
		}else{
			// realAuth = 3，保存信息按钮时灰色的
			that.ajax(that.url.searchDriverAuth, 'GET', null, function(resp){
				let json = resp.data.result;
				that.idcard.pid = json.pid;
			    that.idcard.name = json.name;
			  that.idcard.sex = json.sex;
			  that.idcard.birthday = json.birthday;
			  that.idcard.address = json.idcardAddress;
			  that.idcard.shortAddress = json.idcardAddress.substr(0, 15) + (json.idcardAddress.length > 15 ? '...' : '');
			  that.idcard.expiration = json.idcardExpiration;
			  that.idcard.idcardFront = json.idcardFront;
			  if (json.idcardFrontUrl.length > 0) {
				that.cardBackground[0] = json.idcardFrontUrl;
			  }
			  that.idcard.idcardBack = json.idcardBack;
			  if (json.idcardBackUrl.length > 0) {
				that.cardBackground[1] = json.idcardBackUrl;
			  }
			  that.idcard.idcardHolding = json.idcardHolding;
			  if (json.idcardHoldingUrl.length > 0) {
				that.cardBackground[2] = json.idcardHoldingUrl;
			  }
			  that.contact.tel = json.tel;
			  that.contact.email = json.email;
			  that.contact.shortEmail = json.email.substr(0, 25) + (json.email.length > 25 ? '...' : '');
			  that.contact.mailAddress = json.mailAddress;
			  that.contact.shortMailAddress = json.mailAddress.substr(0, 15) + (json.mailAddress.length > 15 ? '...' : '');
			  that.contact.contactName = json.contactName;
			  that.contact.contactTel = json.contactTel;
			  that.drcard.carClass = json.drcardType;
			  that.drcard.validTo = json.drcardExpiration;
			  that.drcard.issueDate = json.drcardIssueDate;
			  that.drcard.drcardFront = json.drcardFront;
			  if (json.drcardFrontUrl.length > 0) {
				that.cardBackground[3] = json.drcardFrontUrl;
			  }
			  that.drcard.drcardBack = json.drcardBack;
			  if (json.drcardBackUrl.length > 0) {
				that.cardBackground[4] = json.drcardBackUrl;
			  }
			  that.drcard.drcardHolding = json.drcardHolding;
			  if (json.drcardHoldingUrl.length > 0) {
				that.cardBackground[5] = json.drcardHoldingUrl;
			  }
			});
		}
	}
};
</script>

<style lang="less">
@import url('filling.less');
</style>