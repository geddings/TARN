import mininet.util
import mininext.util
import sys

mininet.util.isShellBuiltin = mininext.util.isShellBuiltin
sys.modules['mininet.util'] = mininet.util

from mininet.log import info
from mininext.cli import CLI

from mininet.topo import Topo
from mininet.net import Mininet
from mininet.log import setLogLevel


class SingleSwitchTopo(Topo):
    def __init__(self):
        Topo.__init__(self)

        # Add hosts into topology
        client = self.addHost('client')
        server = self.addHost('server')

        self.addLink(client, server)


def iperfTest():
    "Create network and run iperf test"
    topo = SingleSwitchTopo()

    net = Mininet(topo=topo, build=False)
    net.build()

    print "Testing network connectivity"
    net.pingAll()

    print "Testing bandwidth between client and server"
    client, server = net.getNodeByName('client', 'server')
    net.iperf((client, server), l4Type='TCP')
    
    info('** Running CLI\n')
    CLI(net)
    net.stop()

if __name__ == '__main__':
    setLogLevel('info')
    iperfTest()