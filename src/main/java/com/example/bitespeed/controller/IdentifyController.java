package com.example.bitespeed.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bitespeed.dto.IdentifyRequest;
import com.example.bitespeed.dto.IdentifyResponse;
import com.example.bitespeed.service.IdentityService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/identify")
public class IdentifyController {

    @Autowired
    private IdentityService identityService;

    @PostMapping
    public ResponseEntity<IdentifyResponse> identify(@Valid @RequestBody IdentifyRequest request){
        return ResponseEntity.ok(identityService.identifyContact(request));
    }

}
