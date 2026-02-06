package com.example.demo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    private final JdbcTemplate jdbcTemplate;

    public MessageService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert_user_message_in_db(String username, String message) {
        jdbcTemplate.update(
            "INSERT INTO messages (username, message) VALUES (?, ?)",
            username, message
        );
    }
}
