
$startovs = <<SCRIPT
    sudo /usr/local/share/openvswitch/scripts/ovs-ctl --system-id=random start
    sudo ovs-vsctl --version
SCRIPT

Vagrant.configure("2") do |config|
  config.vm.box = "geddings/mininext"
  config.vm.box_version = "1.0.0"
  
  config.vm.provider "virtualbox" do |v|
      # v.name = "tarn"
      # v.customize ["modifyvm", :id, "--cpuexecutioncap", "80"]
      v.customize ["modifyvm", :id, "--memory", "4096"]
  end

  ## Guest config
  config.vm.hostname = "tarn"
  config.vm.network :private_network, type: "dhcp"
  #config.vm.network "forwarded_port", guest: 8080, host: 8080, auto_correct: true


  ## Provisioning
  config.vm.provision "shell", inline: $startovs

  ## SSH config
  config.ssh.forward_x11 = true

  config.vm.synced_folder "./", "/home/vagrant/TARN", group: "root", owner: "root"
  config.vm.synced_folder "./mininet/topologies/PEERing/configs", "/home/vagrant/TARN/mininet/topologies/PEERing/configs", group: "quaggavty", owner: "quagga", mount_options: ["dmode=775,fmode=775"]
  config.vm.synced_folder "./mininet/topologies/PEERING-Test/configs", "/home/vagrant/TARN/mininet/topologies/PEERING-Test/configs", group: "quaggavty", owner: "quagga", mount_options: ["dmode=775,fmode=775"]

end
