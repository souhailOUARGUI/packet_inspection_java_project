# Target IP (localhost for safe testing)
$targetIP = "127.0.0.1"

# Test a wider range of ports (100 ports) in quick succession
$ports = 1..100

Write-Host "Starting aggressive port scan test..."
Write-Host "Testing $($ports.Count) different ports rapidly..."

foreach ($port in $ports) {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    try {
        Write-Host "Testing port $port..." -NoNewline
        
        # Attempt to connect with a very short timeout
        $result = $tcpClient.BeginConnect($targetIP, $port, $null, $null)
        $success = $result.AsyncWaitHandle.WaitOne(10) # Very short timeout
        
        if ($success) {
            Write-Host " [OPEN]" -ForegroundColor Green
            $tcpClient.EndConnect($result)
        } else {
            Write-Host " [CLOSED]" -ForegroundColor Red
        }
    } catch {
        Write-Host " [ERROR]" -ForegroundColor Yellow
    } finally {
        if ($tcpClient.Connected) {
            $tcpClient.Close()
        }
        $tcpClient.Dispose()
    }
    
    # Very small delay to ensure packets are captured but still trigger detection
    Start-Sleep -Milliseconds 5
}

Write-Host "`nPort scan complete. Check your packet capture application for alerts."