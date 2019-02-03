app.controller('searchController',function($scope,$location,searchService){
	
//	搜索的对象
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'',
			'pageNo':1,'pageSize':40,'sort':'','sortField':''};
	
	//搜索
	$scope.search=function(){
		
		$scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
		searchService.search($scope.searchMap).success(
			function(response){
				$scope.resultMap=response;		
				
				buildPageLabel();//构建分页
			}
		);		
	}
	
//	构建分页
	buildPageLabel=function(){
		$scope.pageLabel=[];//新增分页栏属性
		var firstPage=1;//当前页码
		var lastPage=$scope.resultMap.totalPages;//截至页码
		$scope.firstDot=true;//前面得点
		$scope.lastDot=true;//后面得点
		
//		判断页码
		if($scope.resultMap.totalPages>5){//如果当前页码数大于5
			
			if($scope.searchMap.pageNo<=3){//如果页码小于3，那就显示当前5页
				lastPage=5;
				$scope.firstDot=false;//前面页面没点
			}else if($scope.searchMap.pageNo>= $scope.resultMap.totalPages-2){//显示最后五页
				firstPage=$scope.resultMap.totalPages-4;//计算规则100-4 94开始显示最后5页
				$scope.lastDot=false;//后面没点
			}else{//如果前者条件都不满足。就为中间条件
				firstPage=$scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2;
			}
			
		}else{
				$scope.firstDot=false;//前面无点
				$scope.lastDot=false;//后边无点
			}
		

//		遍历
		for(var i=firstPage;i<=lastPage;i++){
			$scope.pageLabel.push(i);
		}
	}
	
	
//	添加搜索项
	$scope.addSearchItem=function(key,value){
//		 判断Key 和Value是否为选择
		if(key=='category' || key=='brand' || key=='price'){
			$scope.searchMap[key]=value;
		}else{
//			如果不是就为规格
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();//执行搜索
	}
	
	$scope.removeSearchItem=function(key,value){
//		 判断Key 和Value是否为选择
		if(key=='category' || key=='brand' || key=='price'){
			$scope.searchMap[key]="";
		}else{
//			移除规格
			delete $scope.searchMap.spec[key];
		}
		$scope.search();//执行搜索
	}
	
	
	$scope.queryByPage=function(pageNo){
//		判断、如果当前页码小于1并且大于最大页码就点击不动
		if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
			return ;
		}
		$scope.searchMap.pageNo=pageNo;
		$scope.search();//执行搜索
	}
	
//	显示上一页
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo==1){
			return true;
		}else{
			return false;
		}
	}

	//判断当前页是否未最后一页
	$scope.isEndPage=function(){
		if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
			return true;
		}else{
			return false;
		}
	}
// 价格排序
	$scope.sortSearch=function(sortField,sort){
		$scope.searchMap.sortField=sortField;
		$scope.searchMap.sort=sort;
		
		$scope.search();//执行搜索
	}
	
//	隐藏品牌，判断当前是否为品牌
	$scope.keywordsByBrand=function(){
		for(var i=0;i<$scope.resultMap.brandList.length;i++){
//			如果包含这个关键字的品牌
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
				return true;
			}
		}
		return false;
	}
	
//	加载查询字符串
	$scope.loadkeywords=function(){
		$scope.searchMap.keywords= $location.search()['keywords'];
		
		$scope.search();//执行搜索
	}
	
	
});