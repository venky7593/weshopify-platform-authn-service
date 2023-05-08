package com.weshopify.platform.outbound;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import java.util.Base64;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weshopify.platform.model.WSO2User;
import com.weshopify.platform.model.WSO2UserAuthnBean;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@Component
@Slf4j
public class IamAuthnCommunicator {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${weshopify-platform.oauth2.grant_type}")
	private String grant_type;

	@JsonIgnore
	@Value("${weshopify-platform.oauth2.clientId}")
	private String clientId;

	@JsonIgnore
	@Value("${weshopify-platform.oauth2.clientSecret}")
	private String clientSecret;

	@JsonIgnore
	@Value("${weshopify-platform.oauth2.uri}")
	private String tokenUrl;

	@Value("${weshopify-platform.oauth2.scope}")
	private String scope;

	@Value("${weshopify-platform.oauth2.logoutUri}")
	private String logoutUri;
	
	@Value("${weshopify-platform.oauth2.userInfoUri}")
	private String userInfoUri;
	
	@Value("${weshopify-platform.scim2.usersUri}")
	private String usersUrl;

	public String authenticate(WSO2UserAuthnBean authnBean) {
		log.info("uri is:\t" + authnBean);
		String respData = null;
		try {
			authnBean.setGrant_type(grant_type);
			authnBean.setScope(scope);
			String payload = objectMapper.writeValueAsString(authnBean);
			
			HttpHeaders headers = basicAuthHeader(clientId,clientSecret);
			HttpEntity<String> request = prepareJsonRequestBody(headers,payload);
			
			ignoreCertificates();
			
			ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, String.class);
			log.info("status code is:\t" + response.getStatusCode().value());
			if (HttpStatus.OK.value() == response.getStatusCode().value()) {
				respData = response.getBody();
				log.info("response body is:\t" + respData);
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return Optional.ofNullable(respData).get();
	}
	
	public String getUserProfile(String accessToken) {
		userInfoUri = userInfoUri+scope;
		log.info("uri is:\t" + userInfoUri);
		String respData = null;
		WSO2User wso2User = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + accessToken);
			HttpEntity<String> requestBody = new HttpEntity<String>(headers);
			
			ResponseEntity<String> response = restTemplate.exchange(userInfoUri, HttpMethod.GET, requestBody, String.class);
			log.info("status code is:\t" + response.getStatusCode().value());
			if (HttpStatus.OK.value() == response.getStatusCode().value()) {
				respData = response.getBody();
				log.info("response body is:\t" + respData);
				JSONObject json = new JSONObject(respData);
				String userId = json.getString("WeshopifyUserId");
				log.info("user id is:\t"+userId);
				//invoke the users/<id> api to fetch the roles
				String updatedUsersUrl = usersUrl;
				String updatedUrl = updatedUsersUrl+userId;
				//usersUrl = usersUrl+userId;
				HttpHeaders adminHeaders = basicAuthHeader("admin","admin");
				HttpEntity<String> usersRequestBody = new HttpEntity<String>(adminHeaders);
				ResponseEntity<String> usersResp = restTemplate.exchange(updatedUrl, HttpMethod.GET, usersRequestBody, String.class);
				if (HttpStatus.OK.value() == usersResp.getStatusCode().value()) {
					//wso2User = objectMapper.readValue(usersResp.getBody(), WSO2User.class);
					respData = usersResp.getBody();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Optional.ofNullable(respData).get();
	}

	public String logout(String tokenType, String token) {
		log.info("uri is:\t" + logoutUri);
		String respData = null;
		try {

			String payload = "token_type_hint="+tokenType+"&token="+token;
			HttpHeaders headers = basicAuthHeader(clientId,clientSecret);
			
			HttpEntity<String> request = prepareFormRequestBody(headers,payload);
			
			ResponseEntity<String> response = restTemplate.exchange(logoutUri, HttpMethod.POST, request, String.class);
			log.info("status code is:\t" + response.getStatusCode().value());
			if (HttpStatus.OK.value() == response.getStatusCode().value()) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("message", "User has been logged out successfully");
				respData = jsonObj.toString();
				log.info("response body is:\t" + respData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Optional.ofNullable(respData).get();
	}

	private HttpHeaders basicAuthHeader(String username, String password) {
		String adminCreds = username + ":" + password;
		log.info("admin creds are:\t" + adminCreds);

		String encodedAdminCreds = Base64.getEncoder().encodeToString(adminCreds.getBytes());
		log.info("admin creds encoded are:\t" + encodedAdminCreds);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + encodedAdminCreds);

		return headers;
	}

	private HttpEntity<String> prepareJsonRequestBody(HttpHeaders headers, String payload) {
		headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<String> requestBody = new HttpEntity<String>(payload, headers);
		return requestBody;
	}
	
	private HttpEntity<String> prepareFormRequestBody(HttpHeaders headers, String payload) {
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		HttpEntity<String> requestBody = new HttpEntity<String>(payload, headers);
		return requestBody;
	}
	
	private void ignoreCertificates() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };

		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {

		}
	}

}
