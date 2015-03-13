
yumrepo { "mongodb":
  baseurl   => "http://downloads-distro.mongodb.org/repo/redhat/os/x86_64/",
  descr     => "MongoDB Repository",
  enabled   => 1,
  gpgcheck  => 0
} -> package { 'mongodb-org':
  ensure    => latest,
}
-> service { 'mongod':
  ensure  => running,
}
/*

class {'::mongodb::globals':
  manage_package_repo => true,
}
-> class {'::mongodb::server':
  auth          => true,
}

mongodb_database { opal_ids:
  ensure        => present,
}
->
mongodb_database { opal_data:
  ensure        => present,
}
*/

/*
mongodb_user { 'opal_ids':
  #name          => 'opal',
  ensure        => present,
  password_hash => mongodb_password('opal_ids', 'password'),
  database      => opal_ids,
  roles         => ['readWrite', 'dbAdmin'],
}

mongodb::db { 'opal_ids':
  user          => 'opal',
  password_hash => mongodb_password('opal', 'password'),
}
-> mongodb::db { 'opal_data':
  user          => 'opal',
  password_hash => mongodb_password('opal', 'password'),
}
*/
