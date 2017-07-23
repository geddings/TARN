import os
import sys
import atexit

import mininet.util
import mininext.util
mininet.util.isShellBuiltin = mininext.util.isShellBuiltin
sys.modules['mininet.util'] = mininet.util

sys.path.insert(0, os.path.abspath('../..'))
from nodes.floodlight import Floodlight
from nodes.bgp_router_cntl import BGPRTCntl

from mininet.log import setLogLevel, info
from mininet.net import Mininet
from mininext.cli import CLI

from topo import QuaggaTopo

from mininext.net import MiniNExT
from mininet.node import OVSController


# def addController():


# Adding Floodlight Controller
# info('\n*** Adding controller\n')
# net.addController('c1', controller=Floodlight)
# net.addController('c2', controller=Floodlight)


def startNetwork():
    info('\n*** Creating BGP network topology\n')
    topo = QuaggaTopo()

    # global net
    net = MiniNExT(topo=topo, build=False)
    info(net)
    # addController()
    info('\n*** Adding controller\n')
    c1 = net.addController('c1', controller=Floodlight)
    c2 = net.addController('c2', controller=Floodlight)

    info('\n*** Starting the network\n')
    net.build()

    sw1 = net.getNodeByName('sw1')
    sw2 = net.getNodeByName('sw2')

    for controller in net.controllers:
        controller.start()

    sw1.start([c1])
    sw2.start([c2])

    h1 = net.getNodeByName('h1')
    h1.setIP('10.0.0.4/24', intf='h1-eth0')
    h1.cmd('route add default gw 10.0.0.2')

    h2 = net.getNodeByName('h2')
    h2.setIP('20.0.0.4/24', intf='h2-eth0')
    h2.cmd('route add default gw 20.0.0.2')

    as1 = net.getNodeByName('as1')
    as1.setIP('10.0.0.2/24', intf='as1-eth0')
    as1.setIP('100.0.0.1/24', intf='as1-eth1')

    as2 = net.getNodeByName('as2')
    as2.setIP('20.0.0.2/24', intf='as2-eth0')
    as2.setIP('100.0.0.2/24', intf='as2-eth1')

    as1.setARP(h1.IP(), h1.MAC())
    as2.setARP(h2.IP(), h2.MAC())

    info('*** Advertising new prefixes..\n')
    bgpController = BGPRTCntl()
    bgpController.inject_one_prefix('as2', 200, '30.0.0.0/24')
    bgpController.inject_one_prefix('as2', 200, '40.0.0.0/24')
    bgpController.show_bgp_all_prefix('as2')
    as2.cmd('route add default gw 20.0.0.4')

    info('** Running CLI\n')
    CLI(net)
    net.stop()

if __name__ == '__main__':
    # Tell mininet to print useful information
    setLogLevel('info')
    startNetwork()
