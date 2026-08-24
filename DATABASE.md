# Database Design

## User
- id
- full_name
- email
- password
- phone
- avatar
- role
- status
- created_at
- updated_at

## PhotographerProfile
- id
- user_id
- bio
- location
- experience
- price_from
- approval_status
- rating

## Portfolio
- id
- photographer_id
- image_url
- description
- category
- created_at

## ServicePackage
- id
- photographer_id
- name
- description
- price
- duration
- status

## Booking
- id
- customer_id
- photographer_id
- service_package_id
- booking_date
- location
- total_price
- status
- note
- created_at

## Review
- id
- booking_id
- customer_id
- photographer_id
- rating
- comment
- created_at

## Message
- id
- sender_id
- receiver_id
- booking_id
- content
- sent_at
- is_read

## Booking Status
- PENDING
- ACCEPTED
- REJECTED
- CANCELLED
- IN_PROGRESS
- COMPLETED
