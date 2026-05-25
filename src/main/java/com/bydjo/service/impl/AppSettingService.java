package com.bydjo.service.impl;

import com.bydjo.entity.AppSetting;
import com.bydjo.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FIX Bug #14 + L4: Store settings persisted in DB.
 * Delivery fees, store info — all configurable by admin without redeploy.
 */
@Service
@RequiredArgsConstructor
public class AppSettingService {

    private final AppSettingRepository settingRepository;

    public Map<String, String> getAllSettings() {
        return settingRepository.findAll().stream()
                .collect(Collectors.toMap(AppSetting::getKey, s -> s.getValue() != null ? s.getValue() : ""));
    }

    public String get(String key, String defaultValue) {
        return settingRepository.findByKey(key)
                .map(AppSetting::getValue)
                .filter(v -> v != null && !v.isBlank())
                .orElse(defaultValue);
    }

    public BigDecimal getDecimal(String key, BigDecimal defaultValue) {
        try {
            return new BigDecimal(get(key, defaultValue.toPlainString()));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Transactional
    public void saveAll(Map<String, String> settings) {
        List<AppSetting> existing = settingRepository.findAll();
        Map<String, AppSetting> byKey = existing.stream()
                .collect(Collectors.toMap(AppSetting::getKey, s -> s));

        for (Map.Entry<String, String> entry : settings.entrySet()) {
            AppSetting setting = byKey.getOrDefault(entry.getKey(),
                    AppSetting.builder().key(entry.getKey()).build());
            setting.setValue(entry.getValue());
            settingRepository.save(setting);
        }
    }
}
