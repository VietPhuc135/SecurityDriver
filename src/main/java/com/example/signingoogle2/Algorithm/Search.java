package com.example.signingoogle2.Algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Search {
    public static List<String> searchForKeyword(String text, String keyword) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(keyword) + "\":\\s*\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(text);
        List<String> fileIds = new ArrayList<>();

        while (matcher.find()) {
            String[] ids = matcher.group(1).split(",");
            for (String id : ids) {
                String cleanedId = id.replaceAll("\"", "").trim();
                if (!cleanedId.isEmpty()) {
                    fileIds.add(cleanedId);
                }
            }
        }

        // Loại bỏ dấu ngoặc vuông [] từ giá trị fileId nếu chỉ có một giá trị
        if (fileIds.size() == 1) {
            String fileId = fileIds.get(0);
            if (fileId.startsWith("[") && fileId.endsWith("]")) {
                fileId = fileId.replaceAll("\\[", "").replaceAll("\\]", "");
                fileIds.set(0, fileId);
            }
        }

        return fileIds.isEmpty() ? null : fileIds;
    }

//    public static List<String> searchForKeyword(Trie trie, String text) {
//        List<String> foundKeywords = new ArrayList<>();
//        for (Emit emit : trie.parseText(text)) {
//            foundKeywords.add(emit.getKeyword());
//        }
//        return foundKeywords;
//    }
//
//    public static Trie buildTrie(String[] keywords) {
//        TrieConfig trieConfig = TrieConfig.builder()
//                .withPreserveCase(PRESERVE_CASE.NO)
//                .withOnlyWholeWordsWhiteSpaceSeparated(true)
//                .withTransitionsMethod(TRANSITION_METHOD.FAST)
//                .build();
//
//        Trie trie = new Trie(trieConfig);
//        for (String keyword : keywords) {
//            trie.addKeyword(keyword);
//        }
//        return trie;
//    }

}
