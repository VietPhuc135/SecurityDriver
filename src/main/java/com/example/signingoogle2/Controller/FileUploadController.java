package com.example.signingoogle2.Controller;

import com.example.signingoogle2.Algorithm.InvertedIndex;
import com.example.signingoogle2.Config.AESEncryptionUtil;
import com.example.signingoogle2.Entity.FileUpload;
import com.example.signingoogle2.Entity.ReverseIndex;
import com.example.signingoogle2.Entity.User;
import com.example.signingoogle2.Repository.FileUploadRepository;
import com.example.signingoogle2.Repository.ReverseIndexRepository;
import com.example.signingoogle2.Repository.UserRepository;
import com.example.signingoogle2.Service.FileUploadService;
import com.example.signingoogle2.Service.ReverseIndexService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
//import org.apache.poi.hwpf.HWPFDocument;
//import org.apache.poi.hwpf.extractor.WordExtractor;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static com.example.signingoogle2.Algorithm.DocumentExtractor.*;
import static com.example.signingoogle2.Algorithm.ImageExtractor.extractImage;
import static com.example.signingoogle2.Algorithm.InvertedIndex.json;
import static com.example.signingoogle2.Algorithm.PDFExtractor.extractPDFImage;
import static com.example.signingoogle2.Algorithm.Search.searchForKeyword;
import static com.example.signingoogle2.Controller.UserController.email;

@Controller
public class FileUploadController {
    private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String USER_IDENTIFIER_KEY = "MY_DUMMY_USER";

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private ReverseIndexService reverseIndexService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReverseIndexRepository reverseIndexRepository;

    @Autowired
    private FileUploadRepository fileUploadRepository;

    @Autowired
    private GoogleAuthorizationCodeFlow flow;


    @PostMapping("/upload")
    public String uploadFileToDrive(@RequestParam("file") MultipartFile[] files, @RequestParam("keyword") String keyword, RedirectAttributes redirectAttributes) throws Exception {
        try {
            InvertedIndex index = new InvertedIndex();

            Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
            Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
                    .setApplicationName("googledrivespringboot").build();

            /*boolean check = false;
            String inverted = "invertedIndex.json";
            List<File> files1 = drive.files().list().setFields("files(name)").execute().getFiles();
            // Kiểm tra tên của từng file xem có trùng với chuỗi "invertedIndex" hay không
            for (File file : files1) {
                //System.out.println("Tên file: " + file.getName());
                if (file.getName().equalsIgnoreCase(inverted)) {
                    check = true;
                }
            }*/

            for(MultipartFile file: files) {

                if (file.isEmpty()) {
                    redirectAttributes.addFlashAttribute("message2", "Please select a file to upload");
                    return "redirect:/dashboard";
                }


                File fileMetadata = new File();
                fileMetadata.setName(file.getOriginalFilename());

                System.out.println("Tên file: " + file.getOriginalFilename());

                String[] arrayWord = null;

                String keyHash = "";

                if (file.getOriginalFilename().endsWith(".docx")){
                    arrayWord = extractFromWord(file);
                } else if (file.getOriginalFilename().endsWith(".pdf")) {
                    arrayWord = extractPDFImage(file);
                } else if (file.getOriginalFilename().endsWith(".doc")) {
                    arrayWord = extractFromDoc(file);
                } else {
                    arrayWord = extractImage(file);
                }


                java.io.File file1 = convertMultipartFileToFile(file);

                Path filePath = Paths.get(file1.getAbsolutePath());

                byte[] bytes = Files.readAllBytes(filePath);

                SecretKey encodedKey = AESEncryptionUtil.generateRandomAESKey();

                byte[] encryptedBytes = AESEncryptionUtil.encrypt(bytes, encodedKey);
                ByteArrayContent mediaContent = new ByteArrayContent("application/octet-stream", encryptedBytes);

                File uploadedFile = drive.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();


                String fileName = fileMetadata.getName();
                String fileId = uploadedFile.getId();
                String keyAES = Base64.getEncoder().encodeToString(encodedKey.getEncoded());
                User user = userRepository.findByEmail(email);

                fileUploadService.uploadFile(fileName, fileId, keyAES, String.valueOf(user.getId()));

                List<String> indexMySQL = new ArrayList<>();

                if (arrayWord != null){
                    for (String str : arrayWord) {
                        keyHash = reverseIndexService.hashKey(str);
                        index.add(keyHash, fileId);
                        System.out.println("Các giá trị: " + keyHash);

                        indexMySQL.add(keyHash);
                    }
                } else {
                    System.out.println("Không có giá trị trả về");
                }


                boolean checkKey = false;

                // Kiểm tra chuỗi có chứa dấu phẩy hay không
                if (keyword.contains(",")) {
                    // Chuyển chuỗi thành mảng
                    String[] stringArray = keyword.split(",");

                    // In ra từng phần tử của mảng
                    for (String str : stringArray) {
                        for (String str2 : arrayWord) {
                            if (str.equalsIgnoreCase(str2)){
                                checkKey = true;
                            }
                        }
                        if (!checkKey){
                            keyHash = reverseIndexService.hashKey(str);
                            index.add(keyHash, fileId);
                            indexMySQL.add(keyHash);
                        }
                        checkKey = false;
                    }
                } else {
                    for (String str2 : arrayWord) {
                        if (keyword.equalsIgnoreCase(str2)){
                            checkKey = true;
                        }
                    }
                    if (!checkKey){
                        keyHash = reverseIndexService.hashKey(keyword);
                        index.add(keyHash, fileId);

                        indexMySQL.add(keyHash);
                    }
                    checkKey = false;
//                    keyHash = reverseIndexService.hashKey(keyword);
//                    index.add(keyHash, fileId);
                }

                reverseIndexService.addToIndex(indexMySQL.toString(), fileId);

                // Return file ID
                redirectAttributes.addFlashAttribute("message2", "Files uploaded successfully");
            }

            /*if (check){
                String jsonString = json(index);
                List<ReverseIndex> reverseIndexs = reverseIndexRepository.findByKeywordContaining(inverted);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                drive.files().get(reverseIndexs.get(0).getFileId()).executeMediaAndDownloadTo(outputStream);

                String currentContent = outputStream.toString(StandardCharsets.UTF_8.name());
                currentContent += "\n" + jsonString;

                byte[] bytes = currentContent.getBytes(StandardCharsets.UTF_8);

                File fileMetadata = new File();
                fileMetadata.setName("invertedIndex.json");

                ByteArrayContent mediaContent3 = new ByteArrayContent("application/json", bytes);
                drive.files().update(reverseIndexs.get(0).getFileId(), fileMetadata, mediaContent3).execute();
            }else {
                String jsonString = json(index);
                // Convert the JSON string to a File
                java.io.File fileContent = new java.io.File("invertedIndex.json");
                java.nio.file.Files.write(fileContent.toPath(), Collections.singleton(jsonString));
                FileContent mediaContent2 = new FileContent("application/json", fileContent);
                // Upload file to Google Drive
                File fileMetadata1 = new File();
                fileMetadata1.setName("invertedIndex.json");
                File file2 = drive.files().create(fileMetadata1, mediaContent2)
                        .setFields("id")
                        .execute();

                reverseIndexService.addToIndex(fileMetadata1.getName(), file2.getId());

                // Delete temporary file
                fileContent.delete();
            }*/
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message2", "An error occurred during upload " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    private java.io.File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        java.io.File file = java.io.File.createTempFile("temp", null);
        try (InputStream inputStream = multipartFile.getInputStream()) {
            org.apache.commons.io.IOUtils.copy(inputStream, new java.io.FileOutputStream(file));
        }
        return file;
    }

    @GetMapping("/download")
    public String downloadFile(@RequestParam("fileId") String fileId, RedirectAttributes redirectAttributes) throws Exception {
        try {
            FileUpload fileUpload = fileUploadRepository.findByFileId(fileId);
            String keyAES = fileUpload.getKeyAES();

            // Giải mã chuỗi Base64 thành một mảng byte
            byte[] decodedKeyBytes = Base64.getDecoder().decode(keyAES);

            // Tạo một đối tượng SecretKeySpec từ mảng byte đã giải mã
            SecretKeySpec secretKeySpec = new SecretKeySpec(decodedKeyBytes, "AES");

            String downloadDir = System.getProperty("user.home") + "/" + "Downloads";
            String destinationPath = downloadDir + "/" + fileUpload.getFileName();

            // Download file from Google Drive
            java.io.File downloadedFile = downloadFileFromDrive(fileId);

            // Read downloaded file content into byte array
            byte[] encryptedBytes = Files.readAllBytes(Paths.get(downloadedFile.getAbsolutePath()));

            // Decrypt file content
            byte[] decryptedBytes = AESEncryptionUtil.decrypt(encryptedBytes, (SecretKey) secretKeySpec);

            // Write decrypted content to local file
            try (OutputStream outputStream = new FileOutputStream(destinationPath)) {
                outputStream.write(decryptedBytes);
            }

            // Delete the downloaded file
            downloadedFile.delete();

            redirectAttributes.addFlashAttribute("message1", "Download file success");
            return "redirect:/search2";
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message1", "Download file fail");
            return "redirect:/search2";
        }
    }
    private java.io.File downloadFileFromDrive(String fileId) throws Exception {
        // Call Drive API to download file
        Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
                .setApplicationName("googledrivespringboot").build();
        java.io.File downloadedFile = java.io.File.createTempFile("temp", ".encrypted");
        OutputStream outputStream = new FileOutputStream(downloadedFile);
        drive.files().get(fileId).executeMediaAndDownloadTo(outputStream);
        outputStream.close();
        return downloadedFile;
    }

    @GetMapping("/success")
    public String success() {
        return "success";
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }

    @PostMapping("/search")
    public String searchUser(@RequestParam("keywordSearch") String keywordSearch, RedirectAttributes redirectAttributes) throws IOException {
        Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
                .setApplicationName("googledrivespringboot").build();

        try {
            //tim kiem tren mysql
            String keyHash = reverseIndexService.hashKey(keywordSearch);
            List<ReverseIndex> reverseIndexs = reverseIndexRepository.findByKeywordContaining(keyHash);
            List<FileUpload> fileUploads = new ArrayList<>();
            List<String> reverseFiles = new ArrayList<>();
            for (ReverseIndex reverseIndex: reverseIndexs){
                System.out.println("FileID: " + reverseIndex.getFileId());
                reverseFiles.add(reverseIndex.getFileId());
            }

            //tim kiem tren drive
            /*String downloadDir = System.getProperty("user.home") + "/Downloads/invertedIndex.json";

            java.io.File file = new java.io.File(downloadDir); // Đường dẫn đến file JSON

            if (!file.exists()) {
                System.out.println("File '" + downloadDir + "' không tồn tại.");
            }*/

            //danh sach tat ca fileId tren Drive
            List<String> fileIds2 = new ArrayList<>();

            String pageToken = null;
            do {
                com.google.api.services.drive.model.FileList result = drive.files().list()
                        .setFields("nextPageToken, files(id)")
                        .setPageToken(pageToken)
                        .execute();
                for (File file1 : result.getFiles()) {
                    fileIds2.add(file1.getId());
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);

            //kiem tra fileId trong file invertedIndex.json co trong drive khong
            List<String> addfileIds = new ArrayList<>();
            boolean checkId = false;

            /*String content = readJsonFile(file);
            String keyHash = reverseIndexService.hashKey(keywordSearch);
            List<String> fileIds = searchForKeyword(content, keyHash);

            for (String fileId: fileIds){
                for (String fileId3: fileIds2){
                    if (fileId.equalsIgnoreCase(fileId3)){
                        checkId = true;
                    }
                }
                if (checkId){
                    addfileIds.add(fileId);
                }
                checkId = false;
            }*/

            for (String reverseFile: reverseFiles){
                for (String fileId3: fileIds2){
                    if (reverseFile.equalsIgnoreCase(fileId3)){
                        checkId = true;
                    }
                }
                if (checkId){
                    addfileIds.add(reverseFile);
                }
                checkId = false;
            }

            for (String fileId: addfileIds){
                fileUploads.add(fileUploadRepository.findByFileId(fileId));
            }

            if (fileUploads != null && !fileUploads.isEmpty()) {
                System.out.println("Tìm thấy từ khóa '" + keyHash + "' với các giá trị fileId: " + addfileIds);
                redirectAttributes.addFlashAttribute("messages", fileUploads);
            } else {
                System.out.println("Không tìm thấy từ khóa '" + keyHash + "'");
                redirectAttributes.addFlashAttribute("message1", "Not found file");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/search2";
    }

    @PostMapping("/share")
    public String handleFileId(@RequestParam String fileId, Model model) {
        model.addAttribute("message", fileId);
        return "redirect:/share";
    }

    @GetMapping("/share/{fileId}")
    public String showSharePage(@PathVariable String fileId, Model model) {
        model.addAttribute("fileId", fileId);
        return "share";
    }

    @PostMapping("/shareFile")
    public String shareFile(@RequestParam("fileId") String fileId, @RequestParam("email") String email, RedirectAttributes redirectAttributes) throws Exception {
        Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
                .setApplicationName("googledrivespringboot").build();

        try {
            // Tạo một đối tượng Permission
            Permission newPermission = new Permission();
            newPermission.setType("user");
            newPermission.setRole("writer");
            newPermission.setEmailAddress(email);

            // Gửi yêu cầu để chia sẻ file với người dùng có địa chỉ email đã chỉ định
            drive.permissions().create(fileId, newPermission).execute();

            //modelAndView.addObject("message", "File shared successfully!");
            redirectAttributes.addFlashAttribute("message1", "File shared successfully");
        } catch (Exception e) {
            //modelAndView.addObject("message", "Error sharing file: " + e.getMessage());
            redirectAttributes.addFlashAttribute("message1", "Error sharing file");
        }
        return "redirect:/search2";
    }

    @DeleteMapping("/delete/{fileId}")
    public String deleteFile(@PathVariable String fileId) {
        try {
            Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
            Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
                    .setApplicationName("googledrivespringboot").build();
            // Kiểm tra file có tồn tại không
            Drive.Files.List request = drive.files().list().setQ("trashed=false");
            FileList files = request.execute();
            List<File> fileItems = files.getFiles();

            boolean fileExists = fileItems.stream().anyMatch(file -> file.getId().equals(fileId));

            ReverseIndex reverseIndex = reverseIndexRepository.findByFileId(fileId);
            FileUpload fileUpload = fileUploadRepository.findByFileId(fileId);

            if (fileExists) {
                drive.files().delete(fileId).execute();
                System.out.println("File deleted successfully");

                // Xóa file trong MySQL
                reverseIndexRepository.deleteById(reverseIndex.getId());
                fileUploadRepository.deleteById(fileUpload.getId());
                System.out.println("File record deleted successfully from MySQL");

            } else {
                System.out.println("File not found");
            }
            return "redirect:/search2";
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to delete file");
            return "redirect:/search2";
        }
    }
}
