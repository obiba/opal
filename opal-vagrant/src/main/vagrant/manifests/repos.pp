yumrepo { "thehyve":
  #baseurl   => "https://repo.thehyve.nl/content/groups/public",
  baseurl   => "https://repo.thehyve.nl/content/repositories/releases",
  descr     => "Hyve Releases Repository",
  enabled   => 1,
  gpgcheck  => 0
}

package { 'epel-release':
  ensure    => present,
  provider  => rpm,
  source    => 'http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm',
}
