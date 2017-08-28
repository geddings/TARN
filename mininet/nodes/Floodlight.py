import httplib
import json
import shutil
import subprocess

import jprops
import mininet.log as log
import os
from mininet.moduledeps import pathCheck
from mininet.node import Controller
from os import chdir
from os import makedirs
from os import path

HOME_FOLDER = os.getenv('HOME')
LOG_PATH = HOME_FOLDER + '/TARNProject/TARN/logs/'


class Floodlight(Controller):
    # Port numbers used to run Floodlight. These must be unique for every instance.
    # Static class variables are used to keep track of which ports have been used already.
    sync_manager_port = 6009
    openflow_port = 6653
    http_port = 8080
    https_port = 8081

    # Number of Floodlight instances created. Used for naming purposes.
    controller_number = 0

    fl_root_dir = os.path.dirname(os.path.dirname(os.path.dirname(__file__))) + '/floodlight'
    logback_path = fl_root_dir + '/src/main/resources/logback.xml'

    def __init__(self, name,
                 command='java -Dlogback.configurationFile=' + logback_path + ' -jar ' + fl_root_dir + '/target/floodlight.jar',
                 cargs='',
                 ip='127.0.0.1',
                 debug=False,
                 debugPort='',
                 **kwargs):
        # Increment the number of controller instances for naming purposes.
        Floodlight.controller_number += 1

        # Initialize attributes
        self.name = name
        self.properties_path = ''
        self.properties_file = ''

        self.createUniqueFloodlightPropertiesFile()
        self.port = self.openflow_port

        # Create the command that will start Floodlight, including the path to the unique properties file.
        self.command = command + ' -cf ' + self.properties_path + self.properties_file

        # Configure the debug stuff
        if debug:
            self.command = 'java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=' + debugPort + '-jar '\
                           + Floodlight.fl_root_dir + '/target/floodlight.jar' + ' -cf ' + self.properties_path + self.properties_file

        # Initialize the parent class.
        Controller.__init__(self, name, cdir=self.fl_root_dir,
                            command=self.command,
                            cargs=cargs, port=self.openflow_port, ip=ip, **kwargs)



    def start(self):
        """Start <controller> <args> on controller.
           Log to /TARN/cN.log (i.e. c1.log, c2.log) """
        log.info('Starting controller...\n')
        pathCheck(self.command)
        # cout = '/tmp/' + self.name + '.log'
        cout = LOG_PATH + self.name + '.log'
        chdir(self.fl_root_dir)
        self.cmd(self.command + ' ' + self.cargs +
                 ' 1>' + cout + ' 2>' + cout + '&')
        self.execed = False

    def stop(self):
        log.debug('Removing ' + self.name + ' properties file...')
        subprocess.call('rm ' + self.properties_path + self.properties_file, shell=True)
        super(Floodlight, self).stop()

    def getInfo(self):
        """Returns general info about the TARN controller."""
        ret = self.rest_call('wm/tarn/info/json', '', 'GET')
        return ret[2]
    
    def configure(self, lan_port, wan_port):
        """Configures TARN with necessary info."""
        data = {
            "lanport": lan_port,
            "wanport": wan_port
        }
        ret = self.rest_call('/wm/tarn/config/json', data, 'POST')
        return ret[0] == 200

    def getASes(self):
        """Returns all configured Autonomous Systems from the TARN controller."""
        ret = self.rest_call('wm/tarn/as/json', '', 'GET')
        return ret[2]

    def getAS(self, as_number):
        """Returns the specified Autonomous Systems from the TARN controller."""
        ret = self.rest_call('wm/tarn/as/' + as_number + '/json', '', 'GET')
        return ret[2]

    def addAS(self, as_number, internal_prefix):
        """Adds an Autonomous System to the TARN controller with a given AS number and internal prefix."""
        data = {
            "as-number": as_number,
            "internal-prefix": internal_prefix
        }
        ret = self.rest_call('/wm/tarn/as/json', data, 'POST')
        return ret[0] == 200

    def addPrefixToAS(self, as_number, prefix):
        """Adds a prefix to the prefix pool of the given AS number if the AS has already been added to the TARN
        controller."""
        data = {
            "prefix": prefix
        }
        ret = self.rest_call('/wm/tarn/as/' + as_number + '/json', data, 'POST')
        return ret[0] == 200

    def removePrefixFromAS(self, as_number, prefix):
        """Attempts to remove the given prefix from the prefix pool of the given AS number if the AS has already been
        added to the TARN controller."""
        data = {
            "prefix": prefix
        }
        ret = self.rest_call('/wm/tarn/as/' + as_number + '/json', data, 'DELETE')
        return ret[0] == 200
    
    def getHosts(self):
        """Returns all configured hosts from the TARN controller."""
        ret = self.rest_call('wm/tarn/host/json', '', 'GET')
        return ret[2]
    
    def addHost(self, internal_address, member_as):
        """Adds a Host to the TARN controller with a given internal address and member AS."""
        data = {
            "internal-address": internal_address,
            "member-as": member_as
        }
        ret = self.rest_call('/wm/tarn/host/json', data, 'POST')
        return ret[0] == 200

    def setLanPort(self, port):
        data = {
            "localport": str(port)
        }
        ret = self.rest_call('/wm/randomizer/config/json', data, 'POST')
        return ret[0] == 200

    def setWanPort(self, port):
        data = {
            "wanport": str(port)
        }
        ret = self.rest_call('/wm/randomizer/config/json', data, 'POST')
        return ret[0] == 200

    def createUniqueFloodlightPropertiesFile(self):
        """
        Creates a unique properties file for the particular Floodlight instance.
        Each file is put in the 'properties' folder in the floodlight directory.
        Static class attributes keep track of the current port number to use.
        :return: None
        """

        # The path to the properties file to be copied and the name of the file
        old_path = Floodlight.fl_root_dir + '/src/main/resources/'
        old_file = 'floodlightdefault.properties'

        # The path where the new properties file will be located and the name of the file
        new_path = Floodlight.fl_root_dir + 'properties/'
        new_file = 'floodlight' + str(Floodlight.controller_number) + '.properties'

        # Set the instance attributes so that the instance can know where its associated properties file is
        self.properties_path = new_path
        self.properties_file = new_file

        # Check if the new path already exists. If not, then create it
        if not path.exists(new_path):
            makedirs(new_path)

        # Copy the old properties file to the new location with the new name
        shutil.copy(old_path + old_file,
                    new_path + new_file)

        # Open the new properties file and scan it for the ports that need to be changed
        with open(new_path + new_file) as fp:
            properties = jprops.load_properties(fp)

            http = [key for key, value in properties.items() if key.endswith('httpPort')][0]
            https = [key for key, value in properties.items() if key.endswith('httpsPort')][0]
            openflow = [key for key, value in properties.items() if key.endswith('openFlowPort')][0]
            syncmanager = [key for key, value in properties.items() if key.endswith('SyncManager.port')][0]

            properties[http] = str(Floodlight.http_port + 10)
            properties[https] = str(Floodlight.https_port + 10)
            properties[openflow] = str(Floodlight.openflow_port + 10)
            properties[syncmanager] = str(Floodlight.sync_manager_port + 10)

            # Update the class attributes so that everyone knows what ports are available now
            Floodlight.http_port += 10
            Floodlight.https_port += 10
            Floodlight.openflow_port += 10
            Floodlight.sync_manager_port += 10

            self.http_port = Floodlight.http_port
            self.openflow_port = Floodlight.openflow_port

            log.debug('Ports being used in controller ' + self.name + ' property file...\n')
            log.debug(http + ' = ' + properties[http] + '\n')
            log.debug(https + ' = ' + properties[https] + '\n')
            log.debug(openflow + ' = ' + properties[openflow] + '\n')
            log.debug(syncmanager + ' = ' + properties[syncmanager] + '\n')

        # Write the updated ports to the new properties file
        with open(new_path + new_file, 'w') as fp:
            # print 'Writing to file ' + new_file
            jprops.store_properties(fp, properties)

    def rest_call(self, path, data, action):
        headers = {
            'Content-type': 'application/json',
            'Accept': 'application/json',
        }
        body = json.dumps(data)

        conn = httplib.HTTPConnection('localhost', self.http_port)
        conn.request(action, path, body, headers)
        response = conn.getresponse()

        ret = (response.status, response.reason, response.read())
        conn.close()
        return ret


def isFloodlightInstalled():
    """
    This is a helper function to determine whether floodlight has been installed.
    :return: true or false
    """
    if not path.isdir(Floodlight.fl_root_dir):
        log.debug('Floodlight is not installed.\n')
        return False
    else:
        log.debug('Floodlight has been installed.\n')
        return True


def installFloodlight():
    """
    Installs floodlight in the parent of the current directory.
    :return: none
    """
    log.info('Installing Floodlight...\n')
    # Install the EAGER version of Floodlight
    subprocess.call('git clone http://github.com/geddings/TARN ' + Floodlight.fl_root_dir, shell=True)
    chdir(Floodlight.fl_root_dir)
    subprocess.call('sudo ant', shell=True)
    chdir(path.abspath(path.pardir))


if __name__ == "__main__":
    log.setLogLevel('info')
