package com.example.signingoogle2.Controller;

import com.example.signingoogle2.Config.GoogleDriveConfig;
import com.example.signingoogle2.Entity.User;
import com.example.signingoogle2.Service.UserService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.Photo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UserController {
    @Autowired
    private GoogleAuthorizationCodeFlow flow;

    @Autowired
    private UserService userService;

    private static final String USER_IDENTIFIER_KEY = "MY_DUMMY_USER";
    private static String name = "Unknow";
    public static String email = "Unknow";
    private static String photo = "Unknow";

    @Value("${google.oauth.callback.uri}")
    private String CALLBACK_URI;

    @GetMapping("/login")
    public String login(){
        return "login";
    }


    @GetMapping(value = { "/google_oauth" })
    public void doGoogleSignIn(HttpServletResponse response) throws Exception {
        GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
        String redirectURL = url.setRedirectUri(CALLBACK_URI).setAccessType("offline").build();
        response.sendRedirect(redirectURL);
    }

    @GetMapping(value = { "/oauth" })
    public String saveAuthorizationCode(HttpServletRequest request, HttpSession session, Model model) throws Exception {
        String code = request.getParameter("code");

        if (code != null) {

            User userInfo = saveToken(code);

            model.addAttribute("name", userInfo.getName());
            model.addAttribute("email", userInfo.getEmail());
            model.addAttribute("photoUrl", userInfo.getPhotoUrl());

            return "dashboard";
        }

        return "login";
    }

    private User saveToken(String code) throws Exception {
        GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(CALLBACK_URI).execute();
        Credential credential = flow.createAndStoreCredential(response, USER_IDENTIFIER_KEY);

        // Fetch user information using People API
        PeopleService peopleService = new PeopleService.Builder(GoogleDriveConfig.httpTransport, GoogleDriveConfig.jsonFactory, credential)
                .setApplicationName("Google-OAuth-Demo")
                .build();

        Person profile = peopleService.people().get("people/me")
                .setPersonFields("names,emailAddresses,photos")
                .execute();

        List<Name> names = profile.getNames();
        List<EmailAddress> emailAddresses = profile.getEmailAddresses();
        List<Photo> photos = profile.getPhotos();

        if (names != null && !names.isEmpty()) {
            System.out.println("Name: " + names.get(0).getDisplayName());
            name = names.get(0).getDisplayName();
        }
        if (emailAddresses != null && !emailAddresses.isEmpty()) {
            System.out.println("Email: " + emailAddresses.get(0).getValue());
            email = emailAddresses.get(0).getValue();
        }
        if (photos != null && !photos.isEmpty()) {
            System.out.println("Photo URL: " + photos.get(0).getUrl());
            photo = photos.get(0).getUrl();
        }

        userService.saveUser(name, email, photo);

        return new User(name, email, photo);
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model){

        model.addAttribute("name", name);
        model.addAttribute("email", email);
        model.addAttribute("photoUrl", photo);

        return "dashboard";
    }

    @GetMapping("/search2")
    public String search(Model model){

        model.addAttribute("name", name);
        model.addAttribute("email", email);
        model.addAttribute("photoUrl", photo);

        return "search";
    }

    @PostMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext();
        return "redirect:/";
    }

}
