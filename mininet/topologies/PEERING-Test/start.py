import atexit
import sys
import time

import mininet.util
import mininext.util
import os

mininet.util.isShellBuiltin = mininext.util.isShellBuiltin
sys.modules['mininet.util'] = mininet.util

sys.path.insert(0, os.path.abspath('../..'))

from mininet.log import setLogLevel, info
from mininext.cli import CLI

from topo import QuaggaTopo
from bgp_manager import bgpMgmt

from mininext.net import MiniNExT

"""
  This start.py aims to test PEETING testbed functionality, no floodlight controller stuff involved.
"""

# global PEERING testbed parameters
home_ASN = '47065'
peering_prefix = ['184.164.240.0/24', '184.164.241.0/24', '184.164.242.0/24', '184.164.243.0/24']

# Connects Server Side MiniNExT Quagga instance to PEERing peers
def serverConnectPEERing():
    # Server-side Parameters for PEERING setup
    quagga_node = 'quaggaS'
    openvpn_tap_device = "tap5"
    openvpn_tap_ip = '100.69.128.9'
    ovs_peering_quagga = 'sw2'

    # Server-side Parameters for end-to-end routing communication
    announce_prefix = peering_prefix[3]
    # quaggaS_facing_server = '10.0.0.2'
    quaggaS_facing_server = '184.164.243.2'
    quaggaS_lo = '184.164.243.1/32'
    # server_ip = '10.0.0.1'
    server_ip = '184.164.243.100'
    server_gw = quaggaS_facing_server
    server_static_route = 'route add -net 184.164.243.0 netmask 255.255.255.0 quaggaS-eth0'

    info('** Connecting Server Side Quagga to PEERing testbed ... \n')
    # Network topology setup
    sw_peering = net.getNodeByName('sw2')
    sw_server = net.getNodeByName('sw1')
    quaggaS = net.getNodeByName('quaggaS')
    server = net.getNodeByName('server')
    sw_peering.start([])

    # Controller setup

    # REST API to configure AS1 controller

    # PEERING setup
    cmd1 = 'ovs-vsctl add-port ' + ovs_peering_quagga + ' ' + openvpn_tap_device
    os.popen(cmd1)
    cmd2 = 'sudo ifconfig ' + openvpn_tap_device + ' 0.0.0.0'
    os.popen(cmd2)
    sw_peering.setIP('0.0.0.0', intf='sw2-eth1')
    quaggaS.setIP(openvpn_tap_ip, intf='quaggaS-eth1')

    # End-to-end communication setup
    quaggaS.setIP(quaggaS_facing_server, prefixLen=24, intf='quaggaS-eth0')
    quaggaS.setIP(quaggaS_facing_server, prefixLen=24, intf='quaggaS-eth0')
    cmd3 = "ip addr add " + quaggaS_lo + ' dev lo'
    quaggaS.cmdPrint(cmd3)
    cmd4 = server_static_route
    quaggaS.cmdPrint(cmd4)
    server.setIP(server_ip, prefixLen=24, intf='server-eth0')
    cmd5 = 'route add default gw ' + server_gw
    server.cmd(cmd5)

    info('** Announcing BGP prefix.. \n')
    bgpManager = bgpMgmt()
    bgpManager.prefix_announce(quagga_node, home_ASN, announce_prefix)


# Connects Client Side MiniNExT Quagga instance to PEERing peers
def clientConnectPEERing():
    # Client-side Parameters for PEERING setup
    quagga_node = 'quaggaC'
    openvpn_tap_device = "tap1"
    openvpn_tap_ip = '100.65.128.6'
    ovs_peering_quagga = 'sw3'

    # Client-side Parameters for end-to-end routing communication
    announce_prefix = peering_prefix[2]
    quaggaC_facing_client = '184.164.242.2'
    quaggaC_lo = '184.164.242.1/32'
    client_ip = '184.164.242.100'
    client_gw = quaggaC_facing_client

    info('** Connecting Client Side Quagga to PEERing testbed ... \n')
    # Network topology setup
    sw_peering = net.getNodeByName('sw3')
    sw_client = net.getNodeByName('sw4')
    quaggaC = net.getNodeByName('quaggaC')
    client = net.getNodeByName('client')
    sw_peering.start([])

    # Controller setup

    # REST API to configure AS1 controller

    # PEERING Setup
    cmd1 = 'ovs-vsctl add-port ' + ovs_peering_quagga + ' ' + openvpn_tap_device
    os.popen(cmd1)
    cmd2 = 'sudo ifconfig ' + openvpn_tap_device + ' 0.0.0.0'
    os.popen(cmd2)
    sw_peering.setIP('0.0.0.0', intf='sw3-eth1')
    quaggaC.setIP(openvpn_tap_ip, intf='quaggaC-eth1')

    # End-to-end communication setup
    quaggaC.setIP(quaggaC_facing_client, prefixLen=24, intf='quaggaC-eth0')
    quaggaC.setIP(quaggaC_facing_client, prefixLen=24, intf='quaggaC-eth0')
    cmd3 = "ip addr add " + quaggaC_lo + ' dev lo'
    quaggaC.cmdPrint(cmd3)
    client.setIP(client_ip, prefixLen=24, intf='client-eth0')
    cmd4 = 'route add default gw ' + client_gw
    client.cmd(cmd4)

    info('** Announcing BGP prefix.. \n')
    bgpManager = bgpMgmt()
    bgpManager.prefix_announce(quagga_node, home_ASN, announce_prefix)


def startNetwork():
    info('\n*** Creating BGP network topology\n')
    topo = QuaggaTopo()

    global net
    net = MiniNExT(topo=topo, build=False)

    info('\n*** Starting the network\n')
    net.build()
    net.start()

    serverConnectPEERing()
    clientConnectPEERing()

    info('** Running CLI\n')
    CLI(net)


def stopNetwork():
    if net is not None:
        info('** Tearing down BGP network\n')
        net.stop()


if __name__ == '__main__':
    # Force cleanup on exit by registering a cleanup function
    atexit.register(stopNetwork)

    # Tell mininet to print useful information
    setLogLevel('info')
    startNetwork()
