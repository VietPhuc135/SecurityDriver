package com.example.signingoogle2.Algorithm;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvertedIndex {
    private TrieNode root;

    public InvertedIndex() {
        root = new TrieNode();
    }

    private static class TrieNode {
        Map<Character, TrieNode> children;
        List<String> fileIDs;

        TrieNode() {
            children = new HashMap<>();
            fileIDs = new ArrayList<>();
        }
    }

    public void add(String keyword, String fileID) {
        TrieNode current = root;
        for (char ch : keyword.toCharArray()) {
            current = current.children.computeIfAbsent(ch, k -> new TrieNode());
        }
        current.fileIDs.add(fileID);
    }

    public Map<String, List<String>> getInvertedIndex() {
        Map<String, List<String>> invertedIndex = new HashMap<>();
        traverse(root, "", invertedIndex);
        return invertedIndex;
    }

    private void traverse(TrieNode node, String currentPrefix, Map<String, List<String>> invertedIndex) {
        if (node.fileIDs.size() > 0) {
            invertedIndex.put(currentPrefix, node.fileIDs);
        }
        for (char ch : node.children.keySet()) {
            traverse(node.children.get(ch), currentPrefix + ch, invertedIndex);
        }
    }

    public static String json(InvertedIndex index){
        // Get and print the inverted index in JSON format
        Map<String, List<String>> invertedIndex = index.getInvertedIndex();
        Gson gson = new Gson();
        String json = gson.toJson(invertedIndex);
        System.out.println(json);
        return json;
    }

    /*public static void main(String[] args) {
        InvertedIndex index = new InvertedIndex();

        // Add keywords and fileIDs to the inverted index
        index.add("apple", 1);
        index.add("banana", 1);
        index.add("apple", 2);
        index.add("cherry", 2);
        index.add("banana", 3);
        index.add("date", 3);

        // Get and print the inverted index in JSON format
        Map<String, List<Integer>> invertedIndex = index.getInvertedIndex();
        Gson gson = new Gson();
        String json = gson.toJson(invertedIndex);
        System.out.println(json);
    }*/
}
