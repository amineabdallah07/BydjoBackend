package com.bydjo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SmsService {

    @Value("${app.sendzen.api-key}")
    private String apiKey;

    @Value("${app.sendzen.from-number}")
    private String fromNumber;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendSms(String to, String message) {
        String url = "https://api.sendzen.io/v1/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("from", fromNumber);
        body.put("to", to);
        body.put("type", "text");

        Map<String, Object> text = new HashMap<>();
        text.put("body", message);
        text.put("preview_url", false);
        body.put("text", text);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            log.info("WhatsApp message sent to {} via SendZen: {}", to, response.getBody());
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to {}: {}", to, e.getMessage());
            log.info("OTP code for {}: {}", to, message);
        }
    }
}