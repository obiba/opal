#!/bin/sh

getent group adm >/dev/null || groupadd -r adm

$(getent passwd opal >/dev/null)

if [ $? != 0 ]; then
    useradd -r -g adm -d /var/lib/opal -s /sbin/nologin \
        -c "User for Opal" opal
else
    usermod -d /var/lib/opal opal
fi

exit 0