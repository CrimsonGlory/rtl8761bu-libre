# Current environment
Cursor (you!) is inside a Docker container with x11docker.  
The current linux kernel on this machine is:
Linux 93d7ac3a9462 6.1.0-38-amd64 #1 SMP PREEMPT_DYNAMIC Debian 6.1.147-1 (2025-08-02) x86_64 GNU/Linux

The following packages have been installed:
```
binutils
bsdextrautils
file
xxd
binwalk
gdb-multiarch
wireshark
tshark
bluez
rizin
python3
python3-pip
```

6.1 tag was done on 11 december 2022.

this is bluetooth src folder for the linux kernel on december 12 2022.
https://github.com/torvalds/linux/tree/4071d98b296a5bc5fd4b15ec651bd05800ec9510/drivers/bluetooth

btrtl.c:
https://github.com/torvalds/linux/blob/4071d98b296a5bc5fd4b15ec651bd05800ec9510/drivers/bluetooth/btrtl.c

btusb.c:
https://github.com/torvalds/linux/blob/4071d98b296a5bc5fd4b15ec651bd05800ec9510/drivers/bluetooth/btusb.c

Always use ```source .venv/bin/activate``` before executing scripts.

