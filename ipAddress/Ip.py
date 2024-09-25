import subprocess
import socket
 
def get_connected_devices():
    try:
        # Check if hotspot is active by querying the network interfaces
        result = subprocess.run(['adb', 'shell', 'cat', '/proc/net/wireless'], capture_output=True, text=True)
 
        # Check if wireless interface exists in the result (usually 'wlan0')
        if "wlan0" not in result.stdout:
            print("No devices connected or hotspot is off.")
            return
 
        # If hotspot is active, retrieve ARP table (connected devices)
        result = subprocess.run(['adb', 'shell', 'cat', '/proc/net/arp'], capture_output=True, text=True)
        arp_output = result.stdout.strip()
 
        if not arp_output:
            print("No devices connected.")
            return
 
        connected_devices = []
        lines = arp_output.splitlines()[1:]  # Skipping the header line
        for line in lines:
            columns = line.split()
            if len(columns) >= 4:
                ip_address = columns[0]
                mac_address = columns[3]
                # Attempt to resolve hostname
                try:
                    hostname = socket.gethostbyaddr(ip_address)[0]
                except socket.herror:
                    hostname = "Unknown Device"
                
                connected_devices.append((hostname, ip_address, mac_address))
 
        if connected_devices:
            print("Connected devices:")
            for device in connected_devices:
                print(f"Device Name: {device[0]}, IP Address: {device[1]}, MAC Address: {device[2]}")
        else:
            print("No devices connected.")
 
    except Exception as e:
        print(f"An error occurred: {e}")
 
if __name__ == "__main__":
    get_connected_devices()
 