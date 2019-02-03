app.controller('itemController',function($scope,$http){
	
	$scope.specificationItems={};//
	
	//������ӡ�����
	$scope.addNum=function(x){
		$scope.num+=x;
		if($scope.num<1){
			$scope.num=1;
		}
	}
	
	//�û����ѡ��
	$scope.selectspecificationItems=function(key,value){
		$scope.specificationItems[key]=value;
		searchSku();//ѡ��ƥ��
		
	}
	
	//�ж�ĳ���ѡ���Ƿ��û�ѡ��
	$scope.isSelectd=function(key,value){
		if($scope.specificationItems[key]==value){
			return true;
		}else{
			return false;
		}	
	}
	
	//����Ĭ��ѡȡ
	$scope.sku = {};
	
	$scope.loadSku = function(){
		$scope.sku=skuList[0];
		//����Ĭ��ѡ����
		$scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec))
		
	}
	
	//
	matchObjcet=function(map1,map2){
		
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}
		}
		
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}
		}
		
		return true;
		
	}
	
	
	//
	searchSku=function(){
		
		for(var i=0;i<skuList.length;i++){
			if(matchObjcet(skuList[i].spec, $scope.specificationItems)){
				$scope.sku = skuList[i];
				return;
			}
		}
		
		$scope.sku={id:0,title:"-----",price:0}//价格和标题
	}
	
	
	//购物车
	$scope.addToCart=function(){
		//alert("SKUID:"+$scope.sku.id);
		
		$http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='+$scope.sku.id+
				'&num='+$scope.num, {'withCredentials':true}).success(
				function(response){
					if(response.success){
						//跳转到购物车页面
						location.href='http://localhost:9107/cart.html';
					}else{
						//提示错误信息
						alert(response.message);
					}
					
				}
		
		
		);
		
	}
	
	
	
	
});