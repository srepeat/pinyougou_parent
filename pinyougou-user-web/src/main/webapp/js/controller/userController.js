 //控制层 
app.controller('userController' ,function($scope,$controller   ,userService){	
	
	
	$scope.reg=function(){
		
		if($scope.entity.password!=$scope.password){
			//清空输入框的数值
			alert("两次密码不一致");
			$scope.entity.password="";
			$scope.password="";
			return;
		}
		
		userService.add($scope.entity,$scope.smscode).success(
				function(response){
					alert(response.message);
			}
		);
	}
	
	
	//发送验证码
	$scope.sendCode=function(){
		if($scope.entity.phone==null && $scope.entity.phone==""){
			alert("请输入手机号！");
			return ;
		}
		
		userService.sendCode($scope.entity.phone).success(
				function(response){
					alert(response.message);
				}
		);
		
	}
	
});	
