
Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/trusty64"

  config.vm.provider "virtualbox" do |v|
      v.name = "tarn"
      # v.customize ["modifyvm", :id, "--cpuexecutioncap", "80"]
      v.customize ["modifyvm", :id, "--memory", "2048"]
  end

  ## Guest config
  config.vm.hostname = "tarn"
  config.vm.network :private_network, type: "dhcp"
  #config.vm.network "forwarded_port", guest: 8080, host: 8080, auto_correct: true


  ## Provisioning
  config.vm.provision "shell", path: "./setup/install_dependencies"
  config.vm.provision "shell", path: "./setup/install_java"
  config.vm.provision "shell", path: "./setup/install_ovs"
  config.vm.provision "shell", path: "./setup/start_ovs"
  config.vm.provision "shell", path: "./setup/install_mininet"
  config.vm.provision "shell", path: "./setup/install_mininext"
  #config.vm.provision "shell", path: "./setup/install_floodlight"
  config.vm.provision "shell", path: "./setup/cleanup"

  ## SSH config
  config.ssh.forward_x11 = true

  config.vm.synced_folder "./", "/home/vagrant/TARN" #, id:"mininext", create: true, group: "vagrant", owner: "vagrant"
  #config.vm.synced_folder "~/Documents/Work/TARN", "/home/vagrant/TARN" #, id:"floodlight", create: true, group: "vagrant", owner: "vagrant" 

end
