
$startovs = <<SCRIPT
    sudo ovsdb-server /usr/local/etc/openvswitch/conf.db \
    --remote=punix:/usr/local/var/run/openvswitch/db.sock \
    --remote=db:Open_vSwitch,Open_vSwitch,manager_options \
    --private-key=db:Open_vSwitch,SSL,private_key \
    --certificate=db:Open_vSwitch,SSL,certificate \
    --bootstrap-ca-cert=db:Open_vSwitch,SSL,ca_cert --pidfile --detach --log-file
    
    sudo ovs-vsctl --no-wait init
    sudo ovs-vswitchd --pidfile --detach
    sudo ovs-vsctl show
    sudo ovs-vsctl --version
SCRIPT

Vagrant.configure("2") do |config|
  config.vm.box = "geddings/mininext"
  config.vm.box_version = "0.0.1"
  
  config.vm.provider "virtualbox" do |v|
      # v.name = "tarn"
      # v.customize ["modifyvm", :id, "--cpuexecutioncap", "80"]
      v.customize ["modifyvm", :id, "--memory", "2048"]
  end

  ## Guest config
  config.vm.hostname = "tarn"
  config.vm.network :private_network, type: "dhcp"
  #config.vm.network "forwarded_port", guest: 8080, host: 8080, auto_correct: true


  ## Provisioning
  config.vm.provision "shell", inline: $startovs

  ## SSH config
  config.ssh.forward_x11 = true

  config.vm.synced_folder "./", "/home/vagrant/TARN" #, id:"mininext", create: true, group: "vagrant", owner: "vagrant"

end
