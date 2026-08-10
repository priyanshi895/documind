package com.priyanshi.rag_backend;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class UploadController {

    @Autowired
    private EmbeddingService embeddingService;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "No file received.";
        }

        try {
            PDDocument document = Loader.loadPDF(file.getBytes());
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();

            List<String> chunks = TextChunker.chunkText(text, 800, 100);

            int savedCount = 0;
            for (String chunk : chunks) {
                embeddingService.embedAndSaveChunk(chunk);
                savedCount++;
            }

            return "Success! Processed " + savedCount + " chunks and saved embeddings to the database.";

        } catch (Exception e) {
            return "Error processing PDF: " + e.getMessage();
        }
    }
}