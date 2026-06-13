package com.bydjo.service.impl;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Slf4j
@Service
public class FirebaseService {

    @Value("${app.firebase.service-account-json}")
    private String serviceAccountJson;

    @PostConstruct
    public void init() {
        try {
            if (serviceAccountJson == null || serviceAccountJson.isBlank()) {
                log.warn("FIREBASE_SERVICE_ACCOUNT_JSON not set. Firebase auth disabled.");
                return;
            }
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(com.google.auth.oauth2.GoogleCredentials.fromStream(
                            new ByteArrayInputStream(serviceAccountJson.getBytes())))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage());
        }
    }

    public String verifyAndGetPhone(String idToken) {
        try {
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String phone = (String) decoded.getClaims().get("phone_number");
            if (phone == null) {
                throw new RuntimeException("Firebase token does not contain a phone number");
            }
            return phone;
        } catch (Exception e) {
            log.error("Firebase token verification failed: {}", e.getMessage());
            throw new RuntimeException("Invalid Firebase token");
        }
    }
}
