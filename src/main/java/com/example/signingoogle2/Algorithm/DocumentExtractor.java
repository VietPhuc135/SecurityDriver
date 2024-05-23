package com.example.signingoogle2.Algorithm;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentExtractor {

//    public static void main(String[] args) {
//        extractFromWord("C:\\Users\\admin\\Downloads\\example.docx");
//        extractFromPDF("C:\\Users\\admin\\Downloads\\example.pdf");
//        extractFromDoc("C:\\Users\\admin\\OneDrive - actvn.edu.vn\\Documents\\FileUploadDrive\\patient\\patient1.doc");
//    }

    public static String[] extractFromWord(MultipartFile file) {
        List<String> extractedWord = new ArrayList<>();

        try {
            XWPFDocument document = new XWPFDocument(file.getInputStream());

            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();

                String fullName = extractFullName(text);
                String bhytNumber = extractBHYTNumber(text);
                String benhAn = extractPatient(text);

                if (!fullName.isEmpty()) {
                    extractedWord.add(fullName);
                    System.out.println("Họ và tên: " + sanitizeText(fullName));
                }
                if (!bhytNumber.isEmpty()) {
                    extractedWord.add(bhytNumber);
                    System.out.println("Số thẻ BHYT: " + sanitizeText(bhytNumber));
                }
                if (!benhAn.isEmpty()) {
                    extractedWord.add(benhAn);
                    System.out.println("Bệnh án: " + sanitizeText(benhAn));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return extractedWord.toArray(new String[0]);
    }

//    public static String[] extractFromPDF(MultipartFile file) {
//        List<String> extractedPDF = new ArrayList<>();
//
//        try {
//            PDDocument document = PDDocument.load(file.getInputStream());
//            PDFTextStripper pdfStripper = new PDFTextStripper();
//            String text = pdfStripper.getText(document);
//
//            String fullName = extractFullName(text);
//            String bhytNumber = extractBHYTNumber(text);
//            String benhAn = extractPatient(text);
//
//            if (!fullName.isEmpty()) {
//                extractedPDF.add(fullName);
//                System.out.println("Họ và tên: " + sanitizeText(fullName));
//            }
//            if (!bhytNumber.isEmpty()) {
//                extractedPDF.add(bhytNumber);
//                System.out.println("Số thẻ BHYT: " + sanitizeText(bhytNumber));
//            }
//            if (!benhAn.isEmpty()) {
//                extractedPDF.add(benhAn);
//                System.out.println("Bệnh án: " + sanitizeText(benhAn));
//            }
//
//            document.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return extractedPDF.toArray(new String[0]);
//    }

    public static String[] extractFromDoc(MultipartFile file) {
        List<String> extractedDoc = new ArrayList<>();

        try {
            //FileInputStream fis = new FileInputStream(new File(filePath));
            HWPFDocument document = new HWPFDocument(file.getInputStream());
            Range range = document.getRange();
            String text = range.text();

            String fullName = extractFullName(text);
            String bhytNumber = extractBHYTNumber(text);
            String benhAn = extractPatient(text);

            if (!fullName.isEmpty()) {
                extractedDoc.add(fullName);
                System.out.println("Họ và tên: " + sanitizeText(fullName));
            }
            if (!bhytNumber.isEmpty()) {
                extractedDoc.add(bhytNumber);
                System.out.println("Số thẻ BHYT: " + sanitizeText(bhytNumber));
            }
            if (!benhAn.isEmpty()) {
                extractedDoc.add(benhAn);
                System.out.println("Bệnh án: " + sanitizeText(benhAn));
            }

            //fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return extractedDoc.toArray(new String[0]);
    }

    public static String extractName(String text) {
        Pattern pattern = Pattern.compile("Họ tên:\\s*([\\p{L}\\s]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    public static String extractDateOfBirth(String text) {
        Pattern pattern = Pattern.compile("Ngày sinh:\\s*\\.\\s*(\\d{1,2}/\\d{1,2}/\\d{4})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    public static String extractFullName(String text) {
        Pattern pattern = Pattern.compile("1\\. Họ và tên \\(In hoa\\):\\s*([\\p{L}\\s]+)");
        Pattern pattern2 = Pattern.compile("Họ tên:\\s*([\\p{L}\\s]+)");
        Matcher matcher = pattern.matcher(text);
        Matcher matcher2 = pattern2.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }else if (matcher2.find()){
            return matcher2.group(1).trim();
        }
        return "";
    }

    public static String extractBHYTNumber(String text) {
        Pattern pattern = Pattern.compile("10\\. BHYT giá trị đến ngày .*? Số thẻ BHYT: (\\d+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    public static String extractPatient(String text) {
        Pattern pattern = Pattern.compile("Bệnh chính:\\s*([\\p{L}\\s]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    public static String sanitizeText(String text) {
        // Loại bỏ các ký tự không cần thiết
        return text.replaceAll("[,\\.]", "").trim();
    }
}
