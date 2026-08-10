package com.priyanshi.rag_backend;

import jakarta.persistence.*;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    // We'll store the embedding as a String temporarily (comma-separated numbers)
    // and let PostgreSQL handle the vector conversion
    @Column(columnDefinition = "vector(768)")
    private String embedding;

    public DocumentChunk() {}

    public DocumentChunk(String content, String embedding) {
        this.content = content;
        this.embedding = embedding;
    }

    public Long getId() { return id; }
    public String getContent() { return content; }
    public String getEmbedding() { return embedding; }
    public void setContent(String content) { this.content = content; }
    public void setEmbedding(String embedding) { this.embedding = embedding; }
}
