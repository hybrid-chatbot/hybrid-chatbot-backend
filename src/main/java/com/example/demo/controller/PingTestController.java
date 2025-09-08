package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingTestController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
