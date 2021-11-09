#!/bin/sh
# postinst script for opal
#

set -e

NAME=opal

[ -r /etc/default/$NAME ] && . /etc/default/$NAME

# Opal file structure on Debian
# /etc/opal: configuration
# /usr/share/opal: executable
# /var/lib/opal: data runtime
# /var/log: logs

rm -f /usr/share/opal
new_release="$(ls -t /usr/share/ |grep opal-server|head -1)"
ln -s /usr/share/${new_release} /usr/share/opal

if [ ! -e /var/lib/opal/conf ] ; then
  ln -s /etc/opal /var/lib/opal/conf
fi

if [ ! -e /var/lib/opal/data ] ; then
  mkdir /var/lib/opal/data
fi

if [ ! -e /var/lib/opal/work ] ; then
  mkdir /var/lib/opal/work
fi

if [ ! -e /var/lib/opal/plugins ] ; then
  mkdir /var/lib/opal/plugins
fi

if [ ! -e /var/lib/opal/extensions ] ; then
  mkdir /var/lib/opal/extensions
fi

if [ ! -e /var/lib/opal/fs ] ; then
  mkdir /var/lib/opal/fs
fi

if [ ! -e /var/lib/opal/logs ] ; then
  mkdir /var/lib/opal/logs
fi

chown -R opal:adm /var/lib/opal /var/log/opal /etc/opal
chmod -R 750      /var/lib/opal /var/log/opal /etc/opal
chmod +x /usr/share/opal/tools/shiro-hasher
find /etc/opal/ -type f | xargs chmod 640

# if upgrading to 2.0, delete old log4j config
if [ -f "/etc/opal/log4j.properties" ]; then
  mv /etc/opal/log4j.properties /etc/opal/log4j.properties.old
fi

# if upgrading to 2.0, move opal-config.xml to data dir
if [ -f "/etc/opal/opal-config.xml" ]; then
  cp /etc/opal/opal-config.xml /etc/opal/opal-config.xml.opal1-backup
  mv /etc/opal/opal-config.xml /var/lib/opal/data/opal-config.xml
fi

# make sure newrelic is removed from defaults
if grep -q newrelic /etc/default/opal; then
  sed -r 's,(\s+-Dnewrelic.config.file=.*)(\s+-javaagent.*)(.*)",\3",g' /etc/default/opal > /tmp/opal.default
  mv /tmp/opal.default /etc/default/opal
  chown opal:adm /etc/default/opal
  chmod 644 /etc/default/opal
fi

exit 0
