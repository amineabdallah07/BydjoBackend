package com.bydjo.controller;

import com.bydjo.entity.Size;
import com.bydjo.repository.SizeRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sizes")
@RequiredArgsConstructor
@Tag(name = "Sizes", description = "Size management APIs")
public class SizeController {

    private final SizeRepository sizeRepository;

    @GetMapping
    public ResponseEntity<List<Size>> getAllSizes() {
        return ResponseEntity.ok(sizeRepository.findAll());
    }
}