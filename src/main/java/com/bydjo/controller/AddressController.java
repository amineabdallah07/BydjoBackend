package com.bydjo.controller;

import com.bydjo.dtos.common.ApiResponse;
import com.bydjo.entity.Address;
import com.bydjo.entity.User;
import com.bydjo.repository.AddressRepository;
import com.bydjo.repository.UserRepository;
import com.bydjo.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "Address management APIs")
public class AddressController {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Address>> getMyAddresses(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userPrincipal.getId());
        return ResponseEntity.ok(addresses);
    }

    @PostMapping
    public ResponseEntity<Address> addAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody Address address) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow();
        address.setUser(user);
        return ResponseEntity.ok(addressRepository.save(address));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @RequestBody Address addressData) {
        Address address = addressRepository.findByIdAndUserId(id, userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Address not found"));
        address.setLabel(addressData.getLabel());
        address.setFullName(addressData.getFullName());
        address.setPhone(addressData.getPhone());
        address.setGovernorate(addressData.getGovernorate());
        address.setCity(addressData.getCity());
        address.setAddressLine(addressData.getAddressLine());
        address.setNotes(addressData.getNotes());
        return ResponseEntity.ok(addressRepository.save(address));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        addressRepository.deleteByIdAndUserId(id, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}
