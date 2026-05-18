package com.trego.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class TempQueryController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/temp-query")
    public List<Map<String, Object>> executeQuery(@RequestParam String query) {
        return jdbcTemplate.queryForList(query);
    }
}
