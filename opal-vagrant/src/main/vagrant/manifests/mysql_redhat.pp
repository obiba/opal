$mysql_pkg = 'mysql-community-server'

package { 'mysql-community-release':
  ensure    => present,
  provider  => rpm,
  source    => 'http://dev.mysql.com/get/mysql-community-release-el6-5.noarch.rpm',
} -> package { $mysql_pkg:
  ensure    => installed,
}

service { 'mysqld':
  ensure  => running,
  require => Package[$mysql_pkg]

} -> mysql_database { 'opal_ids':
  ensure    => present,
  charset   => 'utf8',

} -> mysql_database { 'opal_data':
  ensure    => present,
  charset   => 'utf8',
}
