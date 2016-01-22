#!/bin/sh

getent group adm >/dev/null || groupadd -r adm

$(getent passwd opal >/dev/null)

if [ $? != 0 ]; then
    useradd -r -g adm -d /var/lib/opal -s /sbin/nologin \
        -c "User for Opal" opal
else

  # stop the service if running
  if service opal status > /dev/null; then
    if which service >/dev/null 2>&1; then
      service opal stop
    elif which invoke-rc.d >/dev/null 2>&1; then
      invoke-rc.d opal stop
    else
      /etc/init.d/opal stop
    fi
  fi

  usermod -d /var/lib/opal opal
fi

exit 0