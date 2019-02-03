//购物车控制层
app.controller('cartController',function($scope,cartService){
	
	//查询购物车列表
	$scope.findCartList=function(){
		cartService.findCartList().success(
				function(response){
					//将控制层的对象传递绑定为对象返回给页面
					$scope.cartList=response;
					//计算机总和
					$scope.totalValue=cartService.sum($scope.cartList);
				}
		)
	}
	
	//商品数量的增加
	$scope.addGoodsToCartList=function(itemId,num){
		cartService.addGoodsToCartList(itemId,num).success(
				function(response){
					//判断是否成功
					if(response.success){
						$scope.findCartList();//更新列表
					}else{
						alert(response.message);//弹出错误提示
					}
				}
		);
	}
	
	//获取地址列表
	$scope.findAddressList=function(){
		cartService.findAddressList().success(
				function(response){
					$scope.addressList=response;
					for(var i=0;i<$scope.addressList.length;i++){
						if($scope.addressList[i].isDefault=="1"){
							$scope.address = $scope.addressList[i];
							break;
						}
					}
				}
		);
	}
	
	
	//选择地址
	$scope.selectAddress=function(address){
		$scope.address=address;
		
		
		
	}
	
	//判断是否当前选择地址
	$scope.isSelectedAddress=function(address){
		
		if($scope.address == address){
			return true;
		}else{
			
			return false;
		}
		
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=addressService.update( $scope.entity ); //修改  
		}else{
			serviceObject=addressService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		addressService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.findAddressList();
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	
	$scope.order={paymentType:'1'};//支付对象，设置一个默认值
	
	//选择支付方式类型
	$scope.selectPayType=function(type){
		$scope.order.paymentType=type;
		
	}
	
	$scope.order={invoiceType:'1'};//发票对象
	//选择发票类型
	$scope.selectInvoiceType=function(type){
		$scope.order.invoiceType=type;
		
	}
	
	//增加订单
	$scope.submitOrder=function(){
		$scope.order.receiverAreaName=$scope.address.address;//收货人地址
		$scope.order.receiverMobile=$scope.address.mobile;//手机号
		$scope.order.receiver=$scope.address.contact;//联系人
		cartService.submitOrder($scope.order).success(
				function(response){
					if(response.success){
						//判断是在线支付还是活到付款支付
						//页面跳转
						if($scope.order.paymentType=='1'){//如果是微信支付，跳转到支付页面
							location.href="pay.html";
						}else{//如果货到付款，跳转到提示页面
							location.href="paysuccess.html";
						}
					}else{
						
						alert(response.message);
					}
					
					
				}
		);
		
	}
	
	
	
	
});