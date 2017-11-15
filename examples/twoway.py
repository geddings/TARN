#!/usr/bin/python
import os
import time
from mininet.cli import CLI
from mininet.log import info, setLogLevel
from mininet.net import Mininet
from nodes import Floodlight
from os import makedirs
from os import path
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


def stopNetwork():
    if net is not None:
        info('** Tearing down network\n')
        net.stop()


if __name__ == '__main__':
    setLogLevel('info')
    startNetwork()
    CLI(net)
    stopNetwork()
