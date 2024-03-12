package com.JavaServices.GoogleDrive_service;

import java.io.IOException;

import java.io.InputStream;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.ByteArrayOutputStream; 

import java.security.GeneralSecurityException;
import java.util.*;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.model.FileList;

@Service
public class GoogleDriveService3 {
	private static final String APPLICATION_NAME = "Spring Project";
	private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

	private Drive getDriveService(String accessToken) throws IOException, GeneralSecurityException {
		return new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
				getRequestInitializer(accessToken)).setApplicationName(APPLICATION_NAME).build();
	}

	private HttpRequestInitializer getRequestInitializer(String accessToken) {
		return request -> request.getHeaders().setAuthorization("Bearer " + accessToken);
	}

//upload files
	public void uploadFolder(String accessToken, java.io.File localFolder, String folderPath)
			throws IOException, GeneralSecurityException {
		Drive driveService = getDriveService(accessToken);
		createFolderStructure(driveService, null, localFolder, folderPath);
	}

// get all files
	public List<String> listFilesFolder(String accessToken, String folderPath)
			throws IOException, GeneralSecurityException {
		Drive driveService = getDriveService(accessToken);
		String folderId = getFolderId(driveService, folderPath);
 
		List<String> data = printFolderStructure(driveService, folderId, "");
		return data;
	}

	// delete code

	public void deleteFileOrFolder(String accessToken, String id) throws IOException, GeneralSecurityException {
		Drive driveService = getDriveService(accessToken);
 
		deleteFileOrFolder(driveService, id);
	}

	// download code

	public String fileToBase64(String accessToken, String fileId) throws IOException, GeneralSecurityException {
		// Fetch the file content
		Drive driveService = getDriveService(accessToken);

		byte[] fileContent = fetchFileContent(driveService, fileId);

		// Convert file content to base64
		return Base64.getEncoder().encodeToString(fileContent);
	}

	public String getFolderId(Drive driveService, String folderPath) throws IOException {
		String[] folderNames = folderPath.split("/");
		String parentId = "root";

		for (String folderName : folderNames) {
			String folderId = searchFolder(driveService, parentId, folderName);
			if (folderId == null) {
				return null; // Folder not found
			}
			parentId = folderId;
		}

		return parentId; // Return the ID of the last folder in the path
	}

	private String searchFolder(Drive driveService, String parentId, String folderName) throws IOException {
		String pageToken = null;
		String query = "'" + parentId + "' in parents and mimeType='" + FOLDER_MIME_TYPE + "' and trashed=false";

		do {
			FileList result = driveService.files().list().setQ(query).setFields("files(id, name)")
					.setPageToken(pageToken).execute();
			for (File file : result.getFiles()) {
				if (file.getName().equals(folderName)) {
					return file.getId(); // Found the folder, return its ID
				}
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);

		return null; // Folder not found
	}

	private String getFolderIdUpload(Drive driveService, String folderName) throws IOException {
		String pageToken = null;
		do {
			FileList result = driveService.files().list().setQ(
					"mimeType='application/vnd.google-apps.folder' and trashed=false and name='" + folderName + "'")
					.setFields("files(id)").setPageToken(pageToken).execute();
			for (File file : result.getFiles()) {
				return file.getId(); // Found the folder, return its ID
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);
		return null; // Folder not found
	}

	@SuppressWarnings("unused")
	private void createFolderStructure(Drive driveService, String parentFolderId, java.io.File localFolder,
			String folderPath) throws IOException, GeneralSecurityException {
		String[] folders = folderPath.split("/");
		for (String folder : folders) {

			// Check if the folder exists in Google Drive

			String folderId = getFolderIdUpload(driveService, folder);

			if (folderId == null) {
				// If the folder doesn't exist, create it
				File folderMetadata = new File();
				folderMetadata.setName(folder);
				folderMetadata.setMimeType("application/vnd.google-apps.folder");
				if (parentFolderId != null) {
					folderMetadata.setParents(Collections.singletonList(parentFolderId));
				}
				File createdFolder = driveService.files().create(folderMetadata).setFields("id").execute();
				parentFolderId = createdFolder.getId();
			} else {
				// If the folder exists, set it as the parent folder for the next iteration
				parentFolderId = folderId;
			}
		}
		// Upload files into the final folder
		uploadFiles(driveService, parentFolderId, localFolder);
	}

	private void uploadFiles(Drive driveService, String parentFolderId, java.io.File fileToUpload)
			throws IOException, GeneralSecurityException {
		if (fileToUpload.isDirectory()) {
			// If it's a directory, create a folder in Google Drive
			File folderMetadata = new File();
			folderMetadata.setName(fileToUpload.getName());
			folderMetadata.setMimeType("application/vnd.google-apps.folder");
			if (parentFolderId != null) {
				folderMetadata.setParents(Collections.singletonList(parentFolderId));
			}
			File createdFolder = driveService.files().create(folderMetadata).setFields("id").execute();

			// Upload files within the directory recursively
			for (java.io.File file : fileToUpload.listFiles()) {
				uploadFiles(driveService, createdFolder.getId(), file);
			}
		} else {
			File fileMetadata = new File();
			fileMetadata.setName(fileToUpload.getName());
			fileMetadata.setParents(Collections.singletonList(parentFolderId));

			ByteArrayContent mediaContent = new ByteArrayContent("application/octet-stream",
					java.nio.file.Files.readAllBytes(fileToUpload.toPath()));

			driveService.files().create(fileMetadata, mediaContent).setFields("id").execute();

		}
	}

 
	public static List<String> printFolderStructure(Drive service, String folderId, String indent) throws IOException {
		List<String> output = new ArrayList<>();

		// List files in the current folder
		List<File> filesInFolder = listFilesInFolder(service, folderId);
		// Print files in the current folder
		for (File file : filesInFolder) {
			output.add(indent + "- " + file.getName() + " " + "fileID" + " " + file.getId());
		}

		// List subfolders
		List<File> subfolders = listSubfolders(service, folderId);
		// Recursively print files in subfolders
		for (File subfolder : subfolders) {
			output.add(indent + "+ " + subfolder.getName() + " " + "folderID" + " " + subfolder.getId());

			List<String> subfolderOutput = printFolderStructure(service, subfolder.getId(), indent + "  ");
			output.addAll(subfolderOutput);

		}

		return output;
	}

	public static List<File> listFilesInFolder(Drive service, String folderId) throws IOException {
		// List files in the current folder
		List<File> result = service.files().list()
				.setQ("'" + folderId
						+ "' in parents and trashed=false and mimeType != 'application/vnd.google-apps.folder'")
				.execute().getFiles();
		return result;
	}

	public static List<File> listSubfolders(Drive service, String folderId) throws IOException {
		// List subfolders
		List<File> result = service.files().list()
				.setQ("'" + folderId
						+ "' in parents and trashed=false and mimeType = 'application/vnd.google-apps.folder'")
				.execute().getFiles();
		return result;
	}

	// delete file or folder

	private static void deleteFileOrFolder(Drive driveService, String fileId) throws IOException {
		driveService.files().delete(fileId).execute();
	}

	// download file or folder code
	private static byte[] fetchFileContent(Drive driveService, String fileId) throws IOException {
		// Fetch the file metadata
		File fileMetadata = driveService.files().get(fileId).execute();

		// Get input stream for the file content
		try (InputStream inputStream = driveService.files().get(fileId).executeMediaAsInputStream()) {
			// Read the content into a byte array
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			return outputStream.toByteArray();
		}

	}
}
