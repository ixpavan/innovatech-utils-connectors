package com.JavaServices.GoodgelDrive_controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.JavaServices.GoogleDrive_service.GoogleDriveAuthToken;
import com.JavaServices.GoogleDrive_service.GoogleDriveService3;

@RestController

@RequestMapping("/GoogleDrive")
public class GoogleDriveController {
	@Autowired
	private GoogleDriveService3 googleDriveUploader;
	@Autowired
	private GoogleDriveAuthToken googleDriveAuthToken;

	@GetMapping("/auth_token")
	public String getAccessToken() {
		String accessToken = googleDriveAuthToken.generateAuthToken();
		return accessToken;
	}

	@GetMapping("/access_tokens")
	public ResponseEntity<Map<String, String>> handleGoogleCallback(@RequestParam("Authenticationcode") String code) {
		Map<String, String> tokens = googleDriveAuthToken.exchangeCodeForTokens(code);
		return ResponseEntity.ok(tokens);
	}

	@PostMapping("/uploadFolder")
	public String uploadFolderToDrive(@RequestParam("accessToken") String accessToken,
			@RequestParam("folderPathInDrive") String folderPathInDrive,
			@RequestParam("localFolderPath") String localFolderPath) {

		try {
			java.io.File localFolder = new java.io.File(localFolderPath);
			googleDriveUploader.uploadFolder(accessToken, localFolder, folderPathInDrive);
			return "Files uploaded successfully to Google Drive.";
		} catch (Exception e) {
			e.printStackTrace();
			return "Failed to upload files to Google Drive: " + e.getMessage();
		}
	}

	@GetMapping("/listFiles")
	public List<String> listFilesInFolder(@RequestParam("accessToken") String accessToken,
			@RequestParam("folderPathInDrive") String folderPathInDrive) {

		try {
			List<String> data = googleDriveUploader.listFilesFolder(accessToken, folderPathInDrive);
			return data;

		} catch (Exception e) {
			e.printStackTrace();

		}
		return null;

	}

	@DeleteMapping("/delete")
	public String deleteFileOrFolder(@RequestParam("accessToken") String accessToken, @RequestParam("id") String id) {

		try {
			googleDriveUploader.deleteFileOrFolder(accessToken, id);
			return "delete Success";

		} catch (Exception e) {
			e.printStackTrace();
			return "Failed to Delete";
		}
	}

	@GetMapping("/download")
	public String downloadFileOrFolder(@RequestParam("accessToken") String accessToken, @RequestParam("id") String id) {

		try {
			String base64 = googleDriveUploader.fileToBase64(accessToken, id);
			return base64;

		} catch (Exception e) {
			e.printStackTrace();
			return "not generated base64";
		}

	}
}
