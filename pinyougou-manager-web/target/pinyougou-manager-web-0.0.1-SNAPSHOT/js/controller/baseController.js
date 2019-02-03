app.controller("baseController",function($scope){
	
	//分页控件配置 currentPage:当前页 ; totalItems:总记录数; itemsPerPage:每页记录数 ;perPageOptions:分页选项，修改每页显示记录数 ; onchange:当页码重新变更时自动触发方法
	$scope.paginationConf = {
			 currentPage: 1,
			 totalItems: 10,
			 itemsPerPage: 10,
			 perPageOptions: [10, 20, 30, 40, 50],
			 onChange: function(){
			        	$scope.reloadList();//重新加载
			 }
	}; 
	
	//重新加载列表 数据
	$scope.reloadList=function(){
		 //切换页码  
		 $scope.search( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
	}
	
	//删除商品
	$scope.selectIds=[];//选中的id集合
	
	//传入源和id
	$scope.updateSelection=function($event,id){
		/* if($event.target.checked){ */
			if($event.target.checked){//如果是被选中,则增加到数组   			
			$scope.selectIds.push(id);
		}else{
			var index = $scope.selectIds.indexOf(id);
			 $scope.selectIds.splice(index, 1);//删除 删除的位置 删除的个数 
			 
		}
	}
	
//	优化品牌
	$scope.jsonToString=function(jsonString,key){
		
		var json=JSON.parse(jsonString);//将json字符串转换为json对象
		var value="";
//		循环遍历
		for(var i=0;i<json.length;i++){		
			if(i>0){
				value+=","
			}
			value+=json[i][key];			
		}
		return value;
	}
	
	
	
	
});