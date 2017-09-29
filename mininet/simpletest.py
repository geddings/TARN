import atexit
import json
import sys
import time

import os
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.net import Mininet
from os import makedirs
from os import path

from nodes import Floodlight
from topologies.simplenobgptopo import SimpleNoBGPTopo

HOME_FOLDER = os.getenv('HOME')
LOG_PATH = HOME_FOLDER + '/TARNProject/TARN/logs/'
PACKET_LOSS_THRESHOLD = 50


def pp_json(json_thing, sort=True, indents=4):
    if type(json_thing) is str:
        print(json.dumps(json.loads(json_thing), sort_keys=sort, indent=indents))
    else:
        print(json.dumps(json_thing, sort_keys=sort, indent=indents))


def setUp():
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

    h1.setIP('10.0.0.1', prefixLen=16)
    h2.setIP('50.0.0.1', prefixLen=16)

    # Wait for all commands to finish
    results = {}
    for h in net.hosts:
        results[h.name] = h.waitOutput()
    print "hosts finished"

    time.sleep(10)

    # REST API to configure AS1 controller
    c1.configure(lan_port="1", wan_port="2")
    c1.addAS("1", "10.0.0.0/24")
    c1.addPrefixToAS("1", "20.0.0.0/24")
    c1.addAS("2", "50.0.0.0/24")
    c1.addPrefixToAS("2", "60.0.0.0/24")
    print "C1 get AS information below"
    print pp_json(c1.getInfo())
    print pp_json(c1.getASes())
    
    c1.addHost("10.0.0.1", "1")

    # REST API to configure AS2 controller
    c2.configure(lan_port="2", wan_port="1")
    c2.addAS("2", "50.0.0.0/24")
    c2.addPrefixToAS("2", "60.0.0.0/24")
    c2.addAS("1", "10.0.0.0/24")
    c2.addPrefixToAS("1", "20.0.0.0/24")
    print "C2 get AS information below"
    print pp_json(c2.getInfo())
    print pp_json(c2.getASes())

    c2.addHost("10.0.0.1", "1")

    # End-to-end communication setup as below
    h1.cmd('route add -net 50.0.0.0 netmask 255.255.255.0 dev h1-eth0')
    h2.cmd('route add -net 10.0.0.0 netmask 255.255.255.0 dev h2-eth0')

    time.sleep(20)
    
    info("** Testing network connectivity\n")
    packet_loss = net.ping(net.hosts)

    info('** Running CLI\n')
    CLI(net)

    if packet_loss > PACKET_LOSS_THRESHOLD:
        sys.exit(-1)
    else:
        sys.exit(0)


def startNetwork():
    # check if the log path already exists. If not, create it
    if not path.exists(LOG_PATH):
        makedirs(LOG_PATH)

    # Create the network from a given topology without building it yet
    global net
    net = Mininet(topo=SimpleNoBGPTopo(), build=False)

    info('** Adding Floodlight Controller\n')
    net.addController(name='c1', controller=Floodlight)
    net.addController(name='c2', controller=Floodlight)

    # Build the network
    net.build()

    setUp()

    info('** Running CLI\n')
    CLI(net)


def stopNetwork():
    if net is not None:
        info('** Tearing down network\n')
        net.stop()


if __name__ == '__main__':
    # force clean up on exit by registering a cleanup function
    atexit.register(stopNetwork)

    setLogLevel('info')

    startNetwork()
