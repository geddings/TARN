# class MiniNExT(Mininext):
#     """Extended MiniNExT class that adds the ability to add controllers from a Topo class"""
#
#     def buildFromTopo(self, topo=None):
#         print self.hosts
#         print '*** Adding controllers:\n'
#         for controllerName in topo.controllers():
#             self.addController(controllerName, **topo.nodeInfo(controllerName))
#             print controllerName + ' '
#         Mininext.buildFromTopo(self, topo)
