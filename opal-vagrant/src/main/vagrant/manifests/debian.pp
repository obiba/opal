include apt

$packages = [ 'openjdk-7-jre', 'mysql-server-5.5'  ]
package { $packages:
	ensure => installed,
}

apt::source { 'opal':
	location    => 'http://apt.thehyve.net/opal',
	release     => 'stable',
  	repos       => 'main',
  	key         => '3375DA21',
    key_server  => 'keyserver.ubuntu.com',
    include_src => false,
    before      => [Package['opal'], Package['opal-rserver']]
}

package { 'opal':
	ensure => latest,
} ~>
service { 'opal':
	ensure => running,
	enable => true,
}

package { 'opal-rserver':
	ensure => latest,
} ~>
service { 'rserver':
	ensure => running,
	enable => true,
}

$java_sec = '/usr/lib/jvm/java-7-openjdk-amd64/jre/lib/security/java.security'

Exec {
	path => '/bin:/usr/bin',
}

exec { 'disable-nss':
    command => "sed -i 's/^\\([^#].*\\/nss.cfg\\)$/#\\1/' '$java_sec'",
	unless  => "grep '^#security\\.provider\\.10' '$java_sec'",
	require => Package['openjdk-7-jre'],
} ~> Service['opal']
