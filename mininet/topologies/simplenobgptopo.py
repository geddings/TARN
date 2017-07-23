#!/usr/bin/python
import os
import sys
from functools import partial

import mininet.log as log
from mininet.cli import CLI
from mininet.net import Mininet
from mininet.node import OVSSwitch
from mininet.topo import Topo

sys.path.insert(0, os.path.abspath('..'))
import nodes


class SimpleNoBGPTopo(Topo):
    def __init__(self):
        Topo.__init__(self)
        s1 = self.addSwitch('s1', cls=OVSSwitch)
        s2 = self.addSwitch('s2', cls=OVSSwitch)
        h1 = self.addHost('h1')
        h2 = self.addHost('h2')
        self.addLink(h1, s1)
        self.addLink(s1, s2)
        self.addLink(s2, h2)


if __name__ == '__main__':
    log.setLogLevel('debug')
    net = Mininet(topo=SimpleNoBGPTopo(), build=False)
    # c1 = net.addController(name='c1', controller=RemoteController, ip='130.127.39.221', port=6653)  #nodes.Floodlight)
    c1 = net.addController(name='c1', controller=nodes.Floodlight)
    c2 = net.addController(name='c2', controller=nodes.Floodlight)
    net.build()
    s1 = net.getNodeByName('s1')
    s1.start([c1])
    s1.cmd('ovs-vsctl set bridge s1 protocols=OpenFlow15')
    s2 = net.getNodeByName('s2')
    s2.start([c2])
    s2.cmd('ovs-vsctl set bridge s2 protocols=OpenFlow15')
    for controller in net.controllers:
        controller.start()
    CLI(net)
    net.stop()
