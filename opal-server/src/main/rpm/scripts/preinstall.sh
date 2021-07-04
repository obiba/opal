#!/bin/sh

getent group adm >/dev/null || groupadd -r adm

getent passwd opal >/dev/null || \
	  useradd -r -g nobody -d /var/lib/opal -s /sbin/nologin -c "opal service user" opal
exit 0
