import subprocess as sub
from subprocess import call
import shlex
import StringIO
import os

HOME_FOLDER = os.getenv('HOME')
LOG_PATH = HOME_FOLDER + '/TARNProject/TARN/logs/'

class DataCollector():

    def __init__(self):
        return

    def tcpdump_on_intf(self, intf):
        call('sudo tcpdump -i ' + intf + ' -n arp or icmp -w ' + LOG_PATH + intf + '.pcap &', shell=True)


    def log_flows_on_switch(self, sw):
        call('ovs-ofctl dump-flows ' + sw.name + ' -O openflow15 > ' + LOG_PATH + sw.name + '.log', shell=True)

