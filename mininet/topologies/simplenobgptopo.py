#!/usr/bin/python

from mininet.node import OVSSwitch
from mininet.topo import Topo


class SimpleNoBGPTopo(Topo):
    def __init__(self):
        Topo.__init__(self)
        s1 = self.addSwitch('s1', cls=OVSSwitch)
        s2 = self.addSwitch('s2', cls=OVSSwitch)
        h1 = self.addHost('h1')
        h1.setIP('10.0.0.1', prefixLen=16)
        h2 = self.addHost('h2')
        h2.setIP('20.0.0.1', prefixLen=16)
        self.addLink(h1, s1)
        self.addLink(s1, s2)
        self.addLink(s2, h2)
