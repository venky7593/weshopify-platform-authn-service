package com.weshopify.platform.service;

import com.weshopify.platform.bean.UserAuthnBean;

public interface UserAuthnService {

	public String authenticate(UserAuthnBean authnBean);
	public String logout(String tokenType, String token);
	
}
