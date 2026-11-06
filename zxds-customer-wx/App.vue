<script>
export default {

    onShow: function() {
        console.log('App Show');
    },
    onHide: function() {
        console.log('App Hide');
    },
	
	onLaunch: function() {
	    //开启GPS后台刷新
	    wx.startLocationUpdate({
	        success(resp) {
	            console.log('开启定位成功');
	        },
	        fail(resp) {
	            console.log('开启定位失败');
	        }
	    });
	    // 当GPS定位发生变化时，自动执行回调函数
	    // LocationChange是一个事件的名字，当GPS定位发生变化时，自动调用回调函数
	    wx.onLocationChange(function(resp) {
	        // 拿到最新的维度
	        let latitude = resp.latitude;
	        // 拿到最新的经度
	        let longitude = resp.longitude;
	        // 封装为对象，方便将数据传递到工作台页面
	        let location = { latitude: latitude, longitude: longitude };
	        //触发自定义事件（在工作台页面绑定了 updateLocation事件，以下代码是触发这个事件，通过这种方式将数据传递给工作台页面）
	        uni.$emit('updateLocation', location);
			console.log("发射location");
	    });
	},
};
</script>

<style lang="scss">
@import "uview-ui/index.scss";
</style>
