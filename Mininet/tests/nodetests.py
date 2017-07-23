import os
import sys

import mininet.log as log
from mininet.net import Mininet
from mininext.cli import CLI

sys.path.insert(0, os.path.abspath('..'))
import nodes

if __name__ == "__main__":
    print nodes
    log.setLogLevel('info')
    log.info('Testing Floodlight class...\n')

    net = Mininet(topo=None, build=False)

    for x in range(0, 20):
        net.addController(name='c' + str(x), controller=nodes.Floodlight)

    net.build()

    for controller in net.controllers:
        controller.start()

    CLI(net)
    net.stop()
