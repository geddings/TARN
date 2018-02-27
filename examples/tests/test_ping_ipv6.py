#!/usr/bin/python
import os
import time
from os import makedirs
from os import path

from mininet.log import info
from mininet.net import Mininet

from nodes import Floodlight
from topologies.lineartopo import LinearTopo

HOME_FOLDER = os.getenv('HOME')
LOG_PATH = HOME_FOLDER + '/TARN/logs/'
PACKET_LOSS_THRESHOLD = 20


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

    # setUp()


def stopNetwork():
    if net is not None:
        info('** Tearing down network\n')
        net.stop()


def test_ping():
    startNetwork()

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

    # Set IPv6 addresses
    h1.cmd('ifconfig h1-eth0 inet6 add fc00:1111::1/64')
    h2.cmd('ifconfig h2-eth0 inet6 add fc00:5555::1/64')

    time.sleep(10)

    # REST API to configure AS1 controller
    c1.addMapping("10.0.0.1", "40.0.0.0/24")
    c1.addMapping("50.0.0.1", "80.0.0.0/16")
    c1.addMapping("fc00:1111::1", "fc00:4444::/64")
    c1.addMapping("fc00:5555::1", "fc00:8888::/64")

    # REST API to configure AS2 controller
    c2.addMapping("10.0.0.1", "40.0.0.0/24")
    c2.addMapping("50.0.0.1", "80.0.0.0/16")
    c2.addMapping("fc00:1111::1", "fc00:4444::/64")
    c2.addMapping("fc00:5555::1", "fc00:8888::/64")

    # End-to-end communication setup as below
    h1.cmd('route -A inet6 add fc00:5555::/64 dev h1-eth0')
    h1.cmd('ip -6 neigh add fc00:5555::1 lladdr ' + h2.MAC() + ' dev h1-eth0')
    h2.cmd('route -A inet6 add fc00:1111::/64 dev h2-eth0')
    h2.cmd('ip -6 neigh add fc00:1111::1 lladdr ' + h1.MAC() + ' dev h2-eth0')

    time.sleep(3)

    # Query flow rules on each switch and write to log file
    s1.cmd('ovs-ofctl dump-flows s1 -O OpenFlow15 > ' + LOG_PATH + ' s1.log')
    s2.cmd('ovs-ofctl dump-flows s2 -O OpenFlow15 > ' + LOG_PATH + ' s2.log')

    info("** Testing network connectivity\n")
    result = h1.cmd('ping6 -c 10 -I h1-eth0 fc00:5555::1')
    sent, received = net._parsePing(result)
    info('Sent:' + str(sent) + ' Received:' + str(received) + '\n')

    stopNetwork()
    assert sent - received == 0
