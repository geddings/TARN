import inspect
import os
import sys

sys.path.insert(0, os.path.abspath('..'))


from mininext.topo import Topo
from mininet.log import info

from mininext.services.quagga import QuaggaService
from collections import namedtuple

QuaggaHost = namedtuple("QuaggaHost", "name ip loIP")
net = None


# class Topo(BaseTopo):
#     """Extended topology to support BGP and session maintenance topologies
#     for the EAGER project at Clemson University"""
#
#     # TODO Consider adding bopts (BGP options) or something similar if useful
#     def __init__(self, **opts):
#         BaseTopo.__init__(self, **opts)
#
#     def addController(self, name, **opts):
#         return self.addNode(name, isController=True, **opts)
#
#     def isController(self, n):
#         '''Returns true if node is a controller.'''
#         info = self.node_info[n]
#         return info and info.get('isController', False)
#
#     def controllers(self, sort=True):
#         '''Return controllers.'''
#         return [n for n in self.nodes(sort) if self.isController(n)]
#
#     def hosts(self, sort=True):
#         '''Return hosts.'''
#         return [n for n in self.nodes(sort) if not self.isSwitch(n) and not self.isController(n)]
#
#     # Add a group consisting of a controller, a switch, and a variable number of hosts
#     def addIPRewriteGroup(self, name, controller=Floodlight, hosts=1, **opts):
#         self.addController(name + '-c', controller=controller)
#         self.addSwitch()
#
#     # Add an autonomous system consisting of variable IP rewrite groups and a BGP router
#     def addAutonomousSystem(self, name, **opts):
#         pass


class QuaggaTopo(Topo):
    """Creates a topology of Quagga routers"""

    def __init__(self):
        """Initialize a Quagga topology with 5 routers, configure their IP
           addresses, loop back interfaces, and paths to their private
           configuration directories."""
        info("*** Init parent Topo class\n")
        Topo.__init__(self)

        # Directory where this file / script is located"
        selfPath = os.path.dirname(os.path.abspath(
            inspect.getfile(inspect.currentframe())))  # script directory

        # Initialize a service helper for Quagga with default options
        info("*** Start Quagga service\n")
        quaggaSvc = QuaggaService(autoStop=False)

        # Path configurations for mounts
        quaggaBaseConfigPath = selfPath + '/configs/'

        # List of Quagga host configs
        info("*** Create list of Quagga host configs\n")
        quaggaHosts = []
        quaggaHosts.append(QuaggaHost(name='as1', ip='100.0.0.1/24',
                                      loIP='1.1.1.1/24'))
        quaggaHosts.append(QuaggaHost(name='as2', ip='100.0.0.2/24',
                                      loIP='2.2.2.2/24'))

        # Add switches
        info("*** Add two switches\n")
        sw1 = self.addSwitch(name='sw1', dpid='0000000000000001', failMode='secure')
        sw2 = self.addSwitch(name='sw2', dpid='0000000000000002', failMode='secure')

        # Add Hosts
        info('*** Adding two regular hosts..\n')
        h1 = self.addHost('h1')
        h2 = self.addHost('h2')

        # Add Links b/w host and OVS switch
        info('*** Adding links between regular hosts and switches..\n')
        self.addLink(h1, sw1)
        self.addLink(h2, sw2)

        # Setup each Quagga router, add link between Routers
        quaggaContainerList = []
        for host in quaggaHosts:
            # Create an instance of a host, called a quaggaContainer
            quaggaContainer = self.addHost(name=host.name,
                                           ip=host.ip,
                                           hostname=host.name,
                                           privateLogDir=True,
                                           privateRunDir=True,
                                           inMountNamespace=True,
                                           inPIDNamespace=True,
                                           inUTSNamespace=True)

            # Add a loopback interface with an IP in router's announced range
            self.addNodeLoopbackIntf(node=host.name, ip=host.loIP)

            # Configure and setup the Quagga service for this node
            quaggaSvcConfig = \
                {'quaggaConfigPath': quaggaBaseConfigPath + host.name}
            self.addNodeService(node=host.name, service=quaggaSvc,
                                nodeConfig=quaggaSvcConfig)

            # Attach the quaggaContainer to the IXP Fabric Switch
            # self.addLink(quaggaContainer, sw)

            quaggaContainerList.append(quaggaContainer)

        # Add Link b/w quagga Router & OVS switch
        self.addLink(sw1, quaggaContainerList[0])
        self.addLink(sw2, quaggaContainerList[1])
        self.addLink(quaggaContainerList[0], quaggaContainerList[1])