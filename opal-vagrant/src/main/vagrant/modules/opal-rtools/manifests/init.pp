class opal-rtools () {
  yumrepo { "thehyve":
    baseurl   => "https://repo.thehyve.nl/content/repositories/releases",
    descr     => "Hyve Releases Repository",
    enabled   => 1,
    gpgcheck  => 0
  } -> package { 'epel-release':
    ensure    => present,
    provider  => rpm,
    source    => 'http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm',
  } -> package { 'tcl':
    ensure  => present,
  } -> package { 'R':
    ensure  => present,
  }

  if $::operatingsystem == 'CentOS' {
    package { 'curl-devel':
      ensure  => present,
    }
  }

  file { ['/var/lib/rserve',
	'/var/lib/rserve/work/',
	'/var/lib/rserve/work/R',
	'/var/lib/rserve/conf']:
    ensure => 'directory',
 
  } -> file { '/var/lib/rserve/logs': 
    ensure => 'directory',
    mode   => 777,
 
  } -> exec { 'unzip_rserver_admin':
    command => "wget http://download.obiba.org/rserver-admin/stable/rserver-admin-1.0.0.zip && unzip rserver-admin-1.0.0.zip",
    cwd     => '/var/lib/rserve',
    path    => '/bin:/usr/bin',
    unless  => "test -d /var/lib/rserve/rserver-admin-1.0.0",

  } -> user { 'rserver':
    ensure  => present,
    system  => true,
    home    => '/var/lib/rserve',

  } -> file { '/etc/init.d/rserver':
    ensure  => file,
    source  => 'puppet:///modules/opal-rtools/rserver',
    mode    => 744,
  
  } -> file { '/var/lib/rserve/packages.R':
    ensure  => file,
    source  => 'puppet:///modules/opal-rtools/packages.R',

  } -> exec { 'install_R_packages':
    command => "R -q -f packages.R > logs/install_packages.log",
    cwd     => '/var/lib/rserve',
    path    => '/bin:/usr/bin',
/*
  } -> service { 'rserver':
    ensure  => running,
    enable  => true,
*/ 
  }
} 

 
