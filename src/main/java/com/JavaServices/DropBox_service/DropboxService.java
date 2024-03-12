package com.JavaServices.DropBox_service;


import java.io.File;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadErrorException;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class DropboxService {

	// create folder and upload
	public void createFolderAndUploadFolder(String folderPath, String parentFolderName, String ACCESS_TOKEN)
			throws IOException, DbxException {
		DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
		DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

		// Create parent folder if it doesn't exist
		try {
			client.files().createFolderV2("/" + parentFolderName);
		} catch (CreateFolderErrorException e) {
			// Folder already exists or other error occurred
		}

		File folder = new File(folderPath);
		if (folder.isDirectory()) {
			// Create folder within the parent folder
			try {
				client.files().createFolderV2("/" + parentFolderName + "/" + folder.getName());
			} catch (CreateFolderErrorException e) {
				// Folder already exists or other error occurred
			}

			// Upload files from the folder
			File[] files = folder.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isFile()) {
						uploadFile(client, parentFolderName, folder.getName(), file);
					}
				}
			}
		}
	}

	private void uploadFile(DbxClientV2 client, String parentFolderName, String folderName, File file)
			throws IOException, DbxException {
		try (InputStream in = new FileInputStream(file)) {
			client.files().uploadBuilder("/" + parentFolderName + "/" + folderName + "/" + file.getName())
					.uploadAndFinish(in);
		} catch (UploadErrorException e) {
			e.printStackTrace();
		}
	}

// upload file in specific path on drive box

	public String uploadFile1(String folderPath, String localFilePath, String ACCESS_TOKEN)
			throws IOException, DbxException {
		DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
		DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

		// Format folder path
		folderPath = formatPath(folderPath);

		// Check if the folder exists
		if (!checkFolderExists(client, folderPath)) {
			System.out.println("Folder does not exist. File not uploaded.");
			return "Folder does not exist. File not uploaded.";
		}

		String fileName = new File(localFilePath).getName();
		String remoteFilePath = folderPath + "/" + fileName;

		// Check if the file with the same name already exists
		if (checkFileExists(client, remoteFilePath)) {
			System.out.println("File with the same name already exists. File not uploaded.");
			return "File with the same name already exists. File not uploaded.";
		}

		try (InputStream in = new FileInputStream(localFilePath)) {
			FileMetadata metadata = client.files().uploadBuilder(remoteFilePath).uploadAndFinish(in);
			System.out.println("File uploaded successfully.");

			return "File uploaded successfully.";
		} catch (UploadErrorException e) {
			e.printStackTrace();
			return "An error occurred: " + e.getMessage();

		}
	}

	private String formatPath(String path) {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return path;
	}

	private boolean checkFolderExists(DbxClientV2 client, String folderPath) {
		try {
			client.files().getMetadata(folderPath);
			return true; // Folder exists
		} catch (Exception e) {
			return false; // Folder does not exist
		}
	}

	private boolean checkFileExists(DbxClientV2 client, String filePath) {
		try {
			client.files().getMetadata(filePath);
			return true; // File exists
		} catch (Exception e) {
			return false; // File does not exist
		}
	}
	// view the list of files in specific folder path

	public List<String> listFilesInFolder(String folderPath, String ACCESS_TOKEN) {
		DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
		DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
		String remoteFilePath = formatPath(folderPath) + "/";
		List<String> fileList = new ArrayList<>();

		try {
			listFilesRecursive(client, remoteFilePath, fileList);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fileList;
	}

	private void listFilesRecursive(DbxClientV2 client, String folderPath, List<String> fileList) throws Exception {

		ListFolderResult result = client.files().listFolder(folderPath);
		for (com.dropbox.core.v2.files.Metadata metadata : result.getEntries()) {
			if (metadata instanceof com.dropbox.core.v2.files.FileMetadata) {
				fileList.add(metadata.getPathLower());
			} else if (metadata instanceof com.dropbox.core.v2.files.FolderMetadata) {
				String subFolderPath = metadata.getPathLower();
				listFilesRecursive(client, subFolderPath, fileList);
			}
		}
	}

	// delete the file or folder in specific path
	public String deleteFileOrFolder(String folderPath, String ACCESS_TOKEN) {
		DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
		DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
		String path = "/" + folderPath;
		try {
			// Check if the given path is a file or folder
			if (path.startsWith("/")) {
				Metadata metadata = client.files().getMetadata(path);
				if (metadata != null) {
					if (isFile(metadata)) {
						client.files().deleteV2(path);
						return "File deleted successfully.";
					} else {
						deleteFolder(client, path);
						return "Folder deleted successfully.";
					}
				} else {
					return "The file or folder does not exist.";
				}
			} else {
				return "Path should start with '/' character.";
			}
		} catch (Exception e) {
			return "An error occurred: " + e.getMessage();
		}
	}

	private boolean isFile(Metadata metadata) {
		return metadata.getClass().getName().endsWith("FileMetadata");
	}

	private void deleteFolder(DbxClientV2 client, String folderPath) throws Exception {
		client.files().deleteV2(folderPath);
	}

// download the file

	public String downloadFileAsBase64(String folderPath, String ACCESS_TOKEN) {

		DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
		DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

		try {
			String filePath = "/" + folderPath;

			// Check if the given path is a file or folder
			if (filePath.startsWith("/")) {
				FileMetadata metadata = (FileMetadata) client.files().getMetadata(filePath);
				if (metadata != null) {
					try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
							InputStream in = client.files().download(filePath).getInputStream()) {
						IOUtils.copy(in, outputStream);
						byte[] fileContent = outputStream.toByteArray();
						return Base64.getEncoder().encodeToString(fileContent);
					}
				} else {
					return "The file or folder does not exist.";
				}
			} else {
				return "The file or folder does not exist.";
			}

		} catch (Exception e) {
			return "An error occurred: " + e.getMessage();
		}
	}

}

