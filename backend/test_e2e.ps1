$ErrorActionPreference = "Stop"

$baseUrl = "http://localhost:8080/api"

function Assert-Status ($response, $expectedStatus) {
    $code = $null
    if ($response -and $response.StatusCode) {
        $code = [int]$response.StatusCode
    }
    if ($code -ne $expectedStatus) {
        Write-Error "Assertion Failed! Expected $expectedStatus but got $code"
    }
}

Write-Host "========================================="
Write-Host "Starting Comprehensive End-to-End API Test"
Write-Host "========================================="

# Helper for catching errors
function Invoke-RestSafe {
    param($Uri, $Method, $Body, $Headers, $ContentType)
    try {
        if ($Body -and $Headers) {
            $resp = Invoke-WebRequest -Uri $Uri -Method $Method -Body $Body -ContentType $ContentType -Headers $Headers
        } elseif ($Body) {
            $resp = Invoke-WebRequest -Uri $Uri -Method $Method -Body $Body -ContentType $ContentType
        } elseif ($Headers) {
            $resp = Invoke-WebRequest -Uri $Uri -Method $Method -Headers $Headers
        } else {
            $resp = Invoke-WebRequest -Uri $Uri -Method $Method
        }
        return $resp
    } catch {
        return $_.Exception.Response
    }
}

$userEmail = "user_$(Get-Random)@showmantra.com"
$userPassword = "password123"

Write-Host "`n--- SUITE 1 & 2: Happy Path & Cancellation Flow ---"

Write-Host "1. Register User..."
$registerBody = @{
    email = $userEmail
    password = $userPassword
    phone = "1234567890"
    role = "USER"
} | ConvertTo-Json
$registerResponse = Invoke-RestSafe -Uri "$baseUrl/users/register" -Method Post -Body $registerBody -ContentType "application/json"
Assert-Status $registerResponse 201

Write-Host "2. Login..."
$loginBody = @{
    email = $userEmail
    password = $userPassword
} | ConvertTo-Json
$loginResponse = Invoke-RestSafe -Uri "$baseUrl/users/login" -Method Post -Body $loginBody -ContentType "application/json"
Assert-Status $loginResponse 200
$loginData = $loginResponse.Content | ConvertFrom-Json
$token = $loginData.token
$userId = $loginData.id
Write-Host "   Login successful. Got JWT Token."

Write-Host "3. Search Movie..."
$movieResponse = Invoke-RestSafe -Uri "$baseUrl/movies/search?q=Inception" -Method Get
Assert-Status $movieResponse 200
$movies = $movieResponse.Content | ConvertFrom-Json
$movieId = $movies[0].id

Write-Host "4. Get Shows..."
$today = (Get-Date).ToString("yyyy-MM-dd")
$showsResponse = Invoke-RestSafe -Uri "$baseUrl/shows/movie/$movieId`?date=$today" -Method Get
Assert-Status $showsResponse 200
$shows = $showsResponse.Content | ConvertFrom-Json
if ($shows.Count -eq 0) {
    $tomorrow = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")
    $showsResponse = Invoke-RestSafe -Uri "$baseUrl/shows/movie/$movieId`?date=$tomorrow" -Method Get
    $shows = $showsResponse.Content | ConvertFrom-Json
}
$showId = $shows[0].showId

Write-Host "5. Get Seats..."
$seatMatrixResponse = Invoke-RestSafe -Uri "$baseUrl/shows/$showId/seats" -Method Get
Assert-Status $seatMatrixResponse 200
$seatMatrix = $seatMatrixResponse.Content | ConvertFrom-Json
$availableSeats = $seatMatrix.seats | Where-Object { $_.status -eq "AVAILABLE" }
$seat1 = $availableSeats[0]
$seat2 = $availableSeats[1]

Write-Host "6. Lock Seats..."
$lockBody = @{
    showId = $showId
    seatIds = @($seat1.seatId, $seat2.seatId)
} | ConvertTo-Json
$lockResponse = Invoke-RestSafe -Uri "$baseUrl/bookings/lock" -Method Post -Body $lockBody -ContentType "application/json" -Headers @{"Authorization"="Bearer $token"}
Assert-Status $lockResponse 200
$lockData = $lockResponse.Content | ConvertFrom-Json
$bookingId = $lockData.bookingId
$amount = $lockData.totalAmount

Write-Host "7. Pay..."
$paymentBody = @{
    bookingId = $bookingId
    amount = $amount
    paymentMethodDetails = "card_123"
} | ConvertTo-Json
$paymentResponse = Invoke-RestSafe -Uri "$baseUrl/payments" -Method Post -Body $paymentBody -ContentType "application/json" -Headers @{"Authorization"="Bearer $token"}
Assert-Status $paymentResponse 200


Write-Host "9. Cancel Booking..."
$cancelResponse = Invoke-RestSafe -Uri "$baseUrl/bookings/$bookingId/cancel" -Method Post -Headers @{"Authorization"="Bearer $token"}
Assert-Status $cancelResponse 200

Write-Host "10. Check History..."
$historyResponse = Invoke-RestSafe -Uri "$baseUrl/bookings/history" -Method Get -Headers @{"Authorization"="Bearer $token"}
Assert-Status $historyResponse 200
$historyData = $historyResponse.Content | ConvertFrom-Json
if ($historyData[0].status -ne "CANCELLED") {
    Write-Error "History booking status is not CANCELLED."
}
Write-Host "   History successfully verified."


Write-Host "`n--- SUITE 3: Security & Authorization ---"
Write-Host "1. Fetch History without token..."
$unauthResponse = Invoke-RestSafe -Uri "$baseUrl/bookings/history" -Method Get
if ($unauthResponse.StatusCode -ne 403 -and $unauthResponse.StatusCode -ne 401) {
    Write-Error "Expected 401/403 but got $($unauthResponse.StatusCode)"
} else {
    Write-Host "   Successfully blocked unauthorized request."
}

Write-Host "`n--- SUITE 4: Seat Concurrency & Conflicts ---"
Write-Host "1. Get next available seat..."
$seatMatrixResponse2 = Invoke-RestSafe -Uri "$baseUrl/shows/$showId/seats" -Method Get
$seatMatrix2 = $seatMatrixResponse2.Content | ConvertFrom-Json
$availableSeats2 = $seatMatrix2.seats | Where-Object { $_.status -eq "AVAILABLE" }
$concurrencySeat = $availableSeats2[0]

Write-Host "2. User A locks seat..."
$lockBody2 = @{ showId = $showId; seatIds = @($concurrencySeat.seatId) } | ConvertTo-Json
$lockResponse2 = Invoke-RestSafe -Uri "$baseUrl/bookings/lock" -Method Post -Body $lockBody2 -ContentType "application/json" -Headers @{"Authorization"="Bearer $token"}
Assert-Status $lockResponse2 200

Write-Host "3. User B attempts to lock SAME seat..."
$user2Email = "user_$(Get-Random)@showmantra.com"
$registerBody2 = @{email=$user2Email; password="123"; phone="111"; role="USER"} | ConvertTo-Json
Invoke-RestSafe -Uri "$baseUrl/users/register" -Method Post -Body $registerBody2 -ContentType "application/json" | Out-Null
$loginResponse2 = Invoke-RestSafe -Uri "$baseUrl/users/login" -Method Post -Body (@{email=$user2Email; password="123"} | ConvertTo-Json) -ContentType "application/json"
$token2 = ($loginResponse2.Content | ConvertFrom-Json).token
$conflictLockResponse = Invoke-RestSafe -Uri "$baseUrl/bookings/lock" -Method Post -Body $lockBody2 -ContentType "application/json" -Headers @{"Authorization"="Bearer $token2"}
if ($conflictLockResponse.StatusCode -ne 409) {
    Write-Error "Expected 409 Conflict for double booking, got $($conflictLockResponse.StatusCode)"
} else {
    Write-Host "   Successfully blocked double-booking!"
}


Write-Host "`n--- SUITE 5: Resource Not Found & Empty States ---"
Write-Host "1. Fetch empty booking history for User B..."
$emptyHistoryResponse = Invoke-RestSafe -Uri "$baseUrl/bookings/history" -Method Get -Headers @{"Authorization"="Bearer $token2"}
if ($emptyHistoryResponse.StatusCode -ne 204) {
    Write-Error "Expected 204 No Content for empty history, got $($emptyHistoryResponse.StatusCode)"
} else {
    Write-Host "   Successfully got 204 No Content."
}

Write-Host "2. Try to cancel non-existent booking..."
$fakeUUID = [guid]::NewGuid().ToString()
$fakeCancelResponse = Invoke-RestSafe -Uri "$baseUrl/bookings/$fakeUUID/cancel" -Method Post -Headers @{"Authorization"="Bearer $token2"}
if ($fakeCancelResponse.StatusCode -ne 404) {
    Write-Error "Expected 404 Not Found, got $($fakeCancelResponse.StatusCode)"
} else {
    Write-Host "   Successfully got 404 Not Found."
}


Write-Host "`n--- SUITE 6: Bad Input Validation ---"
Write-Host "1. Register with invalid email..."
$badRegisterBody = @{email="invalid-email"; password="123"; phone="111"; role="USER"} | ConvertTo-Json
$badRegisterResponse = Invoke-RestSafe -Uri "$baseUrl/users/register" -Method Post -Body $badRegisterBody -ContentType "application/json"
if ($badRegisterResponse.StatusCode -ne 400) {
    Write-Error "Expected 400 Bad Request for invalid email, got $($badRegisterResponse.StatusCode)"
} else {
    Write-Host "   Successfully blocked invalid email!"
}

Write-Host "`n--- SUITE 7: Payment Failures ---"
Write-Host "1. Get another available seat..."
$seatMatrixResponse3 = Invoke-RestSafe -Uri "$baseUrl/shows/$showId/seats" -Method Get
$seatMatrix3 = $seatMatrixResponse3.Content | ConvertFrom-Json
$availableSeats3 = $seatMatrix3.seats | Where-Object { $_.status -eq "AVAILABLE" }
$failSeat = $availableSeats3[0]

Write-Host "2. Lock seat for User B..."
$lockBody3 = @{ showId = $showId; seatIds = @($failSeat.seatId) } | ConvertTo-Json
$lockResponse3 = Invoke-RestSafe -Uri "$baseUrl/bookings/lock" -Method Post -Body $lockBody3 -ContentType "application/json" -Headers @{"Authorization"="Bearer $token2"}
Assert-Status $lockResponse3 200
$failLockData = $lockResponse3.Content | ConvertFrom-Json
$bookingId3 = $failLockData.bookingId
$failAmount = $failLockData.totalAmount

Write-Host "3. Attempt payment with failure..."
$failPaymentBody = @{ bookingId = $bookingId3; amount = $failAmount; paymentMethodDetails = "fail" } | ConvertTo-Json
$failPaymentResponse = Invoke-RestSafe -Uri "$baseUrl/payments" -Method Post -Body $failPaymentBody -ContentType "application/json" -Headers @{"Authorization"="Bearer $token2"}
Assert-Status $failPaymentResponse 200
$failPaymentData = $failPaymentResponse.Content | ConvertFrom-Json
if ($failPaymentData.status -ne "FAILED") {
    Write-Error "Expected payment status FAILED, but got $($failPaymentData.status)"
} else {
    Write-Host "   Payment gateway failure handled successfully!"
}

Write-Host "`n========================================="
Write-Host "ALL TESTS PASSED SUCCESSFULLY!"
Write-Host "========================================="
