# Bitespeed Backend Task: Identity Reconciliation

##  Overview

This project implements a RESTful web service that identifies and consolidates customer contacts based on **email** and/or **phone number**. It supports **FluxKart.com** by linking different orders made with varying contact details to a single unified customer identity.


##  Features

- Accepts `POST` requests at `/identify` with customer contact details (`email` and/or `phoneNumber`).
- Returns consolidated contact information including primary and secondary contacts.
- Creates new contacts or links existing ones with appropriate link precedence (`primary` or `secondary`).
- Uses **BFS (Breadth-First Search)** to discover transitive linkage across contacts.
- Validates input data using regex for email and phone number formats.


##  Contact Table Schema

| Field          | Type                   | Description                                         |
|----------------|------------------------|-----------------------------------------------------|
| id             | Int (Primary Key)      | Unique identifier for contact                       |
| phoneNumber    | String (Nullable)      | Phone number of the contact                         |
| email          | String (Nullable)      | Email address of the contact                        |
| linkedId       | Int (Nullable)         | Reference to another linked contact `id`            |
| linkPrecedence | Enum (primary/secondary) | Marks if contact is `primary` or `secondary`      |
| createdAt      | DateTime               | Creation timestamp                                  |
| updatedAt      | DateTime               | Last update timestamp                               |
| deletedAt      | DateTime (Nullable)    | Soft delete timestamp (if applicable)               |



##  API

### Endpoint

POST /identify
### Request Body 

```json
{
  "email": "kavya@example.com",
  "phoneNumber": "+12345678901"
}

Note: At least one of email or phoneNumber must be provided.
Phone Number Regex: ^\+?[0-9]{10,15}$

Response Body

{
  "contact": {
    "primaryContactId": 3,
    "emails": [
      "kavya@example.com"
    ],
    "phoneNumbers": [
      "+12345678901"
    ],
    "secondaryContactIds": []
  }
}


primaryContactId: ID of the primary contact.

emails: List of unique emails linked to this contact (primary email first).

phoneNumbers: List of unique phone numbers linked (primary number first).

secondaryContactIds: List of all secondary contact IDs linked to the primary.



How It Works 

Finds existing contacts matching the provided email and/or phone number.

Uses Breadth-First Search (BFS) to discover all transitive connections.

Determines the primary contact as the one with the earliest creation timestamp.

Links all other related contacts as secondary to the primary.

If the incoming data has a new email/phone, it is added as a secondary contact.

Responds with a consolidated view of the contact.



Tech Stack

Java 17+
Spring Boot 3+
Spring Data JPA / Hibernate
MySQL 
Gradle Build Tool
JUnit 5 for testing



Setup and Run

1. Clone the repository

git clone https://github.com/kavyamaguluri/bitespeed-identity-reconciliation.git
cd bitespeed-identity-reconciliation

2. Configure the database

3. Build and run the project
