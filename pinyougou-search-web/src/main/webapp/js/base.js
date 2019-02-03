var app = angular.module("pinyougou",[]);//pagination是引入的分页插件模块

//$sce过滤器服务
app.filter('trustHtml',['$sce',function($sce){
	return function(data){
		
		return $sce.trustAsHtml(data);
	}
	
	
}]);
