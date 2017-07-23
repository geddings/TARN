import time

import util
from pick import pick
from quagga_driver import QuaggaDriver

peering_prefix = ['184.164.240.0/24', '184.164.241.0/24', '184.164.242.0/24', '184.164.243.0/24']
home_ASN = '47065'
quagga_server = 'quaggaS'
quagga_client = 'quaggaC'
route_map = 'AMSIX'


class bgpMgmt:
    def __init__(self):
        self.bgpController = QuaggaDriver()

    def show_node_prefix(self, node):
        print '>>> Display Current BGP Prefix on Node', node, '!!'
        self.bgpController.show_bgp_all_prefix(node)
        print '\n\n'

    def show_all_neighbors(self, node):
        print '>>> Display BGP neighbors on Node', node, '!!'
        self.bgpController.show_bgp_neighbors(node)
        print '\n\n'

    def show_node_neighbor(self, node, prefix):
        print '>>> Display BGP neighbor ', prefix, ' on Node', node, '!!'
        self.bgpController.show_bgp_neighbor(node, prefix)
        print '\n\n'

    def show_adv_route(self, node, prefix):
        print '>>> Display advertised routes on Node', node, '!!'
        self.bgpController.show_adv_routes(node, prefix)
        print '\n\n'

    # inbound soft-reconfiguration must be enabled
    def show_recv_route(self, node, prefix):
        print '>>> Display received routes on Node', node, '!!'
        self.bgpController.show_recv_routes(node, prefix)
        print '\n\n'

    def show_learned_route(self, node, prefix):
        print '>>> Display learned BGP routes on Node', node, '!!'
        self.bgpController.show_learned_routes(node, prefix)
        print '\n\n'

    def show_route_map(self, node, route_map):
        print '>>> Display BGP route-map on Node', node, '!!'
        self.bgpController.show_route_map(node, route_map)
        print '\n\n'

    def prefix_announce(self):
        node = chooseNode()
        router_id = home_ASN
        prefix = choosePrefix()
        print '>>> Inject BGP prefix ', prefix, ' on Node', node, '!!'
        self.bgpController.inject_one_prefix(node, router_id, prefix)
        # self.bgpController.show_advertise_routes(node, prefix)
        print '\n\n'

    def prefix_withdraw(self):
        node = chooseNode()
        router_id = home_ASN
        prefix = choosePrefix()
        print '>>> Withdraw BGP prefix ', prefix, ' on Node', node, '!!'
        self.bgpController.remove_one_prefix(node, router_id, prefix)
        # self.bgpController.show_advertise_routes(node, prefix)
        print '\n\n'

    # Flip bgp prefix in certain time interval
    # Can NOT Flap prefix too fast, needs to stick on one announcement for 2h
    def prefix_flip(self, node, router_id, prefix_orin, prefix_new, delay_interval):
        self.bgpController.inject_one_prefix(node, router_id, prefix_new)
        time.sleep(delay_interval)
        self.bgpController.remove_one_prefix(node, router_id, prefix_orin)


def chooseNode():
    quagganodes = [quagga_client, quagga_server]
    title = 'What node is this?: '
    node, index = pick(quagganodes, title)
    return node


def choosePrefix():
    title = 'Choose a prefix: '
    prefix, index = pick(peering_prefix, title)
    return prefix



def unimplemented():
    print("\n Feature not implemented yet. \n")


def main():
    bm = bgpMgmt()

    # node = chooseNode()

    # options = {'0': bm.show_node_prefix(node),
    #            '1': bm.show_all_neighbors(node),
    #            '2': bm.show_node_neighbor(node),
    #            '3': bm.show_adv_route(node, choosePrefix()),
    #            '4': bm.show_recv_route(node, choosePrefix()),
    #            '5': bm.show_learned_route(node, choosePrefix()),
    #            '6': unimplemented(),
    #            '7': bm.prefix_announce(node, home_ASN, choosePrefix()),
    #            '8': bm.prefix_withdraw(node, home_ASN, choosePrefix()),
    #            '9': exit()
    #            }

    # while True:
    #     print("\nBGP MANAGER")
    #     print("0: Show current BGP prefix")
    #     print("1: Show all BGP neighbors")
    #     print("2: Show BGP neighbor")
    #     print("3: Show advertised routes")
    #     print("4: Show received routes")
    #     print("5: Show learned routes")
    #     print("6: Show BGP route map")
    #     print("7: Announce BGP prefix")
    #     print("8: Withdraw BGP prefix")
    #     print("9: Quit")
    #
    #     choice = raw_input("Choose a number to run a command. What do you want to do? >> ")
    #
    #     try:
    #         options[choice]()
    #     except(KeyError):
    #         print("Invalid key pressed. Choose a number 0-9!")

    functions = {0: bm.prefix_announce,
                 1: bm.prefix_withdraw}

    title = 'What do you want to do?: '
    options = ['Announce BGP prefix', 'Withdraw BGP prefix']
    option, index = pick(options, title)
    functions[index]()


if __name__ == '__main__':
    main()
