package com.priyanshi.rag_backend;

import java.util.ArrayList;
import java.util.List;

public class TextChunker {

    // Splits text into overlapping chunks
    public static List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            String chunk = text.substring(start, end).trim();

            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            // Move start forward, but overlap with previous chunk
            start += (chunkSize - overlap);
        }

        return chunks;
    }
}
