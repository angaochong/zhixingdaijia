<template>
	<div v-if="isAuth(['ROOT', 'DRIVER:SELECT'])">
		<el-form :inline="true" :model="dataForm" :rules="dataRule" ref="dataForm">
			<el-form-item prop="name"><el-input v-model="dataForm.name" placeholder="姓名" size="medium" class="input" clearable="clearable" /></el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.sex" class="input" placeholder="性别" size="medium" clearable="clearable">
					<el-option label="男" value="男" />
					<el-option label="女" value="女" />
				</el-select>
			</el-form-item>
			<el-form-item prop="tel"><el-input v-model="dataForm.tel" placeholder="电话" size="medium" class="input" clearable="clearable" /></el-form-item>
			<el-form-item prop="pid"><el-input v-model="dataForm.pid" placeholder="身份证" size="medium" clearable="clearable" /></el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.realAuth" class="input" placeholder="实名认证" size="medium" clearable="clearable">
					<el-option label="未认证" value="1" />
					<el-option label="已认证" value="2" />
					<el-option label="审核中" value="3" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.status" class="input" placeholder="状态" size="medium" clearable="clearable">
					<el-option label="正常" value="1" />
					<el-option label="禁用" value="2" />
					<el-option label="减少接单" value="3" />
				</el-select>
			</el-form-item>
			<el-form-item><el-button size="medium" type="primary" @click="searchHandle()">查询</el-button></el-form-item>
		</el-form>
		<el-table
			:data="dataList"
			border
			v-loading="dataListLoading"
			cell-style="padding: 4px 0"
			style="width: 100%;"
			size="medium"
			@expand-change="expand"
			:row-key="getRowKeys"
			:expand-row-keys="expands"
		>
			<el-table-column prop="content" header-align="center" align="center" type="expand">
				<template #default="scope">
					<div class="content-container">
						<div class="left">
							<table class="content-table">
								<tbody>
									<tr>
										<th>开车驾龄</th>
										<th>生日</th>
										<th>邮箱</th>
										<th>收信地址</th>
										<th>身份证住址</th>
									</tr>
									<tr>
										<td>{{ content.year }}年</td>
										<td>{{ content.birthday }}</td>
										<td>{{ content.email }}</td>
										<td>{{ content.mailAddress }}</td>
										<td>{{ content.idcardAddress }}</td>
									</tr>
								</tbody>
							</table>
							<ul class="credentials-list">
								<li class="item">
									<el-image style="width: 350px; height: 220px" :src="content.idcardFront" :preview-src-list="content.idcardFrontList" fit="cover" />
									<p>【 身份证正面 】</p>
								</li>
								<li class="item">
									<el-image style="width: 350px; height: 220px" :src="content.idcardBack" :preview-src-list="content.idcardBackList" fit="cover" />
									<p>【 身份证背面 】</p>
								</li>
								<li class="item">
									<el-image style="width: 350px; height: 220px" :src="content.idcardHolding" :preview-src-list="content.idcardHoldingList" fit="cover" />
									<p>【 手持身份证 】</p>
								</li>
								<li class="item">
									<el-image style="width: 350px; height: 220px" :src="content.drcardFront" :preview-src-list="content.drcardFrontList" fit="cover" />
									<p>【 驾驶证正面 】</p>
								</li>
								<li class="item">
									<el-image style="width: 350px; height: 220px" :src="content.drcardBack" :preview-src-list="content.drcardBackList" fit="cover" />
									<p>【 驾驶证背面 】</p>
								</li>
								<li class="item">
									<el-image style="width: 350px; height: 220px" :src="content.drcardHolding" :preview-src-list="content.drcardHoldingList" fit="cover" />
									<p>【 手持驾驶证 】</p>
								</li>
							</ul>
						</div>
						<div class="right" v-if="scope.row.realAuth == '已认证'">
							<div class="summary-panel">
								<h3 class="title">【 代驾数据摘要 】</h3>
								<ul class="monthly-list">
									<li class="item">
										<p class="section">月订单金额</p>
										<p class="value">
											<span>￥</span>
											{{ content.total }}
										</p>
									</li>
									<li class="item">
										<p class="section">月订单分账</p>
										<p class="value">
											<span>￥</span>
											{{ content.income }}
										</p>
									</li>
									<li class="item">
										<p class="section">代扣个税</p>
										<p class="value">
											<span>￥</span>
											{{ content.tax }}
										</p>
									</li>
								</ul>
								<ul class="statistics-list">
									<li class="item">
										<p class="section">总代驾单数</p>
										<p class="value">{{ content.count }}</p>
									</li>
									<li class="item">
										<p class="section">好评总数</p>
										<p class="value">{{ content.ct1 }}</p>
									</li>
									<li class="item">
										<p class="section">差评总数</p>
										<p class="value">{{ content.ct2 }}</p>
									</li>
									<li class="item">
										<p class="section">差评总数</p>
										<p class="value">{{ content.ct3 }}</p>
									</li>
								</ul>
							</div>
							<div id="chart"></div>
						</div>
					</div>
				</template>
			</el-table-column>
			<el-table-column type="index" header-align="center" align="center" width="100" label="序号">
				<template #default="scope">
					<span>{{ (pageIndex - 1) * pageSize + scope.$index + 1 }}</span>
				</template>
			</el-table-column>
			<el-table-column prop="name" header-align="center" align="center" min-width="100" label="姓名" />
			<el-table-column prop="sex" header-align="center" align="center" min-width="60" label="性别" />
			<el-table-column prop="tel" header-align="center" align="center" min-width="130" label="电话" />
			<el-table-column prop="pid" header-align="center" align="center" min-width="190" label="身份证" />
			<el-table-column prop="contactName" header-align="center" align="center" min-width="130" label="紧急联络人" />
			<el-table-column prop="contactTel" header-align="center" align="center" min-width="130" label="联络人电话" />
			<el-table-column prop="realAuth" header-align="center" align="center" min-width="130" label="实名认证" />
			<el-table-column prop="status" header-align="center" align="center" min-width="100" label="状态" />
			<el-table-column header-align="center" align="center" width="250" label="操作">
				<template #default="scope">
					<el-button type="text" size="medium" v-if="isAuth(['ROOT', 'USER:UPDATE'])" @click="showApproveModel(scope.row.id)" :disabled="scope.row.realAuth != '审核中'">
						审批认证
					</el-button>
					<el-button type="text" size="medium" v-if="isAuth(['ROOT', 'USER:UPDATE'])" @click="showRepealModel(scope.row.id)" :disabled="scope.row.realAuth != '已认证'">
						撤销认证
					</el-button>
					<el-button type="text" size="medium" v-if="isAuth(['ROOT', 'USER:UPDATE'])" @click="showUpdatePanel(scope.row.id)">设置状态</el-button>
				</template>
			</el-table-column>
		</el-table>
		<el-pagination
			@size-change="sizeChangeHandle"
			@current-change="currentChangeHandle"
			:current-page="pageIndex"
			:page-sizes="[10, 20, 50]"
			:page-size="pageSize"
			:total="totalCount"
			layout="total, sizes, prev, pager, next, jumper"
		></el-pagination>
		<el-dialog v-model="approveModelVisible" title="提示信息" width="400px" center>
			<div class="notice">你确定已经核准该代驾司机的实名认证？</div>
			<template #footer>
				<span class="dialog-footer">
					<el-button @click="approveHandle(false)">拒绝</el-button>
                    <el-button type="primary" @click="approveHandle(true)">批准</el-button>
				</span>
			</template>
		</el-dialog>
		<el-dialog v-model="repealModelVisible" title="提示信息" width="400px" center>
			<div class="notice">你确定取消该代驾司机的实名认证？</div>
			<template #footer>
				<span class="dialog-footer">
					<el-button @click="repealModelVisible = false">取消</el-button>
					<el-button type="primary" @click="repealHandle">确定</el-button>
				</span>
			</template>
		</el-dialog>
		<update ref="update" @refreshDataList="loadDataList"></update>
	</div>
</template>
<script>
import $ from 'jquery';
import Update from './driver-update.vue';
import axios from 'axios';
export default {
	components: {
		Update
	},
	data() {
		return {
			dataForm: {
				name: null,
				sex: null,
				tel: null,
				pid: null,
				realAuth: null,
				status: null
			},
			chart: '',
			dataList: [],
			pageIndex: 1,
			pageSize: 10,
			totalCount: 0,
			dataListLoading: false,
			dataRule: {
				name: [{ required: false, pattern: '^[\u4e00-\u9fa5]{1,10}$', message: '姓名格式错误' }],
				tel: [{ required: false, pattern: '^1\\d{10}$', message: '电话格式错误' }],
				pid: [{ required: false, pattern: '^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$', message: '身份证格式错误' }]
			},
			expands: [],
			content: {
				year: null,
				birthday: null,
				email: null,
				mailAddress: null,
				idcardAddress: null,
				idcardFront: null,
				idcardFrontList: [],
				idcardBack: null,
				idcardBackList: [],
				idcardHolding: null,
				idcardHoldingList: [],
				drcardFront: null,
				drcardFrontList: [],
				drcardBack: null,
				drcardBackList: [],
				drcardHolding: null,
				drcardHoldingList: [],
				total: null,
				income: null,
				tax: null,
				count: null,
				ct1: null,
				ct2: null,
				ct3: null
			},
			approveModelVisible: false,
			repealModelVisible: false,
			driverId: null,
			getRowKeys(row) {
				return row.id;
			}
		};
	},
	methods: {
		async loadDataList(){
			let that = this;
			
			that.dataListLoading = true;
			let data = {
				dataList: [],
				pageIndex: that.pageIndex,
				pageSize: that.pageSize,
				name: that.dataForm.name || null,
				sex: that.dataForm.sex || null,
				tel: that.dataForm.tel || null,
				pid: that.dataForm.pid || null,
				realAuth: that.dataForm.realAuth || null,
				status: that.dataForm.status || null
			};
			
			let response = await axios.post(`${that.$baseUrl}driver/searchDriverByPage`, data, {
				headers: {
					token: localStorage.getItem("token")
				}
			});
			
			let result = response.data.result;
			let list = result.list;
			let statusEnum = {
				'1': '正常',
				'2': '禁用',
				'3': '降低接单'
			};
			let realAuthEnum = {
				'1': '未认证',
				'2': '已认证',
				'3': '审核中'
			};
			for(let one of list){
				one.status = statusEnum[one.status + ""];
				one.realAuth = realAuthEnum[one.realAuth + ""];
			}
			that.dataList = list;
			that.totalCount = result.totalCount;
			that.dataListLoading = false;
		},
		
		sizeChangeHandle(val){
			this.pageSize = val;
			this.pageIndex = 1;
			this.loadDataList();
		},
		
		currentChangeHandle(val){
			// Vue2+ElementUI的一个bug。页面加载时，这个方法会调用一次，并且val是null，因此要不为空的情况下再执行分页查询
			if(val){
				this.pageIndex = val;
				this.loadDataList();
			}
		},
		
		async searchHandle(){
			let valid = await this.$refs.dataForm.validate();
			if(valid){
				this.$refs.dataForm.clearValidate();
				this.pageIndex = 1;
				this.loadDataList();
			}
		},
		
		async expand(row, expandedRows){
			// row: 当前被点击的那一行的完整数据对象
			// expandedRows: 当前表格里所有已展开的行，是一个数组。例如点开2个：[row1, row2]
			let that = this;
			
			// 一上来就置空，保证只打开一个折叠面板
			that.expands = [];
			if(expandedRows.length > 0){
				if(row){
					if(row.realAuth == '未认证'){
						return;
					}
					// 如果已经认证，就可以点开
					// 把当前行id放到数组中
					that.expands.push(row.id);
					
					let data = {
						realAuth: row.realAuth == '已认证' ? 2 : 3,
						driverId: row.id
					};
					let resp = await axios.post(`${that.$baseUrl}driver/searchDriverComprehensiveData`, data, {
						headers: {
							token: localStorage.getItem("token")
						}
					});
					
					let summaryMap = resp.data.result.summaryMap;
					that.content.year = summaryMap.year;
					that.content.birthday = summaryMap.birthday;
					that.content.email = summaryMap.email;
					that.content.mailAddress = summaryMap.mailAddress;
					that.content.idcardAddress = summaryMap.idcardAddress;
					that.content.idcardFront = summaryMap.idcardFront;
					that.content.idcardFrontList = [summaryMap.idcardFront];
					that.content.idcardBack = summaryMap.idcardBack;
					that.content.idcardBackList = [summaryMap.idcardBack];
					that.content.idcardHolding = summaryMap.idcardHolding;
					that.content.idcardHoldingList = [summaryMap.idcardHolding];
					that.content.drcardFront = summaryMap.drcardFront;
					that.content.drcardFrontList = [summaryMap.drcardFront];
					that.content.drcardBack = summaryMap.drcardBack;
					that.content.drcardBackList = [summaryMap.drcardBack];
					that.content.drcardHolding = summaryMap.drcardHolding;
					that.content.drcardHoldingList = [summaryMap.drcardHolding]; 
					
					// TODO 这里以后还有代码要写
				}
			}
		},
			
		showApproveModel(id){
			this.approveModelVisible = true;
			// 将来审批方法的回调函数要用的
			this.driverId = id;
		},
		
		// 审批方法
		async approveHandle(bool){
			let that = this;
			let data = {
				realAuth: bool ? 2 : 1,
				driverId: that.driverId
			};
			let resp = await axios.post(`${that.$baseUrl}driver/updateDriverRealAuth`, data, {
				headers: {
					token: localStorage.getItem("token")
				}
			});
			if(resp.data.result){
				that.approveModelVisible = false;
				that.$message.success("数据更新成功");
				that.expands = [];
				that.loadDataList();
			}
		},
		
		showRepealModel(id){
			this.repealModelVisible = true;
			this.driverId = id;
		},
		
		async repealHandle(){
			let that = this;
			let data = {
				realAuth: 3,
				driverId: that.driverId
			};
			let resp = await axios.post(`${that.$baseUrl}driver/updateDriverRealAuth`, data, {
				headers: {
					token: localStorage.getItem("token")
				}
			});
			if(resp.data.result){
				that.repealModelVisible = false;
				that.$message.success("数据更新成功");
				that.expands = [];
				that.loadDataList();
				
			}
		}
	},
	// 在vue组件实例创建完成后自动执行
	created: function() {
		this.loadDataList();
	}
};
</script>
<style lang="less" scoped="scoped">
@import url('driver.less');
</style>
