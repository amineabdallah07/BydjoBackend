package com.bydjo.service.impl;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsService {

    @Value("${app.twilio.account-sid}")
    private String accountSid;

    @Value("${app.twilio.auth-token}")
    private String authToken;

    @Value("${app.twilio.phone-number}")
    private String twilioPhoneNumber;

    @Value("${app.twilio.enabled:false}")
    private boolean enabled;

    @PostConstruct
    public void init() {
        if (enabled) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio SMS service initialized");
        } else {
            log.info("Twilio SMS service disabled (development mode)");
        }
    }

    public void sendSms(String to, String message) {
        if (!enabled) {
            log.info("SMS would be sent to {}: {}", to, message);
            return;
        }

        try {
            Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(twilioPhoneNumber),
                    message
            ).create();
            log.info("SMS sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send SMS");
        }
    }
}
