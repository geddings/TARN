import json
import os
import sys
import time
from mininet.log import info, setLogLevel
from mininet.net import Mininet
from os import makedirs
from os import path

from nodes import Floodlight
from topologies.simplenobgptopo import SimpleNoBGPTopo

HOME_FOLDER = os.getenv('HOME')
LOG_PATH = HOME_FOLDER + '/TARN/logs/'
PACKET_LOSS_THRESHOLD = 20


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

    h1.setIP('10.0.0.1', prefixLen=24)
    h2.setIP('50.0.0.1', prefixLen=24)

    time.sleep(10)

    # REST API to configure AS1 controller
    c1.addMapping("50.0.0.1", "80.0.0.0/24")
    print pp_json(c1.getMappings())

    # REST API to configure AS2 controller
    c2.addMapping("50.0.0.1", "80.0.0.0/24")
    print pp_json(c2.getMappings())

    # End-to-end communication setup as below
    h1.cmd('route add -net 50.0.0.0 netmask 255.255.255.0 dev h1-eth0')
    h2.cmd('route add -net 10.0.0.0 netmask 255.255.255.0 dev h2-eth0')

    # Add static ARP entries
    # h1.setARP(h2.IP(), h2.MAC())
    # h2.setARP(h1.IP(), h1.MAC())

    time.sleep(3)

    # Query flow rules on each switch and write to log file
    s1.cmd('ovs-ofctl dump-flows s1 -O OpenFlow15 > ' + LOG_PATH + ' s1.log')
    s2.cmd('ovs-ofctl dump-flows s2 -O OpenFlow15 > ' + LOG_PATH + ' s2.log')

    info("** Testing network connectivity\n")
    # packet_loss = net.ping(net.hosts)
    result = h1.cmd('ping -i 0.1 -c 100 ' + str(h2.IP()))
    sent, received = net._parsePing( result )
    info('Sent:' + str(sent) + ' Received:' + str(received) + '\n')

    packet_loss = 100.0 * (sent - received) / sent

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


def stopNetwork():
    if net is not None:
        info('** Tearing down network\n')
        net.stop()


if __name__ == '__main__':
    # force clean up on exit by registering a cleanup function
    # atexit.register(stopNetwork)

    setLogLevel('debug')

    startNetwork()

    stopNetwork()
