app.controller("indexController",function($scope,$controller  ,loginService){
//	显示登录姓名
	$scope.showLoginName=function(){
		loginService.loginName().success(
				function(response){
					$scope.loginName=response.loginName;
			}
		);
	}
	
});