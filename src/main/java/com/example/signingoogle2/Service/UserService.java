package com.example.signingoogle2.Service;

import com.example.signingoogle2.Entity.User;
import com.example.signingoogle2.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public void saveUser(String name, String email, String photoUrl){
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPhotoUrl(photoUrl);
            userRepository.save(user);
        } else {
            System.out.println("Email already exists in database: " + email);
        }
    }
}
