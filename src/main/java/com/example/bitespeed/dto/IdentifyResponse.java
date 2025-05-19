package com.example.bitespeed.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentifyResponse {
    private ContactResponse contact;

    @Data
    public static class  ContactResponse {
        
        private Long primaryContactId;
        private List<String> emails;
        private List<String> phoneNumbers;
        private List<Long> secondaryContactIds;            
        
    }
}
