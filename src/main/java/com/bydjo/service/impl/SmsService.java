package com.bydjo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsService {

    public void sendSms(String to, String message) {
        log.info("====== SMS to {} : {} ======", to, message);
    }
}