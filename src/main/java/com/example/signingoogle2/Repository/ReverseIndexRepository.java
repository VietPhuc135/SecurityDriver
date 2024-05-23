package com.example.signingoogle2.Repository;

import com.example.signingoogle2.Entity.FileUpload;
import com.example.signingoogle2.Entity.ReverseIndex;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReverseIndexRepository extends JpaRepository<ReverseIndex, Long> {
    List<ReverseIndex> findByKeywordContaining(String keyword);

    ReverseIndex findByFileId(String fileId);
    //ReverseIndex findByKeyword(String keyword);
}
