package com.example.signingoogle2.Service;

import com.example.signingoogle2.Entity.ReverseIndex;
import com.example.signingoogle2.Repository.ReverseIndexRepository;
import com.google.api.services.drive.model.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReverseIndexService {

    @Autowired
    private ReverseIndexRepository reverseIndexRepository;

    private static final String FIXED_SALT = "$2a$10$8xZM4TJyftXHgVa9wr3G4O";

    public void addToIndex(String keyword, String fileId) {
            ReverseIndex reverseIndex = new ReverseIndex();
            reverseIndex.setKeyword(keyword);
            reverseIndex.setFileId(fileId);
            reverseIndexRepository.save(reverseIndex);
    }

    public String hashKey(String keyword) {
        //return BCrypt.hashpw(keyword, BCrypt.gensalt());
        return BCrypt.hashpw(keyword, FIXED_SALT);
    }
}
