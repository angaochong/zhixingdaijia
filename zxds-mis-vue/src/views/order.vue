<template>
	<div v-if="isAuth(['ROOT', 'ORDER:SELECT'])">
		<el-form :inline="true" :model="dataForm" :rules="dataRule" ref="dataForm">
			<el-form-item prop="orderId"><el-input v-model="dataForm.orderId" placeholder="订单编号" size="medium" clearable="clearable" /></el-form-item>
			<el-form-item prop="driverId"><el-input v-model="dataForm.driverId" placeholder="司机编号" size="medium" clearable="clearable" /></el-form-item>
			<el-form-item prop="customerId"><el-input v-model="dataForm.customerId" placeholder="客户编号" size="medium" clearable="clearable" /></el-form-item>
			<el-form-item>
				<el-date-picker v-model="dataForm.date" type="daterange" range-separator="~" start-placeholder="开始日期" end-placeholder="结束日期" size="medium"></el-date-picker>
			</el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.status" class="input" placeholder="状态" size="medium" clearable="clearable">
					<el-option label="等待接单" value="1" />
					<el-option label="已接单" value="2" />
					<el-option label="司机已到达" value="3" />
					<el-option label="开始代驾" value="4" />
					<el-option label="结束代驾" value="5" />
					<el-option label="未付款" value="6" />
					<el-option label="已付款" value="7" />
					<el-option label="订单已结束" value="8" />
					<el-option label="顾客撤单" value="9" />
					<el-option label="司机撤单" value="10" />
					<el-option label="事故关闭" value="11" />
					<el-option label="其他" value="12" />
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
			<el-table-column prop="panel" header-align="center" align="center" type="expand">
				<template #default="scope">
					<div class="content-container">
						<div class="left">
							<div class="order-table">
								<div class="row">
									<div class="th">客户编号</div>
									<div class="td">{{ panel.customer.id }}</div>
									<div class="th">客户性别</div>
									<div class="td">{{ panel.customer.sex }}</div>
									<div class="th">客户电话</div>
									<div class="td">{{ panel.customer.tel }}</div>
								</div>
								<div class="row">
									<div class="th">车型</div>
									<div class="td">{{ panel.order.carType }}</div>
									<div class="th">车牌</div>
									<div class="td">{{ panel.order.carPlate }}</div>
									<div class="th">车辆隶属</div>
									<div class="td">{{ panel.order.city }}</div>
								</div>
								<div class="row">
									<div class="th">司机编号</div>
									<div class="td">{{ panel.driver.id }}</div>
									<div class="th">司机姓名</div>
									<div class="td">{{ panel.driver.name }}</div>
									<div class="th">司机电话</div>
									<div class="td">{{ panel.driver.tel }}</div>
								</div>
								<div class="row">
									<div class="th">接单时间</div>
									<div class="td">{{ panel.order.acceptTime }}</div>
									<div class="th">到达时间</div>
									<div class="td">{{ panel.order.arriveTime }}</div>
									<div class="th">代驾等时</div>
									<div class="td">{{ panel.order.waitingMinute }}</div>
								</div>
								<div class="row">
									<div class="th">开始时间</div>
									<div class="td">{{ panel.order.startTime }}</div>
									<div class="th">结束时间</div>
									<div class="td">{{ panel.order.endTime }}</div>
									<div class="th">代驾时长</div>
									<div class="td">{{ panel.order.driveMinute }}</div>
								</div>
								<div class="row">
									<div class="th">代驾里程</div>
									<div class="td">{{ panel.order.realMileage }}</div>
									<div class="th">订单费用</div>
									<div class="td">{{ panel.order.realFee }}</div>
									<div class="th">状态</div>
									<div class="td">{{ panel.order.status }}</div>
								</div>
								<div class="row">
									<div class="th">费用规则</div>
									<div class="td">{{ panel.order.chargeRule }}</div>
									<div class="th">取消规则</div>
									<div class="td">{{ panel.order.cancelRule }}</div>
									<div class="th">分账规则</div>
									<div class="td">{{ panel.order.profitsharingRule }}</div>
								</div>
							</div>
						</div>
						<div class="right"><div :id="panel.id" class="map"></div></div>
					</div>
				</template>
			</el-table-column>
			<el-table-column type="index" header-align="center" align="center" width="100" label="序号">
				<template #default="scope">
					<span>{{ (pageIndex - 1) * pageSize + scope.$index + 1 }}</span>
				</template>
			</el-table-column>
			<el-table-column prop="id" header-align="center" align="center" min-width="200" label="订单编号" />
			<el-table-column prop="startPlace" header-align="center" align="center" min-width="220" show-overflow-tooltip="true" label="代驾起点" />
			<el-table-column prop="endPlace" header-align="center" align="center" min-width="220" show-overflow-tooltip="true" label="代驾终点" />
			<el-table-column prop="realMileage" header-align="center" align="center" min-width="130" label="实际里程" />
			<el-table-column prop="realFee" header-align="center" align="center" min-width="130" label="实际金额" />
			<el-table-column prop="createTime" header-align="center" align="center" min-width="150" label="日期时间" />
			<el-table-column prop="status" header-align="center" align="center" min-width="100" label="状态" />
			<el-table-column header-align="center" align="center" width="150" label="操作">
				<template #default="scope">
					<el-button
						type="text"
						size="medium"
						v-if="isAuth(['ROOT', 'ORDER:SELECT'])"
						:disabled="!['结束代驾', '未付款', '已付款', '订单已结束'].includes(scope.row.status)"
						@click="showOrderBill(scope.row.id)"
					>
						账单详情
					</el-button>
					<el-button
						type="text"
						size="medium"
						v-if="isAuth(['ROOT', 'ORDER:UPDATE'])"
						:disabled="!['等待接单', '已接单', '司机已到达', '开始代驾'].includes(scope.row.status)"
						@click="showUpdatePanel(scope.row.id)"
					>
						关闭订单
					</el-button>
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
		<order-bill ref="orderBill"></order-bill>
		<order-close ref="orderClose" @refreshDataList="loadDataList"></order-close>
	</div>
</template>

<script>
	import axios from 'axios';
import OrderBill from './order_bill.vue';
import OrderClose from './order-close.vue';

import $ from 'jquery';
import { calculateCarPlateCity } from '../utils/car_plate.js';
let status = {
	'1': '等待接单',
	'2': '已接单',
	'3': '司机已到达',
	'4': '开始代驾',
	'5': '结束代驾',
	'6': '未付款',
	'7': '已付款',
	'8': '订单已结束',
	'9': '顾客撤单',
	'10': '司机撤单',
	'11': '事故关闭',
	'12': '其他'
};
export default {
	components: {
		OrderBill,
		OrderClose
	},
	data() {
		return {
			dataForm: {
				orderId: null,
				driverId: null,
				customerId: null,
				date: [],
				status: null
			},
			dataList: [],
			pageIndex: 1,
			pageSize: 10,
			totalCount: 0,
			dataListLoading: false,
			dataRule: {
				orderId: [{ required: false, pattern: '^[1-9]\\d{18}$', message: '订单编号格式错误' }],
				driverId: [{ required: false, pattern: '^[1-9]\\d{18}$', message: '司机编号格式错误' }],
				customerId: [{ required: false, pattern: '^[1-9]\\d{18}$', message: '客户编号格式错误' }]
			},
			expands: [],
			panel: {
				id: null,
				customer: {
					id: null,
					sex: null,
					tel: null
				},
				driver: {
					id: null,
					name: null,
					tel: null
				},
				order: {
					carPlate: null,
					carType: null,
					city: null,
					acceptTime: null,
					arriveTime: null,
					startTime: null,
					endTime: null,
					waitingMinute: null,
					driveMinute: null,
					realMileage: null,
					realFee: null,
					status: null,
					chargeRule: null,
					cancelRule: null,
					profitsharingRule: null
				}
			},
			getRowKeys(row) {
				return row.id;
			},
			gpsTimer: null
		};
	},
	methods: {
		// 分页查询订单
		async loadDataList() {
		    let that = this;
		    that.dataListLoading = true;
		
		    let data = {
		        pageIndex: that.pageIndex,
		        pageSize: that.pageSize,
		        orderId: that.dataForm.orderId == '' ? null : that.dataForm.orderId,
		        driverId: that.dataForm.driverId == '' ? null : that.dataForm.driverId,
		        customerId: that.dataForm.customerId == '' ? null : that.dataForm.customerId,
		        status: that.dataForm.status == '' ? null : that.dataForm.status
		    };
		    if (that.dataForm.date != null && that.dataForm.date.length == 2) {
		        let startDate = that.dataForm.date[0];
		        let endDate = that.dataForm.date[1];
		        data.startDate = dayjs(startDate).format('YYYY-MM-DD');
		        data.endDate = dayjs(endDate).format('YYYY-MM-DD');
		    }
		
		  let resp = await axios.post(`${that.$baseUrl}order/searchOrderByPage`, data, {
		    headers : {
		      token: localStorage.getItem("token")
		    }
		  });
		  
		  let result = resp.data.result;
		  let list = result.list;
		      
		  for (let one of list) {
		      one.status = status[one.status + ''];
		      if (!one.hasOwnProperty('realMileage')) {
		          one.realMileage = '--';
		      }
		      
		      if (!one.hasOwnProperty('realFee')) {
		          one.realFee = '--';
		      }
		  }
		  that.dataList = list;
		  that.totalCount = Number(result.totalCount);
		  that.dataListLoading = false;
		},
		
		sizeChangeHandle(val) {
		    this.pageSize = val;
		    this.pageIndex = 1;
		    this.loadDataList();
		},
		  
		currentChangeHandle(val) {
		    if(val){
		      this.pageIndex = val;
		      this.loadDataList();  
		    }
		},
		
		// 分页条件查询
		async searchHandle() {
		  let valid = await this.$refs.dataForm.validate();
		  if (valid) {
		    this.$refs.dataForm.clearValidate();
		    this.loadDataList();
		  } else {
		    return false;
		  }
		},
		
		// 加载面板数据
		async loadPanelData(row) {
		    let that = this;
		    // 发送ajax请求，获取数据
		    let data = {
		        orderId: row.id
		    };
		    let resp = await axios.post(`${that.$baseUrl}order/searchOrderComprehensiveInfo`, data, {
		      headers:{
		        token: localStorage.getItem("token")
		      }
		    });
		    let result = resp.data.result;
		  
		    // 获取客户信息
		    let customerInfo = result.customerInfo;
		    that.panel.customer.id = customerInfo.id;
		    that.panel.customer.sex = customerInfo.sex;
		    that.panel.customer.tel = customerInfo.tel;
		    
		    // 获取司机信息
		    if(result.driverInfo){
		      let driverInfo = result.driverInfo;
		      that.panel.driver.id = driverInfo.id;
		      that.panel.driver.name = driverInfo.name;
		      that.panel.driver.tel = driverInfo.tel;
		    }
		    
		    // 获取订单信息
		    let content = result.content;
		    that.panel.order.carPlate = content.carPlate;
		    that.panel.order.carType = content.carType;
		    let city = calculateCarPlateCity(content.carPlate);
		    that.panel.order.city = city;
		    if (content.hasOwnProperty('acceptTime')) {
		        that.panel.order.acceptTime = content.acceptTime;
		    } else {
		        that.panel.order.acceptTime = '--';
		    }
		    if (content.hasOwnProperty('arriveTime')) {
		        that.panel.order.arriveTime = content.arriveTime;
		    } else {
		        that.panel.order.arriveTime = '--';
		    }
		    if (content.hasOwnProperty('startTime')) {
		        that.panel.order.startTime = content.startTime;
		    } else {
		        that.panel.order.startTime = '--';
		    }
		    if (content.hasOwnProperty('endTime')) {
		        that.panel.order.endTime = content.endTime;
		    } else {
		        that.panel.order.endTime = '--';
		    }
		    if (content.hasOwnProperty('waitingMinute')) {
		        that.panel.order.waitingMinute = content.waitingMinute + '分钟';
		    } else {
		        that.panel.order.waitingMinute = '--';
		    }
		    if (content.hasOwnProperty('driveMinute')) {
		        that.panel.order.driveMinute = content.driveMinute + '分钟';
		    } else {
		        that.panel.order.driveMinute = '--';
		    }
		    if (content.hasOwnProperty('realMileage')) {
		        that.panel.order.realMileage = content.realMileage + '公里';
		        // 一旦有真实的里程数据后，一定要回填给row.realMileage，这样可以保证详细信息中显示的真实里程和表格中的真实里程一致。
		        // 如果不写这行代码，最终会导致：分页查询时表格显示的里程数是"--"，过了一会，订单结束了，真实里程生成了，表格上还是显示"--"，详细信息中会显示里程数。
		        row.realMileage = content.realMileage;
		    } else {
		        that.panel.order.realMileage = '--';
		        row.realMileage = '--';
		    }
		    if (content.hasOwnProperty('realFee')) {
		        that.panel.order.realFee = content.realFee + '元';
		        // 实际费用也需要回填
		        row.realFee = content.realFee;
		    } else {
		        that.panel.order.realFee = '--';
		        row.realFee = '--';
		    }
		    
		    that.panel.order.status = status[String(content.status)];
		    row.status = status[String(content.status)];
		  
		    if (result.hasOwnProperty('chargeRule')) {
		        that.panel.order.chargeRule = result.chargeRule.code;
		    } else {
		        that.panel.order.chargeRule = '--';
		    }
		    if (result.hasOwnProperty('cancelRule')) {
		        that.panel.order.cancelRule = result.cancelRule.code;
		    } else {
		        that.panel.order.cancelRule = '--';
		    }
		    if (result.hasOwnProperty('profitsharingRule')) {
		        that.panel.order.profitsharingRule = result.profitSharingRule.code;
		    } else {
		        that.panel.order.profitsharingRule = '--';
		    }
		  
		    // $nextTick 用于等待 Vue 把展开行的地图容器 div 渲染到 DOM 后，再初始化地图，避免因元素不存在而导致地图创建失败。
		    that.$nextTick(function() {
		        // 获取起点和终点坐标
		        let startPlaceLocation = content.startPlaceLocation;
		        let endPlaceLocation = content.endPlaceLocation;
		
		        // 以起点为中心创建地图实例
		        let mapCenter = new TMap.LatLng(startPlaceLocation.latitude, startPlaceLocation.longitude);
		
		        // 在指定的div容器中创建地图
		        let map = new TMap.Map($(`.el-table__expanded-cell #order_${row.id}`)[0], {
		            center: mapCenter,   // 地图显示中心点（起点）
		            zoom: 13,            // 缩放级别，数字越大越详细
		            viewMode: '2D',      // 2D视图模式
		            baseMap: {           // 底图配置
		                type: 'vector',  // 矢量地图
		                features: ['base', 'label']  // 显示基础地图和标签
		            }
		        });
		
		        // 绘制规划路线（灰色线）
		        let driveLine = result.driveLine; // 从后端获取的规划路线数据
		        let coors = driveLine.routes[0].polyline; // 获取路线坐标点串（压缩格式）
		        let pl = [];  // 存储解压后的坐标点
		
		        // 腾讯地图的坐标解压算法
		        let kr = 1000000;
		        for (let i = 2; i < coors.length; i++) {
		            coors[i] = Number(coors[i - 2]) + Number(coors[i]) / kr;
		        }
		      
		        // 将解压后的坐标生成LatLng数组
		        for (let i = 0; i < coors.length; i += 2) {
		            pl.push(new TMap.LatLng(coors[i], coors[i + 1]));
		        }
		
		        // 创建折线图层，绘制灰色规划路线
		        let polylineLayer = new TMap.MultiPolyline({
		            id: 'polyline-layer', //图层唯一标识
		            map: map, //绘制到目标地图
		            styles: {  // 折线样式定义
		                style_blue: new TMap.PolylineStyle({
		                    color: 'rgba(190,188,188,1)',  // 灰色
		                    width: 6,     // 线宽6像素
		                    lineCap: 'round' // 线端头为圆角
		                })
		            },
		            geometries: [    // 折线数据
		                {
		                    id: 'pl_1',
		                    styleId: 'style_blue',  // 使用上面定义的灰色样式
		                    paths: pl   // 解压后的坐标点数组
		                }
		            ]
		        });
		
		        // 创建标记图层（起点、终点、车辆）
		        let markerLayer = new TMap.MultiMarker({
		            map: map,
		            styles: {  // 定义三种标记样式
		                startStyle: new TMap.MarkerStyle({ // 起点标记样式
		                    width: 24,
		                    height: 36,
		                    anchor: { x: 16, y: 32 },
		                    src: 'https://mapapi.qq.com/web/lbs/javascriptGL/demo/img/start.png'
		                }),
		                endStyle: new TMap.MarkerStyle({  // 终点标记样式
		                    width: 24,
		                    height: 36,
		                    anchor: { x: 16, y: 32 },
		                    src: 'https://mapapi.qq.com/web/lbs/javascriptGL/demo/img/end.png'
		                }),
		                carStyle: new TMap.MarkerStyle({  // 车辆标记样式
		                    width: 30,
		                    height: 30,
		                    src: '../order/driver-icon.png',
		                    anchor: { x: 16, y: 32 }
		                })
		            },
		            geometries: [   // 添加起点和终点标记
		                //起点标记
		                {
		                    id: '1',
		                    styleId: 'startStyle',
		                    position: new TMap.LatLng(startPlaceLocation.latitude, startPlaceLocation.longitude)
		                },
		                //终点标记
		                {
		                    id: '2',
		                    styleId: 'endStyle',
		                    position: new TMap.LatLng(endPlaceLocation.latitude, endPlaceLocation.longitude)
		                }
		            ]
		        });
		
		        // 判断订单状态，进行不同处理
		        // 判断司机是否驾车中
		        if (content.status == 4) {
		            // 获取司机最后已知位置
		            let lastGps = result.lastGps;
					console.log(lastGps);
		
		            // 在地图上添加车辆标记
		            markerLayer.add([
		                {
		                    id: '3',
		                    styleId: 'carStyle', //指定样式id
		                    position: new TMap.LatLng(lastGps.latitude, lastGps.longitude)
		                }
		            ]);
		          
		            // 设置定时器，每隔15秒获取最新GPS定位，实时更新车辆位置
		            that.gpsTimer = setInterval(async function() {
		                //先查询订单状态
		                let data = {
		                    orderId: row.id
		                };
		                let resp = await axios.post(`${baseUrl}order/searchOrderLastGps`, data, {
		                  headers: {
		                    token: localStorage.getItem("token")
		                  }
		                });
		        
		                if (resp.data.hasOwnProperty('result')) {
		                    // 获取到新的GPS坐标，更新车辆标记位置
		                    let lastGps = resp.data.result;
		                    markerLayer.updateGeometries([
		                        {
		                            id: '3',
		                            styleId: 'carStyle',
		                            position: new TMap.LatLng(lastGps.latitude, lastGps.longitude)
		                        }
		                    ]);
		                } else {
		                    // 如果没有获取到GPS数据（可能订单状态变了），重新加载面板
		                    $(`.el-table__expanded-cell #order_${row.id}`).empty(); // 清空地图容器
		                    that.loadPanelData(row);  // 重新加载面板数据
		                    clearInterval(that.gpsTimer);  // 清除定时器
		                }
		            }, 15 * 1000);
		        }
		      
		        // 代驾已结束
		        if (content.status >= 5 && content.status <= 8) {
		            // 获取代驾全程的GPS记录
		            let orderGps = result.orderGps;
		            let paths = [];
		            // 将GPS记录转换为路径点
		            for (let one of orderGps) {
		                let temp = new TMap.LatLng(one.latitude, one.longitude);
		                paths.push(temp);
		            }
		
		            // 绘制蓝色实走路线（车辆实际行驶轨迹）
		            let polylineLayer = new TMap.MultiPolyline({
		                id: 'drive-polyline-layer', //图层唯一标识
		                map: map,
		                //折线样式定义
		                styles: {
		                    style_blue: new TMap.PolylineStyle({
		                        color: '#3777FF',  // 蓝色，表示实际行驶路线
		                        width: 6 //折线宽度
		                    })
		                },
		                //折线数据定义
		                geometries: [
		                    {
		                        id: 'pl_1',
		                        styleId: 'style_blue',
		                        paths: paths  // 实际GPS轨迹点
		                    }
		                ]
		            });
		        }
		    });
		},
		
		// 打开或关闭折叠面板回调
		expand(row, expandedRows) {
		    let that = this;
		    if (expandedRows.length > 0) {
		        that.expands = [];
		        if (row) {
		            that.expands.push(row.id);
		            that.panel.id = `order_${row.id}`;
		            that.loadPanelData(row);
		        }
		    } else {
		        that.expands = [];
		    }
		},
	},
	created: function() {
		this.loadDataList();
	}
};
</script>
<style lang="less" scoped="scoped">
@import url('order.less');
</style>
