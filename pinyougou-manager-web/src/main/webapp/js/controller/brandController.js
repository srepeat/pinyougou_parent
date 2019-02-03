app.controller("beandController",function($scope,$controller,brandService){
	
//	继承
		$controller("baseController",{$scope:$scope});
	
	
    		$scope.findAll=function(){
    			brandService.findAll().success(
    				function(response){
    					$scope.list=response;
    				}	
    			);
    		}
    		

    	
    	//页数方法
    	$scope.findPage=function(page,size){
    		brandService.findPage(page,size).success(
    			function(response){
    					$scope.list=response.rows;//显示当前页数据
    					$scope.paginationConf.totalItems=response.total;//更新总数据
    			}		
    			
    		);
    		
    	}
    	
    	//新增
    	 $scope.save=function(){
    		
    		var objcet = null;
    		if($scope.entity.id !=null){
    			objcet = brandService.update($scope.entity);
    		}else{
				objcet = brandService.add($scope.entity);    			
    		}
    			objcet.success(
    			function(response){
    				if(response.success){
    					$scope.reloadList(); //重新刷新
    				}else{
    					alert(response.message);
    				}
    			}		
    		);
    		
    	}
    	
    	
    	//回显商品
    	$scope.findOne=function(id){
    		brandService.findOne(id).success(
    				function(response){
    					$scope.entity= response;
    				}
    		);
    		
    	}
    	
    	
    		
    	//删除
    	$scope.dele=function(){
    		
    	if(confirm("确认要删除吗?")){
    		brandService.dele($scope.selectIds).success(
    				function(response){
    					if(response.success){
    						$scope.reloadList();//重新加载
    					 }else{
    						 alert(response.message);
    					 }
    				}	
    		
    			 );
    			}
    		}
    	
    	// 分页+查询
    	//初始化搜索对象
    	$scope.searchEntity={};
    	
    	$scope.search=function(page,size){
    		brandService.search(page,size,$scope.searchEntity).success(
    			function(response){
    					$scope.list=response.rows;//显示当前页数据
    					$scope.paginationConf.totalItems=response.total;//更新总数据
    			}		
    		);
    	}
    	
    	
    	
    	});