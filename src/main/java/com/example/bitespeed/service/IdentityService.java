package com.example.bitespeed.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bitespeed.dto.IdentifyRequest;
import com.example.bitespeed.dto.IdentifyResponse;
import com.example.bitespeed.exceptions.ResourceNotFoundException;
import com.example.bitespeed.model.Contact;
import com.example.bitespeed.model.LinkPrecedence;
import com.example.bitespeed.repository.ContactRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IdentityService {

    @Autowired
    private ContactRepository contactRepository;

    public IdentifyResponse identifyContact(IdentifyRequest request) {
        String email = request.getEmail();
        String phoneNumber = request.getPhoneNumber();

        // Step 1: Find matching contacts by email or phone number
        List<Contact> matchedContacts = contactRepository.findByEmailOrPhoneNumber(email, phoneNumber);

        if (matchedContacts.isEmpty()) {
            // Step 2: No existing contact, create a new primary contact
            Contact newContact = new Contact();
            newContact.setEmail(email);
            newContact.setPhoneNumber(phoneNumber);
            newContact.setLinkPrecedence(LinkPrecedence.PRIMARY);
        
    newContact.setCreatedAt(LocalDateTime.now());
            newContact.setUpdatedAt(LocalDateTime.now());

            contactRepository.save(newContact);

            return buildResponse(newContact, List.of());
        }

        // Step 3: Collect all related contacts using BFS (transitive linkage)
        Set<Contact> allRelatedContacts = new HashSet<>(matchedContacts);
        Queue<Contact> queue = new LinkedList<>(matchedContacts);

        while (!queue.isEmpty()) {
            Contact current = queue.poll();
            List<Contact> more = contactRepository.findByEmailOrPhoneNumber(current.getEmail(), current.getPhoneNumber());
            for (Contact c : more) {
                if (!allRelatedContacts.contains(c)) {
                    allRelatedContacts.add(c);
                    queue.add(c);
                }
            }
        }

        // Step 4: Determine primary contact (earliest created)
        Contact primaryContact = allRelatedContacts.stream()
                .filter(c -> c.getLinkPrecedence() == LinkPrecedence.PRIMARY)
                .min(Comparator.comparing(Contact::getCreatedAt))
                .orElseThrow(() -> new ResourceNotFoundException("No primary contact found"));

        // Step 5: Normalize links
        for (Contact contact : allRelatedContacts) {
            if (!contact.getId().equals(primaryContact.getId()) &&
                    (contact.getLinkPrecedence() != LinkPrecedence.SECONDARY || !Objects.equals(contact.getLinkedId(), primaryContact.getId()))) {

                contact.setLinkPrecedence(LinkPrecedence.SECONDARY);
                contact.setLinkedId(primaryContact.getId());
                contact.setUpdatedAt(LocalDateTime.now());
                contactRepository.save(contact);
            }
        }

        // Step 6: If the incoming data is new, create a secondary contact
        boolean isNewInfo = allRelatedContacts.stream()
                .noneMatch(c -> Objects.equals(c.getEmail(), email) && Objects.equals(c.getPhoneNumber(), phoneNumber));

        if (isNewInfo) {
            Contact newSecondary = new Contact();
            newSecondary.setEmail(email);
            newSecondary.setPhoneNumber(phoneNumber);
            newSecondary.setLinkPrecedence(LinkPrecedence.SECONDARY);
            newSecondary.setLinkedId(primaryContact.getId());
            newSecondary.setCreatedAt(LocalDateTime.now());
            newSecondary.setUpdatedAt(LocalDateTime.now());

            contactRepository.save(newSecondary);
            allRelatedContacts.add(newSecondary);
        }

        return buildResponse(primaryContact, new ArrayList<>(allRelatedContacts));
    }

    private IdentifyResponse buildResponse(Contact primary, List<Contact> allContacts) {
        IdentifyResponse response = new IdentifyResponse();
        IdentifyResponse.ContactResponse contactResponse = new IdentifyResponse.ContactResponse();

        List<Contact> secondaryContacts = allContacts.stream()
                .filter(c -> !c.getId().equals(primary.getId()))
                .collect(Collectors.toList());

        Set<String> emails = new LinkedHashSet<>();
        Set<String> phoneNumbers = new LinkedHashSet<>();
        List<Long> secondaryIds = new ArrayList<>();

        emails.add(primary.getEmail());
        phoneNumbers.add(primary.getPhoneNumber());

        for (Contact contact : secondaryContacts) {
            if (contact.getEmail() != null) emails.add(contact.getEmail());
            if (contact.getPhoneNumber() != null) phoneNumbers.add(contact.getPhoneNumber());
            secondaryIds.add(contact.getId());
        }

        contactResponse.setPrimaryContactId(primary.getId());
        contactResponse.setEmails(new ArrayList<>(emails));
        contactResponse.setPhoneNumbers(new ArrayList<>(phoneNumbers));
        contactResponse.setSecondaryContactIds(secondaryIds);

        response.setContact(contactResponse);
        return response;
    }
}