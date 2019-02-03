package com.pinyougou.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

@RestController
@RequestMapping("/brand")
public class BrandController {
	
//	注入service
	@Reference
	private BrandService brandService;

//	查询全部品牌信息
	@RequestMapping("/findAll")
	public List<TbBrand> findAll(){
		
		return brandService.findAll();
	}
	
//	查询品牌信息
	@RequestMapping("/selectOptionList")
	public List<Map> selectOptionList(){
		return brandService.selectOptionList();
	}
	
	
//	分页
	@RequestMapping("/findPage")
	public PageResult findPage(int page,int size) {
		
		return brandService.findPage(page, size);
	}
	
//	添加数据
	@RequestMapping("/add")
	public Result add(@RequestBody TbBrand brand){
		try {
			brandService.add(brand);
			
			return new Result(true, "增加成功");
		} catch (Exception e) {
			
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
//	查询修改id
	@RequestMapping("/findOne")
	public TbBrand findOne(long id) {
		
		return brandService.findOne(id);
	}

//	修改信息
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand){
		try {
			brandService.update(brand);
			
			return new Result(true, "增加成功");
		} catch (Exception e) {
			
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(long [] ids){
		try {
			brandService.delete(ids);
			
			return new Result(true, "增加成功");
		} catch (Exception e) {
			
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 查询+分页
	 * @param brand 查询的对象
	 * @param page  页数
	 * @param size	最大的长度
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbBrand brand,int page,int size) {
		
		return brandService.findPage(brand,page,size);
	}
	
	
}
