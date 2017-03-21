/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.security;

import com.google.common.collect.Lists;
import org.obiba.opal.web.model.Opal;

public class VCFStorePermissionsConverter extends OpalPermissionConverter {
  @Override
  public Iterable<String> convert(String domain, String node, String permission) {
    return Permission.valueOf(permission.toUpperCase()).convert(node);
  }

  @Override
  protected boolean hasPermission(Opal.AclAction action) {
    return false;
  }

  public enum Permission {
    VCF_STORE_VIEW {
      @Override
      public Iterable<String> convert(String node) {
        String[] args = args(node, "/project/(.+)/vcf-store/vcfs");
        return Lists.newArrayList(toRest("/project/{0}/vcf-store/vcfs", "GET", args));
      }
    },
    VCF_STORE_EDIT {
      @Override
      public Iterable<String> convert(String node) {
        String[] args = args(node, "/project/(.+)/vcf-store/vcfs");
        return Lists.newArrayList(toRest("/project/{0}/vcf-store/vcfs", "GET", args),
          toRest("/project/{0}/commands/_export_vcf", "POST", args));
      }
    },
    VCF_STORE_ALL {
      @Override
      public Iterable<String> convert(String node) {
        String[] args = args(node, "/project/(.+)/vcf-store/vcfs");
        return Lists.newArrayList(toRest("/project/{0}/vcf-store/vcfs", "GET", args),
          toRest("/project/{0}/commands/_export_vcf", "POST", args),
          toRest("/project/{0}/commands/_import_vcf", "POST", args),
          toRest("/project/{0}/commands/_import_vcf/samples", "PUT:GET/*", args));
      }
    };

    public abstract Iterable<String> convert(String node);
  }
}
