//控制层
app.controller('seckillGoodsController' ,function($scope,$location,$interval,seckillGoodsService){
	
	$scope.findList=function(){
		seckillGoodsService.findList().success(
			function(response){
				$scope.list=response;
			}			
		);
	}
	
	$scope.findOneFromRedis=function(){
		var id = $location.search()['id'];
		seckillGoodsService.findOneFromRedis(id).success(
				function(response){
					$scope.entity=response;
					
					//倒计时开始
					//获取从结束时间到当前日期的秒数
					allsecond=Math.floor( (new Date($scope.entity.endTime).getTime()) - new Date().getTime()/1000);
					
					time = $interval(function(){
						allsecond = allsecond-1;
						$scope.timeString = conventTimeString(allsecond);
						if(allsecond<=0){
							$interval.cancel(time);
						}
					},1000);
				}
		);
		
	}
	
	//转换天数
	conventTimeString=function(allsecond){
		var days =Math.floor( allsecond/(60*60*24));//天数
		var hours = Math.floor((allsecond-days*60*60*24)/(60*60));//小时数
		var min = Math.floor((allsecond-days*60*60*24-hours*60*60)/60);//分钟数
		var seconds = allsecond-days*60*60*24-hours*60*60-min*60;//秒数
		var timeString = "";
		if(days>0){
			timeString=days+"天";
		}
		return timeString+hours+":"+min+":"+seconds;
		
	}
	
	//提交订单
	$scope.submitOrder=function(){
		seckillGoodsService.submitOrder($scope.entity.id).success(
				function(response){
					if(response.success){//提交成功
						alert("抢购成功，请在五分钟之内付款");
						location.href='pay.html'
					}else{
						alert(response.message);
					}
				}
		);
		
		
	}
	
	
	
	
});