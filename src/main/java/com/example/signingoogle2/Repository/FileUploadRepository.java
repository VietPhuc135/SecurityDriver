package com.example.signingoogle2.Repository;

import com.example.signingoogle2.Entity.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {
    FileUpload findByFileId(String fileId);
}
