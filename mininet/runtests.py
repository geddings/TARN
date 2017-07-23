from topologies.simplenobgptopo import SimpleNoBGPTopo
from mininet.cli import CLI
import mininet.log as log
from mininet.net import Mininet
from mininet.node import RemoteController
from nodes import Floodlight

from time import time
from select import poll, POLLIN
from subprocess import Popen, PIPE


def monitorFiles(outfiles, seconds, timeoutms):
    "Monitor set of files and return [(host, line)...]"
    devnull = open('/dev/null', 'w')
    tails, fdToFile, fdToHost = {}, {}, {}
    for h, outfile in outfiles.iteritems():
        tail = Popen(['tail', '-f', outfile],
                     stdout=PIPE, stderr=devnull)
        fd = tail.stdout.fileno()
        tails[h] = tail
        fdToFile[fd] = tail.stdout
        fdToHost[fd] = h
        # Prepare to poll output files
    readable = poll()
    for t in tails.values():
        readable.register(t.stdout.fileno(), POLLIN)
    # Run until a set number of seconds have elapsed
    endTime = time() + seconds
    while time() < endTime:
        fdlist = readable.poll(timeoutms)
        if fdlist:
            for fd, _flags in fdlist:
                f = fdToFile[fd]
                host = fdToHost[fd]
                # Wait for a line of output
                line = f.readline().strip()
                yield host, line
        else:
            # If we timed out, return nothing
            yield None, ''
    for t in tails.values():
        t.terminate()
    devnull.close()  # Not really necessary

log.setLogLevel('info')
net = Mininet(topo=SimpleNoBGPTopo(), build=False)

c1 = net.addController(name='c1', controller=Floodlight)
c2 = net.addController(name='c2', controller=Floodlight)
net.build()

h1 = net.getNodeByName('h1')
h2 = net.getNodeByName('h2')

s1 = net.getNodeByName('s1')
s1.start([c1])
s2 = net.getNodeByName('s2')
s2.start([c2])

for controller in net.controllers:
    controller.start()

s = '5'
h1.cmdPrint('ping -c %s 10.0.0.2 > ./logs/h1ping.txt &' % s)
h2.cmdPrint('ping -c %s 10.0.0.1 > ./logs/h2ping.txt &' % s)
# s1.cmdPrint('for i in {1..%s}; do {sudo ovs-ofctl dump-flows "s1"} done' % s)
# outfiles, errfiles = {}, {}
# hosts = net.hosts
# for h in hosts:
#     outfiles[h] = './logs/%s.out' % h.name
#     errfiles[h] = './logs/%s.err' % h.name
#     h.cmd('echo >', outfiles[h])
#     h.cmd('echo >', errfiles[h])
#     # Start pings
#     h.cmdPrint('watch', 'ifconfig',
#                '>', outfiles[h],
#                '2>', errfiles[h],
#                '&')
# for h, line in monitorFiles(outfiles, 10, timeoutms=500):
#     if h:
#         log.info('%s: %s\n' % (h.name, line))
#
# for h in hosts:
#     h.cmd('kill %ping')

CLI(net)
net.stop()
