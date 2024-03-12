package com.JavaServices.GoogleDrive_service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GoogleDriveAuthToken {

	@Value("${googledriveclient_id}")
	private String client_id;
	@Value("${googledrivescope}")

	private String scope;
	@Value("${googledriveaccess_type}")

	private String access_type;
	@Value("${googledriveresponse_type}")

	private String response_type;
	@Value("${googledriveredirect_uri}")

	private String redirect_uri;
	@Value("${googledrivetokenUrl}")

	private String readTokenUrl;
	@Value("${googledriveclient_secret}")

	private String client_secret;
	@Value("${googledrivegrant_type}")

	private String grant_type;
	@Value("${googledriveinclude_granted_scopes}")
	private String include_granted_scopes;
	
	@Value("${googledriveauthorizationUrl}")
	private String authUrl;
	@Value("${googledrivestate}")
	private String state;
	
	
	
	
	
	public String generateAuthToken() {
		// Set up request parameters
		String authorizationUrl = UriComponentsBuilder.fromHttpUrl(authUrl)
				.queryParam("client_id", client_id)
				.queryParam("scope",
						scope)

				.queryParam("access_type", access_type).queryParam("include_granted_scopes",include_granted_scopes)
				.queryParam("response_type", response_type).queryParam("state", state)
				.queryParam("redirect_uri", redirect_uri).build().toUriString();

		// Redirect the user to the authorization URL
		return authorizationUrl;
	}

	public Map<String, String> exchangeCodeForTokens(String code) {
		String tokenUrl = readTokenUrl;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		String requestBody = "code=" + code + "&client_id="
				+ client_id + "&client_secret="
				+ client_secret + "&redirect_uri="+redirect_uri
				+ "&grant_type="+grant_type;

		HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

		ResponseEntity<TokenResponse> response = new RestTemplate().postForEntity(tokenUrl, request,
				TokenResponse.class);

		return response.getBody().toMap();
	}

	private static class TokenResponse {
		private String access_token;
		private String refresh_token;
		private int expires_in;

		public String getAccess_token() {
			return access_token;
		}

		public String getRefresh_token() {
			return refresh_token;
		}

		public int getExpires_in() {
			return expires_in;
		}

		public Map<String, String> toMap() {
			Map<String, String> map = new HashMap<>();
			map.put("access_token", access_token);
			map.put("refresh_token", refresh_token);
			map.put("expires_in", String.valueOf(expires_in));
			return map;
		}
	}

}
