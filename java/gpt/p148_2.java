package com.example.demo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageRepository {

    private final JdbcTemplate jdbc;

    public MessageRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert_user_message_in_db(String username, String message) {
        jdbc.update("INSERT INTO messages(username, message) VALUES (?, ?)", username, message);
    }

    public List<Message> fetchLatest(int limit) {
        return jdbc.query(
            "SELECT id, username, message, created_at FROM messages ORDER BY id DESC LIMIT ?",
            (rs, rowNum) -> new Message(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("message"),
                rs.getTimestamp("created_at").toString()
            ),
            limit
        );
    }
}
