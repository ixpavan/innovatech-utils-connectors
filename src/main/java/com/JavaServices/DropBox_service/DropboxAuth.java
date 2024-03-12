package com.JavaServices.DropBox_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class DropboxAuth {
	@Autowired
	private RestTemplate restTemplate;

	@Value("${DropBoxclient_id}")
	private String clientId;
	@Value("${DropBoxredirectUri}")
	private String redirectUri;
	@Value("${DropBoxauthorizationUrl}")
	private String authorizationUrl;
	@Value("${DropBoxresponse_type}")
	private String response_type;
	@Value("${DropBoxgrant_type}")
	private String grant_type;
	@Value("${DropBoxclient_secret}")
	private String client_secret;
	@Value("${DropBoxaccessTokenUrl}")

	private String accessTokenUrl;

	public String dropAuthorizationUrl() {
		String authorizationUrlReturn = authorizationUrl + "client_id=" + clientId + "&response_type=" + response_type
				+ "&redirect_uri=" + redirectUri;
		return authorizationUrlReturn;
	}

	public String getAccessCode(String code) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("code", code);
		map.add("grant_type", grant_type);
		map.add("client_id", clientId);
		map.add("client_secret", client_secret);
		map.add("redirect_uri", redirectUri);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(accessTokenUrl, request, String.class);
		// Parse the response and extract the access token
		String accessToken = response.getBody();
		return "Access token: " + accessToken;
	}

}
