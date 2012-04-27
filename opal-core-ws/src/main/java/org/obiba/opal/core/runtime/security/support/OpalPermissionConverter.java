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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.obiba.opal.core.runtime.security.SubjectPermissionConverter;
import org.obiba.opal.web.model.Opal.AclAction;

/**
 * Convert a opal domain permission as a set of magma domain permissions.
 */
public abstract class OpalPermissionConverter implements SubjectPermissionConverter {

  @Override
  public boolean canConvert(String domain, String permission) {
    if(domain == null || domain.equals("opal") == false || permission == null) return false;
    for(AclAction action : AclAction.values()) {
      if(action.toString().equals(permission)) {
        return hasPermission(action);
      }
    }
    return false;
  }

  protected abstract boolean hasPermission(AclAction action);

  protected static String magmaConvert(String magmaNode, String permission, String... args) {
    String node = magmaNode;
    if(args != null) {
      for(int i = 0; i < args.length; i++) {
        node = node.replace("{" + i + "}", args[i]);
      }
    }
    return "magma:" + node + ":" + permission;
  }

  protected static String[] args(String node, String patternStr) {
    // Compile and use regular expression
    Pattern pattern = Pattern.compile(patternStr);
    Matcher matcher = pattern.matcher(node);
    String[] args;
    if(matcher.find()) {
      args = new String[matcher.groupCount()];
      // Get all groups for this match
      for(int i = 1; i <= matcher.groupCount(); i++) {
        args[i - 1] = matcher.group(i);
      }
    } else {
      args = new String[0];
    }

    return args;
  }

}
