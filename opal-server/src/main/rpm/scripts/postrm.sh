#!/bin/sh
# postrm script for opal
#

set -e

systemctl daemon-reload >/dev/null 2>&1 || :
case "$1" in
	0)
    # Package removal, not upgrade
    userdel -f opal || true
    unlink /usr/share/opal
    # Remove logs and data
    rm -rf /var/lib/opal /var/log/opal /etc/opal /usr/share/opal-*
  ;;
  1)
    # Package upgrade, not removal
    find /usr/share/opal-* -empty -type d -delete
    systemctl try-restart opal.service >/dev/null 2>&1 || :
  ;;
esac