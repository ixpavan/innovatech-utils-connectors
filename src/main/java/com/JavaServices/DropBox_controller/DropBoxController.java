package com.JavaServices.DropBox_controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.JavaServices.DropBox_service.DropboxAuth;
import com.JavaServices.DropBox_service.DropboxService;
import com.dropbox.core.DbxException;
@RestController
@RequestMapping("/DropBox")
public class DropBoxController {
	@Autowired
	private DropboxService dropboxService;
	@Autowired
	private DropboxAuth dropboxAuth;

	@GetMapping("/AuthorizationUrl")
	public String getAuthorizationUrl() {
		return dropboxAuth.dropAuthorizationUrl();
	}

	@GetMapping("/AccessCode")
	public String getAccessCode(@RequestParam("passAuthorizationCode") String AuthorizationCode) {
		String accessCode=dropboxAuth.getAccessCode(AuthorizationCode);
		return accessCode; 
	}

	// create folder and uplaod folder
	@PostMapping("/uploadFolderToDropbox")
	public ResponseEntity<String> uploadFolderToDropbox(@RequestParam("localFolderPath") String localFolderPath,
			@RequestParam("createFolderPath") String dropboxFolderPath,
			@RequestParam("accessToken") String accessToken) {

		try {
			dropboxService.createFolderAndUploadFolder(localFolderPath, dropboxFolderPath, accessToken);

			return ResponseEntity.ok("Folder uploaded to Dropbox successfully.");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error occurred while uploading folder to Dropbox.");
		}
	}

	@PostMapping("/uploadFileInSpecificFolder")
	public String uploadFile(@RequestParam("destinationFolderPath") String folderPath,
			@RequestParam("localFilePath") String localFilePath, @RequestParam("accessToken") String accessToken)
			throws IOException, DbxException {
		try {
			String message = dropboxService.uploadFile1(folderPath, localFilePath, accessToken);
			return message;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@GetMapping("/listFilesInFolder")
	public List<String> listFilesInFolder(@RequestParam("destinationFolderPath") String folderPath,
			@RequestParam("accessToken") String accessToken) {
		try {
			List<String> listFiles = dropboxService.listFilesInFolder(folderPath, accessToken);
			return listFiles;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	@GetMapping("/downloadFile")
	public String downloadFileAsBase64(@RequestParam("destinationFilePath") String filePath,
			@RequestParam("accessToken") String accessToken) {
		return dropboxService.downloadFileAsBase64(filePath, accessToken);
	}

	@DeleteMapping("/deleteFileOrFolder")
	public String deleteFileOrFolder(@RequestParam("destinationFolderPath") String folderPath,
			@RequestParam("accessToken") String accessToken) {

		try {
			String response = dropboxService.deleteFileOrFolder(folderPath, accessToken);

			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "The file or folder does not exist";
	}

}
