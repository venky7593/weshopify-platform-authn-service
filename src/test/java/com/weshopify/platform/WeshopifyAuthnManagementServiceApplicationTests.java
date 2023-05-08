package com.weshopify.platform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.weshopify.platform.model.WSO2UserAuthnBean;
import com.weshopify.platform.outbound.IamAuthnCommunicator;

@SpringBootTest
class WeshopifyAuthnManagementServiceApplicationTests {

	@Autowired
	private IamAuthnCommunicator authnComm;
	
	@Test
	void contextLoads() {
	}
	
	@Test
	public void testAuthentication() {
		WSO2UserAuthnBean userAuthnBean =  WSO2UserAuthnBean.builder()
											.username("weshopify-admin").password("Adance123$").build();
		authnComm.authenticate(userAuthnBean);
	}

}
