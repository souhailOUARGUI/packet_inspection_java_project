from scapy.all import *
import time

def port_scan():
    target_ip = "127.0.0.1"  # localhost for testing
    start_port = 20
    end_port = 100  # Scanning 80 ports should trigger detection (threshold is 50)
    
    print(f"Starting port scan test on {target_ip}")
    print(f"Scanning ports {start_port} to {end_port}")
    
    # Create a TCP SYN packet template
    ip = IP(dst=target_ip)
    
    start_time = time.time()
    
    # Perform rapid port scanning
    for port in range(start_port, end_port + 1):
        # Create TCP SYN packet for current port
        tcp = TCP(sport=RandShort(), dport=port, flags="S")
        packet = ip/tcp
        
        # Send the packet
        send(packet, verbose=False)
        
        # Small delay to make packets visible in the monitor
        time.sleep(0.05)
        
        # Show progress
        if port % 10 == 0:
            print(f"Scanned up to port {port}")
    
    duration = time.time() - start_time
    print(f"\nPort scan completed in {duration:.2f} seconds")
    print(f"Scanned {end_port - start_port + 1} ports")

if __name__ == "__main__":
    # Suppress Scapy's IPv6 warning
    conf.ipv6_enabled = False
    port_scan()
