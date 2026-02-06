// UnsubscribeService.java

package com.example.unsubscribe;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class UnsubscribeService {
    private final JdbcTemplate jdbc;

    public UnsubscribeService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String unsubscribe(String email) {
        String normalized = email.trim().toLowerCase();

        Integer exists = jdbc.queryForObject(
            "SELECT COUNT(*) FROM newsletter_subscribers WHERE email = ?",
            Integer.class,
            normalized
        );

        if (exists == null || exists == 0) {
            return "That email is not in our database.";
        }

        jdbc.update("DELETE FROM newsletter_subscribers WHERE email = ?", normalized);
        return "You have been unsubscribed.";
    }
}
