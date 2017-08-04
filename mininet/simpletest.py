import json
import os
import subprocess

import mininet.log as log
from mininet.net import Mininet

from nodes import Floodlight
from topologies.simplenobgptopo import SimpleNoBGPTopo

HOME_FOLDER = os.getenv('HOME')
LOG_PATH = HOME_FOLDER + '/TARN/logs/'


def pp_json(json_thing, sort=True, indents=4):
    if type(json_thing) is str:
        print(json.dumps(json.loads(json_thing), sort_keys=sort, indent=indents))
    else:
        print(json.dumps(json_thing, sort_keys=sort, indent=indents))


if __name__ == '__main__':
    log.setLogLevel('info')

    # Create the network from a given topology without building it yet
    net = Mininet(topo=SimpleNoBGPTopo(), build=False)

    # Add the controllers to the network
    c1 = net.addController(name='c1', controller=Floodlight)
    c2 = net.addController(name='c2', controller=Floodlight)

    # Build the network
    net.build()

    # Start the switches and specify a controller
    s1 = net.getNodeByName('s1')
    s1.start([c1])
    s1.cmd('ovs-vsctl set bridge s1 protocols=OpenFlow15')
    s2 = net.getNodeByName('s2')
    s2.start([c2])
    s2.cmd('ovs-vsctl set bridge s2 protocols=OpenFlow15')

    # Start each controller
    for controller in net.controllers:
        controller.start()

    # Send non blocking commands to each host
    h1 = net.getNodeByName('h1')
    h2 = net.getNodeByName('h2')
    h1.sendCmd('ping -c 10 -i 1 ' + h2.IP() + ' > ' + LOG_PATH + 'h1.log')
    h2.sendCmd('ping -c 10 -i 1 ' + h1.IP() + ' > ' + LOG_PATH + 'h2.log')

    # Wait for all the commands to finish
    results = {}
    for h in net.hosts:
        results[h.name] = h.waitOutput()
    print "hosts finished"

    # Log the flow tables on the switches
    for sw in net.switches:
        subprocess.call('ovs-ofctl dump-flows ' + sw.name + ' -O openflow15 > ' + LOG_PATH + sw.name + '.log',
                        shell=True)

    # Issue some commands to the controllers
    print pp_json(c1.getInfo())
    print pp_json(c1.getASes())
    c1.addAS("3", "10.0.0.0/16")
    print pp_json(c1.getASes())

    # CLI(net)

    # Stop everything in the network
    net.stop()
