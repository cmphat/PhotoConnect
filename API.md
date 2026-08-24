# API Documentation

## Authentication

### Register
`POST /api/auth/register`

### Login
`POST /api/auth/login`

## Photographers

### Get all photographers
`GET /api/photographers`

### Get photographer detail
`GET /api/photographers/{id}`

### Update photographer profile
`PUT /api/photographers/{id}`

## Portfolio

### Get portfolio
`GET /api/photographers/{id}/portfolio`

### Add portfolio image
`POST /api/photographers/{id}/portfolio`

### Delete portfolio image
`DELETE /api/portfolio/{id}`

## Booking

### Create booking
`POST /api/bookings`

### Get booking detail
`GET /api/bookings/{id}`

### Accept booking
`PUT /api/bookings/{id}/accept`

### Reject booking
`PUT /api/bookings/{id}/reject`

### Complete booking
`PUT /api/bookings/{id}/complete`

## Review

### Create review
`POST /api/reviews`

### Get photographer reviews
`GET /api/photographers/{id}/reviews`
