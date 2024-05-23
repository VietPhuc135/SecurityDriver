package com.example.signingoogle2.Service;

import com.example.signingoogle2.Entity.FileUpload;
import com.example.signingoogle2.Entity.User;
import com.example.signingoogle2.Repository.FileUploadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileUploadService {
    @Autowired
    private FileUploadRepository fileUploadRepository;

    public void uploadFile(String fileName, String fileId, String keyAES, String userId){
        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileName(fileName);
        fileUpload.setFileId(fileId);
        fileUpload.setKeyAES(keyAES);
        fileUpload.setUserId(userId);
        fileUploadRepository.save(fileUpload);
    }
}
