//购物车服务层
app.service('cartService',function($http){
	
	//购物车列表
	this.findCartList=function(){
		return $http.get('cart/findCartList.do');
	}
	
	//商品数量的增加
	this.addGoodsToCartList=function(itemId,num){
		return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
		
	}
	
	//商品计价总和方法抽取
	this.sum=function(cartList){
		//构建一个对象来存数量和金额
		var totalValue = {totalNum:0,totalMoney:0.00};
		
		//遍历购物车列表
		for(var i=0;i<cartList.length;i++){
			var cart =cartList[i];
			//遍历明细列表
			for(var j=0;j<cart.orderItemList.length;j++){
				var orderItem = cart.orderItemList[j];
				//对象的初始值加上页面上传递数量的累加记和
				totalValue.totalNum+=orderItem.num;
				totalValue.totalMoney+=orderItem.totalFee;
			}
		}
		return totalValue;
		
	}
	
	//获取地址列表
	this.findAddressList=function(){
		return $http.get('address/findListByLoginUser.do');
	}
	
	//提交订单
	this.submitOrder=function(order){
		
		return $http.post('order/add.do',order);
	}
	
	
	//增加 
	this.add=function(entity){
		return  $http.post('address/add.do',entity );
	}
	
	//修改 
	this.update=function(entity){
		return  $http.post('address/update.do',entity );
	}
	//删除
	this.dele=function(ids){
		return $http.get('address/delete.do?ids='+ids);
	}

	
});