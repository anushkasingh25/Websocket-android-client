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

        # Write the output to devices.txt


        with open('../app/src/main/assets/devices.txt', 'w') as file:
            if connected_devices:
                for device in connected_devices:
                    if (device[2]=="08:f9:e0:f6:1c:ec"):
#                         file.write(f"Device Name: {device[0]}, IP Address: {device[1]}, MAC Address: {device[2]}\n")
                          file.write(f"{device[1]}\n")


            else:
                file.write("No devices connected.\n")

        print("Output written to devices.txt")

    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    get_connected_devices()
