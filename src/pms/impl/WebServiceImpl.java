package pms.impl;

import javax.jws.WebService;

import pms.inface.WebServiceInterface;

@WebService
public class WebServiceImpl implements WebServiceInterface{

	@Override
	public String sayBye(String word) {
		return word + "当和这个真实的世界迎面撞上时，你是否找到办法和自己身上的欲望讲和，又该如何理解这个铺面而来的人生？";
	}

}
