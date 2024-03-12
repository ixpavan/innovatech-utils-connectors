package com.JavaServices.AmazonS3_service;

import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

@Service
public class S3Service implements FileServiceImpl {

	@Value("${AmazonS3bucketName}")
	private String bucketName;

	private final AmazonS3 s3;

	public S3Service(AmazonS3 s3) {
		this.s3 = s3;
	}

	@Override
	public String downloadFile(String filenPath) {

		// List objects in the bucket with the given prefix (folder)
		ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request().withBucketName(bucketName)
				.withPrefix(filenPath);
		ListObjectsV2Result result = s3.listObjectsV2(listObjectsRequest);

		// Find the file and download its content
		try {
			for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
				if (objectSummary.getKey().equals(filenPath)) {
					S3Object s3Object = s3.getObject(bucketName, objectSummary.getKey());
					return inputStreamToBase64(s3Object.getObjectContent());
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;

	}

	private static String inputStreamToBase64(InputStream inputStream) throws IOException {
		// Read the content into a byte array
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		byte[] fileContent = outputStream.toByteArray();

		// Convert file content to Base64
		return Base64.getEncoder().encodeToString(fileContent);
	}

	@Override
	public String deleteFile(String filename, String folderName) {

		s3.deleteObject(bucketName, folderName + "/" + filename);
		return "File deleted";
	}

	@Override
	public List<String> listAllFiles(String folderName) {

		ListObjectsV2Result listObjectsV2Result = s3.listObjectsV2(bucketName, folderName + "/");
		return listObjectsV2Result.getObjectSummaries().stream().map(S3ObjectSummary::getKey)
				.collect(Collectors.toList());

	}

	@Override
	public void moveFiles(String fileName, String sourceFolderName, String destinationFolderName) {

		try {
			// Copy the object to the destination folder
			CopyObjectRequest copyObjRequest = new CopyObjectRequest(bucketName, sourceFolderName + fileName,
					bucketName, destinationFolderName + fileName);
			s3.copyObject(copyObjRequest);

			// Delete the object from the source folder
			s3.deleteObject(new DeleteObjectRequest(bucketName, sourceFolderName + fileName));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String folderLocation1 = "";

	@Override
	public void uploadFolders(String folderLocation) {
		folderLocation1 = folderLocation;
		uploadFilesInFolder(new File(folderLocation));
	}

	private void uploadFilesInFolder(File folder) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					uploadFilesInFolder(file); // Recursively upload files in subfolders
				} else {
					String fileName = file.getAbsolutePath().replace("\\", "/"); // Convert to Unix-style path
					String key = fileName.substring(fileName.indexOf(folderLocation1) + folderLocation1.length() + 1);
					s3.putObject(new PutObjectRequest(bucketName, key, file));
				}
			}
		}
	}

}
