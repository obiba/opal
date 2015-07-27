# Virtualized Opal using Vagrant

This module provides a Vagrant/Puppet setup for hosting Opal.
The current Vagrant/Puppet setup and Opal packaging is targeting a .rpm based Linux distribution (RedHat or CentOS).

## Installation

1. Install Vagrant from

    http://www.vagrantup.com/downloads
2. Download the latest Opal Vagrant bundle from <br> [Latest Opal Vagrant zip release](http://repo.thehyve.nl/service/local/artifact/maven/redirect?r=releases&g=org.obiba.opal.sesi&a=opal-vagrant&e=zip&v=LATEST&c=vagrant) OR <br> [Latest Opal Vagrant zip snapshot (for developers)](http://repo.thehyve.nl/service/local/artifact/maven/redirect?r=snapshots&g=org.obiba.opal.sesi&a=opal-vagrant&e=zip&v=LATEST&c=vagrant)<br> This is a very small file that only contains descriptors on how to prepare the guest VM and required dependencies and system settings<br>
3. Unzip this file to a folder in the host machine (where you want your Vagrant image to be located).<br>
The Vagrantfile defines some important VM settings, but most of them can be overriden locally.<br>
All overridable settings are described in the file 'config.yaml.sample', along with the defaults.<br>
If you need to customize something, create a 'config.yaml' copying from 'config.yaml.sample', edit the values you want and comment/remove all the others.<br>
Please kept all your customization in this config.yaml filee, and don't modify the Vagrantfile directly.<br>
This way we can update/enhance later on the Vagrantfile making sure your settings are kept.<br>
Please refer to section 'Guest VM customization' for concrete examples<br>
4. In the same folder of Vagrantfile (src/main/vagrant), run

    vagrant up


## Guest VM customization

Examples of what you can modify in the config.yaml:

 * the box (guest VM image) to use. The current setup requires it to be a recent RPM based Linux (RedHat or CentOS)
 * if you want box automatic update (better leave the defaults)
 * guest VM to run in headless mode or not
 * aspects of the 'hardware', like number of CPUs, memory, etc..
 * ports forwarded from host to guest VM
 * synchronized folders (existing in the host machine and mounted in the guest VM)

## After booting the VM

Opal is now up and running, and available in the host machine on the configured ports.
This Opal Vagrant VM provides:
* running Opal on the defaults ports (the actual host ports will depend on the Vagrantfile changes)

You can at any moment modify these settings and try others, then relaunch the VM by running

    vagrant reload

To login in the guest VM, run

    vagrant ssh

To shutdown the VM, run

    vagrant halt

And to boot it up again, run

    vagrant up

Please refer to Vagrant documentation for more information at
    http://docs.vagrantup.com/v2/getting-started/index.html

---

# Provisioning using Puppet

Its possible to completely bypass the use of vagrant, and use the Puppet manifests to automate all the installation steps.
This is useful for anybody not interested in having Opal in a VM, but instead have their own RedHat or CentOS box.

1 - Install puppet standalone (assuming a RHEL 6 based OS) by running

    sudo rpm -ivh http://yum.puppetlabs.com/puppetlabs-release-el-6.noarch.rpm
    sudo yum install puppet

2 - Install puppet firewall module

    sudo puppet module install puppetlabs-firewall

3 - Download the latest Opal Vagrant bundle from <br> [Latest Opal Vagrant zip release](http://repo.thehyve.nl/service/local/artifact/maven/redirect?r=releases&g=org.obiba.opal.sesi&a=opal-vagrant&e=zip&v=LATEST&c=vagrant) OR <br> [Latest Opal Vagrant zip snapshot (for developers)](http://repo.thehyve.nl/service/local/artifact/maven/redirect?r=snapshots&g=org.obiba.opal.sesi&a=opal-vagrant&e=zip&v=LATEST&c=vagrant)<br>

4 - Unpack the opal-vagrant-xxxxx-vagrant.zip, and go to the base folder inside the extracted folder

    unzip opal-vagrant-xxxxx-vagrant.zip
    cd <folder>/opal-vagrant-x.y

5 - You should have now be in a folder with a subfolder manifests with the puppet files. run

    sudo puppet apply manifests/redhat.pp


Opal is now installed, along with OpenJDK and the TCP ports 8080 and 8443 are open

---

# Opal Post-Install Configuration

The very fist time you login in Opal, you will be presented with the 'Post-Install Configuration' screen.
Here you should register the datasources to be used in Opal.
There are 2 major types of datasources (identifiers and data), and you should register at least one of each.
So we need at least the identifiers database and one data database.

You can use MySQL, MongoDB, or a combination of both.
These can point to a dedicated database server, or just use the local databases.

PS: You should mark one of the 'Data Databases' as 'Project default storage'. Its not mandatory, but if you don't do it, you have to explicitly specify the database everytime you create a project, and you cannot automatically create the local project (if needed) when using Opal-Opal data sharing.

## Locally install MySQL and setting 2 databases for Opal (optional)

All the work here is done by Puppet.
We have a Puppet file to make it easy to install and setup MySQL for Opal

execute

    sudo puppet module install puppetlabs-mysql
    sudo puppet apply /vagrant/manifests/mysql_redhat.pp

Then create a user with a password, or modify root's password by executing

    mysqladmin -u root password <password>

You will end up with a MySQL installed, with databases opal_ids and opal_data.

## Locally install MongoDB (optional)

We also have a Puppet file to make it easy to install MongoDB for Opal.
This will only install and launch MongoDB.

Unfortunately the Puppet MongoDB module is not stable/rich enough to provide a way to automate the creation of databases and users.
So you need to create manually the MongoDB databases and the respective users.

execute

    sudo puppet apply /vagrant/manifests/mongo_redhat.pp


---

# Service control and updates

## Opal service commands
Opal is registered as a service, so we can run in the guest VM commands like

    sudo stop opal
    sudo start opal
    sudo restart opal

## Update opal (for RedHat based box)

To update opal, we just need to run in the guest VM:

    sudo yum update opal-server

A restart on the Opal service will be needed after an upgrade

---

# Installing R tools using Puppet

We have a Puppet module to automate the installation of opal-r and rserver.
To use it, run the steps in section 'Provisioning using Puppet', and...

1 - In the folder containing subfolders manifests, modules and files, run

    sudo puppet apply --modulepath=modules -e "include opal-rtools"

2 - launch rserver by running

    sudo service rserver start
    

To check if R server admin is running properly, execute

    curl http://localhost:6312/rserver
    
And the result should be 
    
    {"running":true,"encoding":"native","port":6311}

You can also check it in the Opal UI, in menu 'Administration', then 'R', 'Test Connection with R'

PS: This puppet module is oncoifmed to work with RedHat, but there are still some issues to solve in CentOS
