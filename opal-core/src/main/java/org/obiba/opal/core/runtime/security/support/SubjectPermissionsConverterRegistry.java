/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.security.support;

import java.util.List;
import java.util.Set;

import org.obiba.opal.core.runtime.security.SubjectPermissionConverter;
import org.obiba.opal.core.service.SubjectAclService.Permissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 *
 */
@Component
public class SubjectPermissionsConverterRegistry {

  private final Set<SubjectPermissionConverter> converters;

  @Autowired
  public SubjectPermissionsConverterRegistry(Set<SubjectPermissionConverter> converters) {
    this.converters = converters;
  }

  public Iterable<String> convert(Iterable<Permissions> permissions) {
    List<String> perms = Lists.newArrayList();
    for(Permissions sp : permissions) {
      for(String p : sp.getPermissions()) {
        Iterables.addAll(perms, convert(sp.getDomain(), sp.getNode(), p));
      }
    }
    return perms;
  }

  public Iterable<String> convert(String domain, String node, String permission) {
    for(SubjectPermissionConverter converter : converters) {
      if(converter.canConvert(domain, permission)) {
        return converter.convert(domain, node, permission);
      }
    }
    // default
    return Lists.newArrayList(domain + ":" + node + ":" + permission);
  }

}
