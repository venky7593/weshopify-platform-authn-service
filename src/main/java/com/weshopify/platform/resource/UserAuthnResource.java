package com.weshopify.platform.resource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weshopify.platform.bean.UserAuthnBean;
import com.weshopify.platform.service.UserAuthnService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class UserAuthnResource {

	UserAuthnService userAuthnService;
	
	UserAuthnResource(UserAuthnService userAuthnService){
		this.userAuthnService = userAuthnService;
	}
	
	@PostMapping(value = {"/users/token"})
	public ResponseEntity<String> authenticate(UserAuthnBean authnBean){
		String authnResp = userAuthnService.authenticate(authnBean);
		return ResponseEntity.ok(authnResp);
	}
	
	@GetMapping(value = {"/users/logout"})
	public ResponseEntity<String> logout(@RequestParam("token_type_hint") String tokenType, 
										@RequestParam("token") String token){
		String logoutResp = userAuthnService.logout(tokenType,token);
		return ResponseEntity.ok(logoutResp);
	}
}
