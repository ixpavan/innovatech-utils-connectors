package com.JavaServices.AmazonS3_service;

import java.util.List;

public interface FileServiceImpl {

	String downloadFile(String passFilePath);

	String deleteFile(String filename, String folderName);

	List<String> listAllFiles(String folderName);

	public void moveFiles(String fileName, String sourceFolder, String destinationFolder);

	public void uploadFolders(String folderLocation);
}
