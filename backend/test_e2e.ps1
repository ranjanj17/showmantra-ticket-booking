$ErrorActionPreference = "Stop"

$baseUrl = "http://localhost:8080/api"

Write-Host "========================================="
Write-Host "Starting End-to-End API Test"
Write-Host "========================================="

# 1. User Login
Write-Host "1. Testing User Login..."
$loginBody = @{
    email = "testuser@showmantra.com"
    password = "hashed_password_123"
} | ConvertTo-Json
$loginResponse = Invoke-RestMethod -Uri "$baseUrl/users/login" -Method Post -Body $loginBody -ContentType "application/json"
$userId = $loginResponse.id
Write-Host "   Login successful! User ID: $userId"

# 2. Get Theaters by City
Write-Host "2. Getting Theaters for City 1..."
$theaters = Invoke-RestMethod -Uri "$baseUrl/theaters?cityId=1" -Method Get
$theaterId = $theaters[0].id
Write-Host "   Found $($theaters.Count) theaters. Selected Theater ID: $theaterId"

# 3. Search for a Movie
Write-Host "3. Searching for movie 'Inception'..."
$movies = Invoke-RestMethod -Uri "$baseUrl/movies/search?q=Inception" -Method Get
$movieId = $movies[0].id
Write-Host "   Found movie! Movie ID: $movieId"

# 4. Get Shows for the movie for today
$today = (Get-Date).ToString("yyyy-MM-dd")
Write-Host "4. Getting shows for Movie ID $movieId on $today..."
$shows = Invoke-RestMethod -Uri "$baseUrl/shows/movie/$movieId`?date=$today" -Method Get
if ($shows.Count -eq 0) {
    Write-Host "   No shows found for today. Trying tomorrow..."
    $tomorrow = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")
    $shows = Invoke-RestMethod -Uri "$baseUrl/shows/movie/$movieId`?date=$tomorrow" -Method Get
}
$showId = $shows[0].showId
Write-Host "   Found $($shows.Count) shows. Selected Show ID: $showId"

# 5. Get Seat Matrix
Write-Host "5. Getting seat matrix for Show ID $showId..."
$seatMatrix = Invoke-RestMethod -Uri "$baseUrl/shows/$showId/seats" -Method Get
$availableSeats = $seatMatrix.seatRows | ForEach-Object { $_.seats } | Where-Object { $_.status -eq "AVAILABLE" }
$seat1 = $availableSeats[0]
$seat2 = $availableSeats[1]
Write-Host "   Selected Seats: $($seat1.seatId), $($seat2.seatId) - Total Amount: $($seat1.price + $seat2.price)"

# 6. Lock Seats
Write-Host "6. Locking Seats..."
$lockBody = @{
    showId = $showId
    seatIds = @($seat1.seatId, $seat2.seatId)
} | ConvertTo-Json
$lockResponse = Invoke-RestMethod -Uri "$baseUrl/bookings/lock" -Method Post -Body $lockBody -ContentType "application/json" -Headers @{"X-User-Id" = $userId}
$bookingId = $lockResponse.bookingId
$bookingAmount = $lockResponse.totalAmount
Write-Host "   Seats Locked! Booking ID: $bookingId (Status: $($lockResponse.status))"

# 7. Process Payment
Write-Host "7. Processing Payment..."
$paymentBody = @{
    bookingId = $bookingId
    amount = $bookingAmount
    paymentMethodDetails = "test-card-token"
} | ConvertTo-Json
$paymentResponse = Invoke-RestMethod -Uri "$baseUrl/payments" -Method Post -Body $paymentBody -ContentType "application/json"
Write-Host "   Payment Processed! Transaction ID: $($paymentResponse.transactionId)"

# 8. Confirm Booking
Write-Host "8. Confirming Booking..."
$confirmResponse = Invoke-RestMethod -Uri "$baseUrl/bookings/$bookingId/confirm" -Method Post
Write-Host "   $confirmResponse"

# 9. Cancel Booking
Write-Host "9. Cancelling Booking..."
$cancelResponse = Invoke-RestMethod -Uri "$baseUrl/bookings/$bookingId/cancel" -Method Post
Write-Host "   $cancelResponse"

Write-Host "========================================="
Write-Host "End-to-End Test Completed Successfully!"
Write-Host "========================================="
