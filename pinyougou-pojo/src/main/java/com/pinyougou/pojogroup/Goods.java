package com.pinyougou.pojogroup;

import java.io.Serializable;
/**
 * 商品组合类
 * @author 鲜磊
 *
 */
import java.util.List;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
public class Goods implements Serializable {

	private TbGoods goods;//商品SPU
	
	private TbGoodsDesc goodsDesc;//商品介绍表、扩展表
	
	private List<TbItem>  itemList; // 商品SKU表

	public TbGoods getGoods() {
		return goods;
	}

	public void setGoods(TbGoods goods) {
		this.goods = goods;
	}

	public TbGoodsDesc getGoodsDesc() {
		return goodsDesc;
	}

	public void setGoodsDesc(TbGoodsDesc goodsDesc) {
		this.goodsDesc = goodsDesc;
	}

	public List<TbItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<TbItem> itemList) {
		this.itemList = itemList;
	}


	
	
	
}
