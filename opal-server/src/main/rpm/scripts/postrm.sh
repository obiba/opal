#!/bin/sh
# postrm script for opal
#

set -e

# summary of how this script can be called:
#        * <postrm> `remove'
#        * <postrm> `purge'
#        * <old-postrm> `upgrade' <new-version>
#        * <new-postrm> `failed-upgrade' <old-version>
#        * <new-postrm> `abort-install'
#        * <new-postrm> `abort-install' <old-version>
#        * <new-postrm> `abort-upgrade' <old-version>
#        * <disappearer's-postrm> `disappear' <overwriter>
#          <overwriter-version>
# for details, see http://www.debian.org/doc/debian-policy/ or
# the debian-policy package

case "$1" in
	0)

    rm -rf /run/opal /var/log/opal /tmp/opal
  ;;
esac

exit 0

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