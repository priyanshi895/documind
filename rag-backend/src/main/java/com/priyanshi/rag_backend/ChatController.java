package com.priyanshi.rag_backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/ask")
    public String askQuestion(@RequestBody QuestionRequest request) {
        return chatService.answerQuestion(request.getQuestion());
    }

    public static class QuestionRequest {
        private String question;
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
    }
}
