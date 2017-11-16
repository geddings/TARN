#!/usr/bin/python
import json
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


def stopNetwork():
    if net is not None:
        info('** Tearing down network\n')
        net.stop()


def test_iperf_tcp():
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

    h1.setIP('10.0.0.1', prefixLen=24)
    h2.setIP('50.0.0.1', prefixLen=24)

    time.sleep(10)

    # REST API to configure AS1 controller
    c1.addMapping("10.0.0.1", "40.0.0.0/24")
    c1.addMapping("50.0.0.1", "80.0.0.0/16")

    # REST API to configure AS2 controller
    c2.addMapping("10.0.0.1", "40.0.0.0/24")
    c2.addMapping("50.0.0.1", "80.0.0.0/16")

    # End-to-end communication setup as below
    h1.cmd('route add -net 50.0.0.0 netmask 255.255.255.0 dev h1-eth0')
    h2.cmd('route add -net 10.0.0.0 netmask 255.255.255.0 dev h2-eth0')

    time.sleep(3)

    # Query flow rules on each switch and write to log file
    s1.cmd('ovs-ofctl dump-flows s1 -O OpenFlow15 > ' + LOG_PATH + ' s1.log')
    s2.cmd('ovs-ofctl dump-flows s2 -O OpenFlow15 > ' + LOG_PATH + ' s2.log')

    info("** Testing network connectivity\n")
    h1_bw, h2_bw = net.iperf((h1, h2), l4Type='TCP')

    stopNetwork()


def test_iperf_udp():
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

    h1.setIP('10.0.0.1', prefixLen=24)
    h2.setIP('50.0.0.1', prefixLen=24)

    time.sleep(10)

    # REST API to configure AS1 controller
    c1.addMapping("10.0.0.1", "40.0.0.0/24")
    c1.addMapping("50.0.0.1", "80.0.0.0/16")

    # REST API to configure AS2 controller
    c2.addMapping("10.0.0.1", "40.0.0.0/24")
    c2.addMapping("50.0.0.1", "80.0.0.0/16")

    # End-to-end communication setup as below
    h1.cmd('route add -net 50.0.0.0 netmask 255.255.255.0 dev h1-eth0')
    h2.cmd('route add -net 10.0.0.0 netmask 255.255.255.0 dev h2-eth0')

    time.sleep(3)

    # Query flow rules on each switch and write to log file
    s1.cmd('ovs-ofctl dump-flows s1 -O OpenFlow15 > ' + LOG_PATH + ' s1.log')
    s2.cmd('ovs-ofctl dump-flows s2 -O OpenFlow15 > ' + LOG_PATH + ' s2.log')

    info("** Testing network connectivity\n")
    bw, h1_bw, h2_bw = net.iperf((h1, h2), l4Type='UDP')

    stopNetwork()
