package com.example.signingoogle2.Algorithm;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDFExtractor {
    public static String[] extractPDFImage(MultipartFile file){
        List<String> extractedPDF = new ArrayList<>();
        // Thiết lập biến môi trường TESSDATA_PREFIX
        String tessdataPath = "C:\\Users\\admin\\OneDrive - actvn.edu.vn\\Documents\\FileUploadDrive\\tessdataOCR";
        System.setProperty("TESSDATA_PREFIX", tessdataPath);

        // Sử dụng Tesseract để nhận dạng văn bản
        ITesseract tesseract = new Tesseract();
        // Thiết lập đường dẫn đến tệp ngôn ngữ tiếng Việt
        tesseract.setLanguage("vie");
        tesseract.setDatapath(tessdataPath);

        try{
            // Load PDF vào PDDocument
            PDDocument document = PDDocument.load(file.getInputStream());

            // Kiểm tra xem có văn bản nào được trích xuất từ tài liệu không
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text.trim().isEmpty()) {
                System.out.println("Tệp PDF này có thể được tạo từ các hình ảnh.");

                // Tạo PDFRenderer để render các trang PDF thành hình ảnh
                PDFRenderer pdfRenderer = new PDFRenderer(document);

                // Lặp qua từng trang và trích xuất hình ảnh
                for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                    // Lấy trang PDF
                    PDPage page = document.getPage(pageIndex);

                    // Render trang PDF thành hình ảnh
                    BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 300);

                    // Xử lý hình ảnh trước khi chuyển đổi
                    BufferedImage processedImage = preprocessImage(image);

                    // Nhận dạng văn bản từ hình ảnh
                    String result = tesseract.doOCR(processedImage);
                    //System.out.println("Nội dung: " + result);

                    String fullName = extractFullName(result);
                    String bhytNumber = extractBHYTNumber(result);
                    String benhAn = extractPatient(result);

                    if (!fullName.isEmpty()) {
                        extractedPDF.add(fullName);
                        System.out.println("Họ và tên: " + sanitizeText(fullName));
                    }
                    if (!bhytNumber.isEmpty()) {
                        extractedPDF.add(bhytNumber);
                        System.out.println("Số thẻ BHYT: " + sanitizeText(bhytNumber));
                    }
                    if (!benhAn.isEmpty()) {
                        extractedPDF.add(benhAn);
                        System.out.println("Bệnh án: " + sanitizeText(benhAn));
                    }
                }

            } else {
                System.out.println("Tệp PDF này không được tạo từ các hình ảnh.");

                String fullName = extractFullName(text);
                String bhytNumber = extractBHYTNumber(text);
                String benhAn = extractPatient(text);

                if (!fullName.isEmpty()) {
                    extractedPDF.add(fullName);
                    System.out.println("Họ và tên: " + sanitizeText(fullName));
                }
                if (!bhytNumber.isEmpty()) {
                    extractedPDF.add(bhytNumber);
                    System.out.println("Số thẻ BHYT: " + sanitizeText(bhytNumber));
                }
                if (!benhAn.isEmpty()) {
                    extractedPDF.add(benhAn);
                    System.out.println("Bệnh án: " + sanitizeText(benhAn));
                }
            }

            // Đóng tài liệu PDF
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TesseractException e) {
            throw new RuntimeException(e);
        }

        return extractedPDF.toArray(new String[0]);
    }

    /*public static void main(String[] args) {
        // Đường dẫn tới tệp PDF
        String filePath = "C:\\Users\\admin\\OneDrive - actvn.edu.vn\\Documents\\FileUploadDrive\\patient\\phieu_benh1.pdf";

        // Thiết lập biến môi trường TESSDATA_PREFIX
        String tessdataPath = "C:\\Users\\admin\\OneDrive - actvn.edu.vn\\Documents\\FileUploadDrive\\tessdataOCR";
        System.setProperty("TESSDATA_PREFIX", tessdataPath);

        // Sử dụng Tesseract để nhận dạng văn bản
        ITesseract tesseract = new Tesseract();
        // Thiết lập đường dẫn đến tệp ngôn ngữ tiếng Việt
        tesseract.setLanguage("vie");
        tesseract.setDatapath(tessdataPath);

        try {
            // Load PDF vào PDDocument
            PDDocument document = PDDocument.load(new File(filePath));

            // Kiểm tra xem có văn bản nào được trích xuất từ tài liệu không
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text.trim().isEmpty()) {
                System.out.println("Tệp PDF này có thể được tạo từ các hình ảnh.");

                // Tạo PDFRenderer để render các trang PDF thành hình ảnh
                PDFRenderer pdfRenderer = new PDFRenderer(document);

                // Lặp qua từng trang và trích xuất hình ảnh
                for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                    // Lấy trang PDF
                    PDPage page = document.getPage(pageIndex);

                    // Render trang PDF thành hình ảnh
                    BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 300);

                    // Xử lý hình ảnh trước khi chuyển đổi
                    BufferedImage processedImage = preprocessImage(image);

                    // Nhận dạng văn bản từ hình ảnh
                    String result = tesseract.doOCR(processedImage);
                    //System.out.println("Nội dung: " + result);

                    String fullName = extractFullName(result);
                    String bhytNumber = extractBHYTNumber(result);
                    String benhAn = extractPatient(result);

                    if (!fullName.isEmpty()) {
                        System.out.println("Họ và tên: " + sanitizeText(fullName));
                    }
                    if (!bhytNumber.isEmpty()) {
                        System.out.println("Số thẻ BHYT: " + sanitizeText(bhytNumber));
                    }
                    if (!benhAn.isEmpty()) {
                        System.out.println("Bệnh án: " + sanitizeText(benhAn));
                    }
                }

            } else {
                System.out.println("Tệp PDF này không được tạo từ các hình ảnh.");
            }

            // Đóng tài liệu PDF
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TesseractException e) {
            throw new RuntimeException(e);
        }
    }*/

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

    private static String extractFullName(String text) {
        Pattern pattern = Pattern.compile("1\\. Họ và tên \\(7: hoa\\):\\s*([\\p{L}\\s]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

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
        Pattern pattern = Pattern.compile("(?<=1I\\. LÍ DO VÀO VIỆN:)(.*)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
}
