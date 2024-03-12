package com.JavaServices.AmazonS3_controller;

//import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.JavaServices.AmazonS3_service.S3Service;

@RestController
@RequestMapping("/AmazonS3")
public class AmazonS3Controller {
	@Autowired
    private S3Service s3Service;;

  

    @PostMapping("uploadFolder")
    public void uploadFolders(@RequestParam("folderLocation")String folderLocation) {
    	s3Service.uploadFolders(folderLocation);
    }
    
    @GetMapping("download")
    public  String download(@RequestParam("passFilePath")String filePath){
 
      String base64Content  = s3Service.downloadFile(filePath);
        return  base64Content ;
    }


    @DeleteMapping("/{folderName}/{filename}")
    public  String deleteFile(@PathVariable("folderName")String folderName,@PathVariable("filename") String filename){
       return s3Service.deleteFile(filename,folderName);
    }

    @GetMapping("list/{folderName}")
    public List<String> getAllFiles(@PathVariable("folderName")String folderName){

        return s3Service.listAllFiles(folderName);

    }
    
    
    @PostMapping("/moveFile")
    public void moveFile(
            @RequestParam("sourceFolder") String sourceFolder,
            @RequestParam("destinationFolder") String destinationFolder,
            @RequestParam("fileName") String fileName) {

        s3Service.moveFiles(fileName,sourceFolder,destinationFolder);
    } 
}
