import time

from bgp_router_cntl import BGPRTCntl


class bgpMgmt:
    def __init__(self):
        self.bgpController = BGPRTCntl()



    def show_node_prefix(self, node):
        print 'Display Current BGP Prefix on Node', node, '!!'
        self.bgpController.show_bgp_all_prefix(node)
        print '\n\n'



    # Flip bgp prefix in certain time interval
    def flip_node_prefix(self, node, router_id, prefix_orin, prefix_new, delay_interval):
        self.bgpController.inject_one_prefix(node, router_id, prefix_new)
        time.sleep(delay_interval)
        self.bgpController.remove_one_prefix(node, router_id, prefix_orin)



def main():
    bgpManager = bgpMgmt()
    bgpManager.show_node_prefix('a1')
    bgpManager.flip_node_prefix('a1', 100, '10.0.1.0/24', '10.0.10.0/24', 60)
    bgpManager.show_node_prefix('a1')


if __name__ == '__main__':
    main()
