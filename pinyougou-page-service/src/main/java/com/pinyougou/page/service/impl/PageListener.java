package com.pinyougou.page.service.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.page.service.ItemPageService;
@Component
public class PageListener implements MessageListener {
	/**
	 * 生成商品详情页
	 */
	@Autowired
	private ItemPageService itemPageService;

	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		try {
			TextMessage textMessage = (TextMessage) message;
			String text = textMessage.getText();
			System.out.println("接收到的:"+text);
			boolean b = itemPageService.genItemHtml(Long.parseLong(text));
			System.out.println("打印结果:"+b);
			
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
