from subprocess import call
from subprocess import check_output


class QuaggaDriver:
    def __init__(self):
        self.path = check_output('sudo find /users/ -type d -name miniNExT', shell=True)
        self.selfPath = self.path.strip() + '/util/mx'
        self.vtysh = 'vtysh -c'

    def inject_one_prefix(self, node, router_id, prefix):
        print(
            self.selfPath + node + 'vtysh -c "configure terminal" -c "router bgp"' + str(
                router_id) + '-c "network"' + str(
                prefix))
        call([self.selfPath, node, 'vtysh', '-c', '\"configure terminal\"', '-c',
              '\"router bgp \"' + str(router_id),
              '-c',
              '\"network \"' + str(prefix)
              ])

    def remove_one_prefix(self, node, router_ASN, prefix):
        call([self.selfPath, node, 'vtysh', '-c', '\"configure terminal\"', '-c',
              '\"router bgp \"' + str(router_ASN),
              '-c',
              '\"no network \"' + str(prefix)
              ])

    def adjust_timer(self, node, router_id, keeplive_interval, holdtime_interval):
        call([self.selfPath, node, 'vtysh', '-c', '\"configure terminal\"', '-c',
              '\"router bgp \"' + str(router_id),
              '-c',
              '\"' + 'timers bgp ' + str(keeplive_interval) + ' ' + str(holdtime_interval) + '\"'
              ])

    def show_adv_routes(self, node, prefix):
        call(
            [self.selfPath, node, 'vtysh', '-c', '\"show ip bgp neighbors \"' + str(prefix) + '\" advertised-routes\"'])

    def show_recv_routes(self, node, prefix):
        call([self.selfPath, node, 'vtysh', '-c', '\"show ip bgp neighbors \"' + str(prefix) + '\" received-routes\"'])

    def show_learned_routes(self, node, prefix):
        call([self.selfPath, node, 'vtysh', '-c', '\"show ip bgp neighbors \"' + str(prefix) + '\" routes\"'])

    def show_route_map(self, node, route_map_name):
        call([self.selfPath, node, 'vtysh', '-c', '\"show ip bgp route-map \"' + str(route_map_name)])

    def show_bgp_all_prefix(self, node):
        call([self.selfPath, node, 'vtysh', '-c', '\"show ip bgp\"'])

    def show_bgp_prefix(self, node, prefix):
        call([self.selfPath, node, 'vtysh', '-c', '\"show ip bgp \"' + str(prefix)])

    def show_bgp_summary(self, node):
        call([self.selfPath, node, 'vtysh', '-c', '\"show ip bgp summary\"'])

    def show_bgp_attributes(self, node):
        call([self.selfPath, node, 'vtysh', '-c', '\"show ip bgp attribute-info\"'])

    def show_bgp_community(self, node):
        call([self.selfPath, node, 'vtysh', '-c', '\"show ip bgp community-info\"'])

    def show_bgp_neighbors(self, node):
        call([self.selfPath, node, 'vtysh', '-c', '\"show ip bgp neighbors\"'])

    def show_bgp_neighbor(self, node, prefix):
        call([self.selfPath, node, 'vtysh', '-c', '\"show ip bgp neighbors \"' + str(prefix)])

    def show_bgp_path(self, node):
        call([self.selfPath, node, 'vtysh', '-c', '\"show ip bgp paths\"'])

    def show_bgp_rsclient(self, node):
        call([self.selfPath, node, 'vtysh', '-c', '\"show ip bgp rsclient summary\"'])

    def show_interface_descrp(self, node):
        call([self.selfPath, node, 'vtysh', '-c', '\"show interface description\"'])

    def show_ipv6_neighbors(self, node):
        call([self.selfPath, node, 'vtysh', '-c', '\"show bgp summary\"'])

    def show_ip_route(self, node):
        call([self.selfPath, node, 'vtysh', '-c', '\"show ip route\"'])

    def show_running_config(self, node):
        call([self.selfPath, node, 'vtysh', '-c', '\"show running-config\"'])

    def show_access_list(self, node, num):
        call([self.selfPath, node, 'vtysh', '-c', '\"show ip access-list \"' + str(num)])

    def show_as_path_access_list(self, node):
        call([self.selfPath, node, 'vtysh', '-c', '\"show ip as-path-access-list\"'])


def main():
    bgpTest = QuaggaDriver()
    bgpTest.show_bgp_neighbors('a1')
    # bgpTest.adjust_timer('a1', 100, 200, 300)
    # bgpTest.inject_one_prefix('a1', 100, '10.0.5.0/24')


if __name__ == '__main__':
    main()
