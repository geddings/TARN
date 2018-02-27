#!/usr/bin/python

"""
A simple one-way randomization example using TARN.

This example uses a simple linear topology:
     c1   c2
      |    |
h1---s1---s2---h2

Each host is capable of both IPv4 and IPv6 communication. Consequently, each TARN controller has been configured
to randomize the IP addresses of h2, both IPv4 and IPv6, to some preconfigured external prefix.

Example usage:
mininet> h1 ping h2

mininet> h1 ping6 -I h1-eth0 fc00:5555::1

author: Geddings Barrineau (cbarrin@g.clemson.edu)
"""

import os
import time
from os import makedirs
from os import path

from mininet.cli import CLI
from mininet.log import info, setLogLevel
from mininet.net import Mininet

from nodes import Floodlight
from topologies.lineartopo import LinearTopo

HOME_FOLDER = os.getenv('HOME')
LOG_PATH = HOME_FOLDER + '/TARN/logs/'


def startNetwork():
    # check if the log path already exists. If not, create it
    if not path.exists(LOG_PATH):
        makedirs(LOG_PATH)

    # Create the network from a given topology without building it yet
    global net
    net = Mininet(topo=LinearTopo(), build=False)

    info('** Adding Floodlight Controller\n')
    net.addController(name='c1', controller=Floodlight)
    net.addController(name='c2', controller=Floodlight)

    # Build the network
    net.build()

    # Start the switches and specify a controller
    s1 = net.getNodeByName('s1')
    s2 = net.getNodeByName('s2')

    # Start each controller
    c1 = net.getNodeByName('c1')
    c2 = net.getNodeByName('c2')
    for controller in net.controllers:
        controller.start()
    s1.start([c1])
    s2.start([c2])

    # After switch registered, set protocol to OpenFlow 1.5
    s1.cmd('ovs-vsctl set bridge s1 protocols=OpenFlow15')
    s2.cmd('ovs-vsctl set bridge s2 protocols=OpenFlow15')

    # Send non blocking commands to each host
    h1 = net.getNodeByName('h1')
    h2 = net.getNodeByName('h2')

    h1.setIP('10.0.0.1', prefixLen=24)
    h1.cmd('ifconfig h1-eth0 inet6 add fc00:1111::1/64')
    h2.setIP('50.0.0.1', prefixLen=24)
    h2.cmd('ifconfig h2-eth0 inet6 add fc00:5555::1/64')

    time.sleep(10)

    # Get link-local IPv6 addresses from hosts
    # h1_local = h1.cmd('ip -6 -o addr show h1-eth0 | awk \'{print $4}\'')
    # h2_local = h2.cmd('ip -6 -o addr show h2-eth0 | awk \'{print $4}\'')
    # info(h2_local)
    # h1_local = h1_local.split('/')[0].strip(' \t\n\r')
    # h2_local = h2_local.split('/')[0].strip(' \t\n\r')
    # info(h2_local)

    # REST API to configure AS1 controller
    c1.addMapping("50.0.0.1", "80.0.0.0/16")
    c1.addMapping("fc00:5555::1", "fc00:8888::/64")

    # REST API to configure AS2 controller
    c2.addMapping("50.0.0.1", "80.0.0.0/16")
    c2.addMapping("fc00:5555::1", "fc00:8888::/64")

    # End-to-end communication setup as below
    h1.cmd('route add -net 50.0.0.0 netmask 255.255.255.0 dev h1-eth0')
    h1.cmd('route -A inet6 add fc00:5555::/64 dev h1-eth0')
    h1.cmd('ip -6 neigh add fc00:5555::1 lladdr ' + h2.MAC() + ' dev h1-eth0')
    h2.cmd('route add -net 10.0.0.0 netmask 255.255.255.0 dev h2-eth0')
    h2.cmd('route -A inet6 add fc00:1111::/64 dev h2-eth0')
    h2.cmd('ip -6 neigh add fc00:1111::1 lladdr ' + h1.MAC() + ' dev h2-eth0')


def stopNetwork():
    if net is not None:
        info('** Tearing down network\n')
        net.stop()


if __name__ == '__main__':
    setLogLevel('info')
    startNetwork()
    CLI(net)
    stopNetwork()
