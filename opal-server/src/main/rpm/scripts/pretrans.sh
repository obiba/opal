# clean old init
if [ -e /etc/init.d/opal ]; then
  service opal stop
  chkconfig --del opal
  systemctl daemon-reload
fi
exit 0
