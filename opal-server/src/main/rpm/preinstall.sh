#!/bin/sh

getent group adm >/dev/null || groupadd -r adm
getent passwd opal >/dev/null || \
    useradd -r -g adm -d /home/opal -s /sbin/nologin \
    -c "User for Opal" opal
exit 0
