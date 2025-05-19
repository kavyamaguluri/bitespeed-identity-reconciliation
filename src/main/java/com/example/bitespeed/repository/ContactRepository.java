package com.example.bitespeed.repository;

import org.springframework.stereotype.Repository;
import com.example.bitespeed.model.Contact;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByEmailOrPhoneNumber(String email, String phoneNumber);
    
}
