package com.example.signingoogle2.Algorithm;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.web.multipart.MultipartFile;
import vn.pipeline.Annotation;
import vn.pipeline.VnCoreNLP;
import vn.pipeline.Word;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageExtractor {
    public static String[] extractImage(MultipartFile file) {
        List<String> extractedImage = new ArrayList<>();
        // Thiết lập biến môi trường TESSDATA_PREFIX
        String tessdataPath = "C:\\Users\\admin\\OneDrive - actvn.edu.vn\\Documents\\FileUploadDrive\\tessdataOCR";
        System.setProperty("TESSDATA_PREFIX", tessdataPath);

        // Sử dụng Tesseract để nhận dạng văn bản
        ITesseract tesseract = new Tesseract();
        // Thiết lập đường dẫn đến tệp ngôn ngữ tiếng Việt
        tesseract.setLanguage("vie");
        tesseract.setDatapath(tessdataPath);

        try {
            // Đọc hình ảnh từ tệp
            BufferedImage image = ImageIO.read(file.getInputStream());

            // Xử lý hình ảnh trước khi chuyển đổi
            BufferedImage processedImage = preprocessImage(image);

            // Nhận dạng văn bản từ hình ảnh
            String result = tesseract.doOCR(processedImage);

            // In kết quả nhận dạng văn bản
            //System.out.println("Kết quả nhận dạng văn bản:");
            //System.out.println(result);

            // Xử lý kết quả nhận dạng để trích xuất thông tin cần thiết
            String fullName = performNLP(result);
            String bhytNumber = extractBHYTNumber(result);
            String benhAn = extractPatient(result);

            if (!fullName.isEmpty()) {
                extractedImage.add(fullName);
                System.out.println("Họ và tên: " + sanitizeText(fullName));
            }
            if (!bhytNumber.isEmpty()) {
                extractedImage.add(bhytNumber);
                System.out.println("Số thẻ BHYT: " + sanitizeText(bhytNumber));
            }
            if (!benhAn.isEmpty()) {
                extractedImage.add(benhAn);
                System.out.println("Bệnh án: " + sanitizeText(benhAn));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TesseractException e) {
            throw new RuntimeException(e);
        }

        return extractedImage.toArray(new String[0]);
    }

    private static BufferedImage preprocessImage(BufferedImage image) {
        // Scale hình ảnh để giảm kích thước (nếu cần)
        int targetWidth = 800; // Độ rộng mục tiêu của hình ảnh
        int targetHeight = (int) ((double) image.getHeight() / image.getWidth() * targetWidth);
        Image scaledImage = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);

        // Chuyển đổi Image thành BufferedImage
        BufferedImage bufferedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(scaledImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return bufferedImage;
    }

    private static String performNLP(String text) {
        try {
            // Cấu hình VNCoreNLP với các annotator cần thiết
            String[] annotators = {"wseg", "pos", "ner"};
            VnCoreNLP pipeline = new VnCoreNLP(annotators);

            // Tạo annotation cho văn bản
            Annotation annotation = new Annotation(text);

            // Chạy pipeline trên văn bản
            pipeline.annotate(annotation);

            // Duyệt qua các từ trong văn bản
            List<Word> words = annotation.getWords();
            boolean foundColon = false; // Biến kiểm tra dấu " :"
            for (Word word : words) {
                String form = word.getForm();
                String ner = word.getNerLabel();

                // Kiểm tra nếu từ hiện tại là dấu " :"
                if (form.equals("tên")) {
                    foundColon = true;
                    continue; // Bỏ qua dấu ":" và tiếp tục vòng lặp
                }

                // Nếu đã gặp dấu ":" và thực thể hiện tại là "PER"
                if (foundColon && ("B-PER".equals(ner) || "I-PER".equals(ner))) {
                    return form.replace("_", " ");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

//    private static String extractFullName(String text) {
//        Pattern pattern = Pattern.compile("1\\. Họ và tên \\(7n hoø\\);\\s*([\\p{L}\\s]+)");
//        Matcher matcher = pattern.matcher(text);
//        if (matcher.find()) {
//            return matcher.group(1).trim();
//        }
//        return "";
//    }

    private static String extractBHYTNumber(String text) {
        Pattern pattern = Pattern.compile("Số thẻ BHYT: (\\d+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    private static String sanitizeText(String text) {
        // Loại bỏ các ký tự không cần thiết
        return text.replaceAll("[,\\.]", "").trim();
    }

    private static String extractPatient(String text) {
        Pattern pattern = Pattern.compile("(?<=II\\. LÍ DO VÀO VIỆN:)(.*)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
}
