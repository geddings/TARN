import os
import subprocess

from mininet.log import info, error

home_directory = '/users/cbarrin'
peering_prefix = ['184.164.240.0/24', '184.164.241.0/24', '184.164.242.0/24', '184.164.243.0/24']


def getOpenVPNAddress():
    info('** Running peering client script to view OpenVPN status.. \n')
    cwd = os.getcwd()
    if (not os.path.isdir(home_directory + '/client')):
        error('** MOVE PEERING CLIENT SCRIPTS TO HOME DIRECTORY ' + home_directory + '\n')
        exit()
    os.chdir(home_directory + '/client')
    vpnstatus = subprocess.check_output('sudo ./peering openvpn status', shell=True)
    vpnstatus = vpnstatus.split('\n')
    vpnup = [s for s in vpnstatus if 'up' in s]
    if not vpnup:
        error('** BRING VPN TUNNEL UP BEFORE STARTING SERVER.. \n')
        exit()
    ip = vpnup[0].split(' ')
    ip = ip[3]
    os.chdir(cwd)
    return ip


def getTapInterface():
    interfaces = subprocess.check_output("ifconfig | sed 's/[ \t].*//;/^\(lo\|\)$/d'", shell=True).split('\n')
    tap = [s for s in interfaces if 'tap' in s]
    if not tap:
        error('** NO TAP INTERFACE DETECTED.. \n')
    return tap[0]


def choosePrefixToAnnounce():
    while True:
        printNumberedList(peering_prefix)
        prefix = raw_input("\nWhich prefix do you want to announce? >> ")
        confirm = raw_input("Are you sure you want to announce prefix " + peering_prefix[prefix] + "? >> ")
        confirm = confirm.strip().lower()
        if confirm == "yes" or confirm == "y":
            return peering_prefix[prefix]


def printNumberedList(list):
    for index, value in enumerate(list):
        print("{}: {}".format(index, value))

