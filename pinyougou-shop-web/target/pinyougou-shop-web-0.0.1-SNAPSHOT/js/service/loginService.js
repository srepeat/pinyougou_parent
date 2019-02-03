app.service("loginService",function($http){
//	读取登陆名称
	this.loginName=function(){
		return $http.get('../login/name.do');
	}
});