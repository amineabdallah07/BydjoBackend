package com.bydjo.service.impl;

import com.bydjo.dtos.common.ApiResponse;
import com.bydjo.dtos.tshirt.TshirtDto;
import com.bydjo.entity.Tshirt;
import com.bydjo.entity.TshirtScan;
import com.bydjo.repository.TshirtRepository;
import com.bydjo.repository.TshirtScanRepository;
import com.bydjo.service.TshirtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TshirtServiceImpl implements TshirtService {

    private final TshirtRepository tshirtRepository;
    private final TshirtScanRepository tshirtScanRepository;
    private final com.bydjo.repository.UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<TshirtDto> createTshirt(String code) {
        if (tshirtRepository.existsByCode(code)) {
            return ApiResponse.error("T-shirt code already exists: " + code);
        }

        Tshirt tshirt = Tshirt.builder()
                .code(code.toUpperCase())
                .scanCount(0)
                .build();

        tshirt = tshirtRepository.save(tshirt);
        log.info("T-shirt created with code: {}", tshirt.getCode());

        return ApiResponse.success("T-shirt created", mapToDto(tshirt));
    }

    @Override
    @Transactional
    public ApiResponse<TshirtDto> assignTshirt(String code, Long userId) {
        Tshirt tshirt = tshirtRepository.findByCode(code.toUpperCase()).orElse(null);
        if (tshirt == null) {
            return ApiResponse.error("T-shirt not found: " + code);
        }
        if (!userRepository.existsById(userId)) {
            return ApiResponse.error("User not found");
        }
        tshirt.setOwnerId(userId);
        tshirtRepository.save(tshirt);
        log.info("T-shirt {} assigned to user {}", code, userId);
        return ApiResponse.success("T-shirt assigned", mapToDto(tshirt));
    }

    @Override
    @Transactional
    public ApiResponse<TshirtDto> registerScan(String code, HttpServletRequest request) {
        Tshirt tshirt = tshirtRepository.findByCode(code.toUpperCase()).orElse(null);
        if (tshirt == null) {
            return ApiResponse.error("T-shirt not found");
        }

        tshirt.setScanCount(tshirt.getScanCount() + 1);
        tshirtRepository.save(tshirt);

        String ip = request != null ? request.getRemoteAddr() : null;
        TshirtScan scan = TshirtScan.builder()
                .tshirtCode(tshirt.getCode())
                .ipAddress(ip)
                .build();
        tshirtScanRepository.save(scan);

        log.info("T-shirt {} scanned. Total scans: {}", code, tshirt.getScanCount());

        return ApiResponse.success("Scan registered", mapToDto(tshirt));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<TshirtDto>> getMyTshirts(Long userId) {
        List<Tshirt> tshirts = tshirtRepository.findByOwnerIdOrderByCreatedAtDesc(userId);
        List<TshirtDto> dtos = tshirts.stream().map(this::mapToDto).collect(Collectors.toList());
        return ApiResponse.success(dtos);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<TshirtDto>> getAllTshirts() {
        List<Tshirt> tshirts = tshirtRepository.findAll();
        List<TshirtDto> dtos = tshirts.stream().map(this::mapToDto).collect(Collectors.toList());
        return ApiResponse.success(dtos);
    }

    private TshirtDto mapToDto(Tshirt tshirt) {
        return TshirtDto.builder()
                .id(tshirt.getId())
                .code(tshirt.getCode())
                .ownerId(tshirt.getOwnerId())
                .scanCount(tshirt.getScanCount())
                .createdAt(tshirt.getCreatedAt())
                .build();
    }
}
