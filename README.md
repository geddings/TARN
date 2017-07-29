![TARN](docs/TARNLogo.png) 
====================================
[![Build Status](https://travis-ci.org/geddings/TARN.svg?branch=develop)](https://travis-ci.org/geddings/TARN)
# Installation
### Prerequisites
- [Download and install VirtualBox.](https://www.virtualbox.org)
- [Download and install Vagrant, a virtual machine environment manager.](https://www.vagrantup.com)

### Building the environment
- Clone the TARN project: `git clone http://github.com/geddings/TARN`
- Change directories: `cd TARN/`
- Start the Vagrant build process: `vagrant up`
  - _Vagrant commands must be run in the directory that contains the **Vagrantfile**_
- Once built, login to the TARN VM using: `vagrant ssh`
  - You can issue single commands to the TARN VM like `vagrant ssh -c 'ifconfig -a'`
  - _Vagrant may ask you for a password. The default is **vagrant**_
- To pause the VM use `vagrant suspend` and to bring it start it again `vagrant resume`

### Running TARN
There are multiple topologies in which TARN can be run with. The following commands assume that you are in the home directory.

- To run a two host, no bgp topology: `sudo python TARN/mininet/topologies/simplenobgptopo.py`
- To run a two AS, bgp topology: `sudo python TARN/mininet/topologies/2-AS-ebgp/start.py`

# Links

**[TARN Floodlight module](https://github.com/geddings/TARN/tree/develop/floodlight/src/main/java/net/floodlightcontroller/tarn)**

**[PEERING BGP testbed](https://peering.usc.edu)**
