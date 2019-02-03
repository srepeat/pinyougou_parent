package com.pinyougou.search.service.impl;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.search.service.ItemSearchService;

@Component
public class ItemDeleteListener implements MessageListener {

	
	@Autowired
	private ItemSearchService itemSearchService;

	@Override
	public void onMessage(Message message) {
		try {
			/**
			 * 将ids使用对象可序列化得方式，对Item进行监听
			 */
			ObjectMessage objectMessage = (ObjectMessage) message;
			Long [] goodIds = (Long[]) objectMessage.getObject();
			System.out.println("移除索引库："+goodIds.toString());
			itemSearchService.deleteByGoodsIds(Arrays.asList(goodIds));
			System.out.println("移除成功");
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
