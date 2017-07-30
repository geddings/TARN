import os
import subprocess
import time

import mininet.log as log
from mininet.net import Mininet

from nodes import Floodlight
from topologies.simplenobgptopo import SimpleNoBGPTopo

HOME_FOLDER = os.getenv('HOME')
LOG_PATH = HOME_FOLDER + '/TARN/logs/'

if __name__ == '__main__':
    log.setLogLevel('info')
    net = Mininet(topo=SimpleNoBGPTopo(), build=False)
    # c1 = net.addController(name='c1', controller=RemoteController, ip='130.127.39.221', port=6653)  #nodes.Floodlight)
    c1 = net.addController(name='c1', controller=Floodlight)
    c2 = net.addController(name='c2', controller=Floodlight)
    net.build()
    s1 = net.getNodeByName('s1')
    s1.start([c1])
    s1.cmd('ovs-vsctl set bridge s1 protocols=OpenFlow15')
    s2 = net.getNodeByName('s2')
    s2.start([c2])
    s2.cmd('ovs-vsctl set bridge s2 protocols=OpenFlow15')
    for controller in net.controllers:
        controller.start()
    h1 = net.getNodeByName('h1')
    h2 = net.getNodeByName('h2')
    h1.sendCmd('ping -c 10 -i 1 ' + h2.IP() + ' > ' + LOG_PATH + 'h1.log')
    h2.sendCmd('ping -c 10 -i 1 ' + h1.IP() + ' > ' + LOG_PATH + 'h2.log')
    time.sleep(10)
    for sw in net.switches:
        subprocess.call('ovs-ofctl dump-flows ' + sw.name + ' -O openflow15 > ' + LOG_PATH + sw.name + '.log',
                        shell=True)
    net.stop()