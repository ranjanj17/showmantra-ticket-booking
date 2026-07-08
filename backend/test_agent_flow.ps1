$baseUrl = "http://localhost:8080/api/agent/chat"
$sessionId = [guid]::NewGuid().ToString()

function Send-ChatMessage($message, $delay=15) {
    Write-Host ">>> USER: $message"
    $body = @{
        sessionId = $sessionId
        message = $message
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri $baseUrl -Method Post -ContentType "application/json" -Body $body
        Write-Host "<<< AGENT: $($response.reply)" -ForegroundColor Cyan
    } catch {
        Write-Host "ERROR: $_" -ForegroundColor Red
        if ($_.Exception.Response) {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            Write-Host "Response Body: $($reader.ReadToEnd())" -ForegroundColor Red
        }
    }
    
    Write-Host "Sleeping for $delay seconds to respect rate limit..." -ForegroundColor Yellow
    Start-Sleep -Seconds $delay
}

Write-Host "Starting E2E Chat Test Flow..." -ForegroundColor Green
Write-Host "Session ID: $sessionId"
Write-Host "================================="

Send-ChatMessage "What movies are playing today?" 15
Send-ChatMessage "Book 2 seats for Joker in Bengaluru for today evening." 15
Send-ChatMessage "I want to choose Show ID 1." 15
Send-ChatMessage "I'll take seats A1 and A2." 15
Send-ChatMessage "Yes, proceed with payment." 15
Send-ChatMessage "Cancel my booking." 5

Write-Host "================================="
Write-Host "E2E Chat Test Flow Complete." -ForegroundColor Green
