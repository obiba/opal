#!/bin/bash
# for update from System-V
systemctl preset opal.service
systemctl start opal.service
exit 0