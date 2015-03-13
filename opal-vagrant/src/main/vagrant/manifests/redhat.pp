$jre = 'java-1.7.0-openjdk'

$packages = [ $jre ]

package { $packages:
	ensure => installed,
}

yumrepo { "opal":
  #baseurl   => "https://repo.thehyve.nl/content/groups/public",
  baseurl   => "https://repo.thehyve.nl/content/repositories/snapshots",
  descr     => "Opal Repository",
  enabled   => 1,
  gpgcheck  => 0
} -> package { 'opal-server':
  ensure    => latest,
}

yumrepo { "thehyve":
#baseurl   => "https://repo.thehyve.nl/content/groups/public",
  baseurl   => "https://repo.thehyve.nl/content/repositories/releases",
  descr     => "Hyve Releases Repository",
  enabled   => 1,
  gpgcheck  => 0
} -> package { 'epel-release':
  ensure    => present,
  provider  => rpm,
  source    => 'http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm',
} -> package { 'opal-python-client':
  ensure    => latest,
}


firewall { "900 accept opal ports":
  proto     => "tcp",
  port      => [8080, 8443],
  action    => "accept",
}

$java_sec = '/usr/lib/jvm/jre-1.7.0/lib/security/java.security'

Exec {
	path => '/bin:/usr/bin',
}

exec { 'disable-nss':
  command => "sed -i 's/^\\([^#].*\\/nss.cfg\\)$/#\\1/' '$java_sec'",
	unless  => "grep '^#security\\.provider\\.10' '$java_sec'",
	require => Package[$jre],
} #~> Service['opal']


