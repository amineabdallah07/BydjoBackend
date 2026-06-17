package com.bydjo.service;

import com.bydjo.dtos.common.ApiResponse;
import com.bydjo.dtos.tshirt.TshirtDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface TshirtService {
    ApiResponse<TshirtDto> createTshirt(String code);
    ApiResponse<TshirtDto> assignTshirt(String code, Long userId);
    ApiResponse<TshirtDto> registerScan(String code, HttpServletRequest request);
    ApiResponse<List<TshirtDto>> getMyTshirts(Long userId);
    ApiResponse<List<TshirtDto>> getAllTshirts();
}
