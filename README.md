![TARN](docs/TARNLogo.png) 
====================================
# Status
Master | Develop
:---: | :---:
[![build status](http://130.127.88.99/tarn/TARN/badges/master/build.svg)](http://130.127.88.99.com/tarn/tarn/commits/master) | [![build status](http://130.127.88.99/tarn/TARN/badges/develop/build.svg)](http://130.127.88.99.com/tarn/tarn/commits/develop)

# Installation
### Prerequisites
- [Download and install VirtualBox.](https://www.virtualbox.org)
- [Download and install Vagrant, a virtual machine environment manager.](https://www.vagrantup.com)

### Building the environment
- Clone the TARN project: `git clone http://130.127.88.99/tarn/tarn`
- Change directories: `cd TARN/`
- Start the Vagrant build process: `vagrant up`
  - _Vagrant commands must be run in the directory that contains the **Vagrantfile**_
- Once built, login to the TARN VM using: `vagrant ssh`
  - You can issue single commands to the TARN VM like `vagrant ssh -c 'ifconfig -a'`
  - _Vagrant may ask you for a password. The default is **vagrant**_
- To pause the VM use `vagrant suspend` and to bring it start it again `vagrant resume`

### Running TARN
There are multiple topologies in which TARN can be run with. The following commands assume that you are in the home directory.

- To run a simple, one-way randomization example: `sudo python ./examples/oneway.py`
- To run a simple, two-way randomization example: `sudo python ./examples/twoway.py`

# TLDR;
```
git clone http://130.127.88.99/tarn/tarn
cd tarn
vagrant up
vagrant ssh -c "sudo python ./TARN/examples/oneway.py"
```

# Links
**[TARN Wiki - Contains info on REST API](http://130.127.88.99/tarn/TARN/wikis/home)**

**[TARN Floodlight module](http://130.127.88.99/tarn/TARN/tree/master/floodlight/src/main/java/net/floodlightcontroller/tarn)**

**[PEERING BGP testbed](https://peering.usc.edu)**
