package com.pinyougou.solrutil;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

@Component
public class SolrUtil {
	
	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private SolrTemplate solrTemplate;
	
	
	public void impotItemDta() {
		TbItemExample example = new TbItemExample();
		Criteria createCriteria = example.createCriteria();
		createCriteria.andStatusEqualTo("1");//审核通过
		List<TbItem> list = itemMapper.selectByExample(example );
		
		System.out.println("商品录入");
		for (TbItem tbItem : list) {
			System.out.println(tbItem.getTitle()+"  "+tbItem.getCategory()+"  "+tbItem.getPrice());
			Map map = JSON.parseObject(tbItem.getSpec(), Map.class);//将JSON字符转换为map
			tbItem.setSpecMap(map);//到注释的字段复制
		}
		System.out.println("商品结束");
		
		solrTemplate.saveBeans(list);//保存
		solrTemplate.commit();//提交
		
	}
	
	
	public static void main(String[] args) {
		
	ApplicationContext context=new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
	
	SolrUtil util = (SolrUtil) context.getBean("solrUtil");
	util.impotItemDta();
	
	}

}
