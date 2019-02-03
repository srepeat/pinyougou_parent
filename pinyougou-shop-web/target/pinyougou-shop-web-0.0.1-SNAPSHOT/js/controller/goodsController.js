 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){	
		var id=$location.search()['id'];
		if(id==null){
			return ;
		}		
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				
				editor.html($scope.entity.goodsDesc.introduction );//商品介绍 
				//商品图片
				$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
				//扩展属性
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//规格选择
				$scope.entity.goodsDesc.specificationItems= JSON.parse($scope.entity.goodsDesc.specificationItems);
				//转换sku列表中的规格对象
				for(var i=0;i< $scope.entity.itemList.length;i++ ){
					$scope.entity.itemList[i].spec=  JSON.parse($scope.entity.itemList[i].spec);					
				}
				
			}
		);		
	}
	
	//保存 
	$scope.save=function(){			
		$scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					alert("保存成功");
					location.href="goods.html";
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	/**
	 * 上传图片
	 */
	$scope.uploadFile=function(){	  
		uploadService.uploadFile().success(
			function(response) {        	
	        	if(response.success){//如果上传成功，取出url
	        		$scope.image_entity.url=response.message;//设置文件地址
	        	}else{
	        		alert(response.message);
	        	}
	        }).error(function() {           
	        	     alert("上传发生错误");
	        });        
	    };   

//	上传添加图片goods:{},
    $scope.entity={goodsDesc:{itemImages:[],specificationItems:[]}};//定义页面实体结构
    //添加图片列表
    $scope.add_image_entity=function(){    	
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    
    
  //列表中移除图片
    $scope.remove_image_entity=function(index){
    	    $scope.entity.goodsDesc.itemImages.splice(index,1);
    }
    
//   下拉列表1级分类
    
    $scope.selectItemCat1List=function(){
    	
    	itemCatService.findByParentId(0).success(
    			function(response){
    				$scope.ItemCatList=response;
    			}
    	);
    }
    
//  下拉列表二级目录
    $scope.$watch('entity.goods.category1Id',function(newValue,oldValue){
    	
    	itemCatService.findByParentId(newValue).success(
    			function(response){
    				$scope.ItemCat2List=response;
    			}
    	);
    	
    });
    
//    下拉列表三级目录
	 $scope.$watch('entity.goods.category2Id',function(newValue,oldValue){
	    	
	    	itemCatService.findByParentId(newValue).success(
	    			function(response){
	    				$scope.ItemCat3List=response;
	    			}
	    	);
	    	
	    });
    
//	 模板ID
	 $scope.$watch('entity.goods.category3Id',function(newValue,oldValue){

		 itemCatService.findOne(newValue).success(
				 function(response){
					 $scope.entity.goods.typeTemplateId=response.typeId;
				 }
		 );
	    });
 
	 
//	 品牌关联
	 $scope.$watch('entity.goods.typeTemplateId',function(newValue,oldValue){

		 typeTemplateService.findOne(newValue).success(
				 function(response){
					 $scope.typeTemplate=response;//品牌id
					 $scope.typeTemplate.brandIds= JSON.parse( $scope.typeTemplate.brandIds);//品牌列表
					 
//					 扩展属性
					 if ($location.search()['id']==null){//如果是增加商品就有id
						 $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
					 }
				 }
		 );
		 typeTemplateService.findSpecList(newValue).success(
				 function(response){
					 $scope.specList=response;
				 }
		 );
		 
	    });
	 
//	 规格
	 $scope.updateSpecAttribute=function($event,name,value){
			
			var object= $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems ,'attributeName', name);
			
			if(object!=null){	
				if($event.target.checked ){
					object.attributeValue.push(value);		
				}else{//取消勾选
					object.attributeValue.splice( object.attributeValue.indexOf(value ) ,1);//移除选项
					//如果选项都取消了，将此条记录移除
					if(object.attributeValue.length==0){
						$scope.entity.goodsDesc.specificationItems.splice(
								$scope.entity.goodsDesc.specificationItems.indexOf(object),1);
					}
					
				}
			}else{	
				$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
			}
			
		}
	 
	//创建SKU列表
		$scope.createItemList=function(){
			
			$scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0'} ];//列表初始化
			
			var items= $scope.entity.goodsDesc.specificationItems;
			
			for(var i=0;i<items.length;i++){
				$scope.entity.itemList= addColumn( $scope.entity.itemList, items[i].attributeName,items[i].attributeValue );			
			}	
			
		}
		
		addColumn=function(list,columnName,columnValues){
			
			var newList=[];		
			for(var i=0;i< list.length;i++){
				var oldRow=  list[i];			
				for(var j=0;j<columnValues.length;j++){
					var newRow=  JSON.parse( JSON.stringify(oldRow)  );//深克隆
					newRow.spec[columnName]=columnValues[j];
					newList.push(newRow);
				}			
			}		
			return newList;
		}
	 
//	 状态分类
		$scope.status=['未审核','已审核','审核未通过','关闭'];

//	查询级别名称
		$scope.itemCatList=[];
//		类别
		$scope.findItemCatList=function(){
			itemCatService.findAll().success(
					function(response){
						for(var i=0;i<response.length;i++){
							$scope.itemCatList[response[i].id]=response[i].name;
						}
					}
			);
			
		}
		
//		规格
		//判断规格与规格选项是否应该被勾选
		$scope.checkAttributeValue=function(specName,optionName){
			var items= $scope.entity.goodsDesc.specificationItems;
			var object =$scope.searchObjectByKey( items,'attributeName', specName);
			
			if(object!=null){
				if(object.attributeValue.indexOf(optionName)>=0){//如果能够查询到规格选项
					return true;
				}else{
					return false;
				}			
			}else{
				return false;
			}		
		}


});	
