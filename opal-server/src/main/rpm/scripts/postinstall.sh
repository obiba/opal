#!/bin/sh
# postinst script for opal
#

set -e

NAME=opal

# summary of how this script can be called:
#        * <postinst> `configure' <most-recently-configured-version>
#        * <old-postinst> `abort-upgrade' <new version>
#        * <conflictor's-postinst> `abort-remove' `in-favour' <package>
#          <new-version>
#        * <postinst> `abort-remove'
#        * <deconfigured's-postinst> `abort-deconfigure' `in-favour'
#          <failed-install-package> <version> `removing'
#          <conflicting-package> <version>
# for details, see http://www.debian.org/doc/debian-policy/ or
# the debian-policy package

installOrUpdate() {

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

  EXPORT_FILE=$OPAL_HOME/data/orientdb/opal-config.export

  JAR_CMD="java -jar /usr/share/opal/tools/lib/opal-config-migrator-*-cli.jar"

  [ -r $OPAL_HOME/data/orientdb/opal-config ] && $JAR_CMD --check $OPAL_HOME/data/orientdb/opal-config && \
      { $JAR_CMD $OPAL_HOME/data/orientdb/opal-config $EXPORT_FILE  && \
          echo "Legacy export completed ..." && \
          mv $OPAL_HOME/data/orientdb/opal-config $OPAL_HOME/data/orientdb/opal-config.bak || exit 1; }

  [ -r $EXPORT_FILE.gz ] && { \
  java -cp "/usr/share/opal/lib/*" -DOPAL_HOME=$OPAL_HOME org.obiba.opal.core.tools.OrientDbUtil import $EXPORT_FILE.gz && \
  rm $EXPORT_FILE.gz && \
  rm -Rf $OPAL_HOME/data/orientdb/opal-config.bak || { mv $OPAL_HOME/data/orientdb/opal-config.bak $OPAL_HOME/data/orientdb/opal-config; \
  exit 1; }; }

  chown -R opal:adm /var/lib/opal /var/log/opal /etc/opal /tmp/opal
  chmod -R 750      /var/lib/opal /var/log/opal /etc/opal/ /tmp/opal
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

  # auto start on reboot
  chkconfig --add opal

  # start opal
  echo "### You can start opal service by executing:"
  echo "sudo /etc/init.d/opal start"

  exit 0
}

case "$1" in
  1)
    installOrUpdate
  ;;

  2)
    installOrUpdate
  ;;

  *)
    echo "postinst called with unknown argument \`$1'" >&2
    exit 1
  ;;
esac

exit 0
