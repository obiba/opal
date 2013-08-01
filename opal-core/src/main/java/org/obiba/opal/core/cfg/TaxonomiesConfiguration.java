/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.cfg;

import java.io.Serializable;
import java.util.List;

import org.obiba.opal.core.domain.taxonomy.Taxonomy;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class TaxonomiesConfiguration implements OpalConfigurationExtension, Serializable {

  private List<Taxonomy> taxonomies;

  public List<Taxonomy> getTaxonomies() {
    return taxonomies == null ? taxonomies = Lists.newArrayList() : taxonomies;
  }

  public boolean hasTaxonomy(String name) {
    for (Taxonomy t : getTaxonomies()) {
      if (!Strings.isNullOrEmpty(t.getName()) && t.getName().equals(name)) {
        return true;
      }
      if (Strings.isNullOrEmpty(t.getName()) && Strings.isNullOrEmpty(name)) {
        return true;
      }
    }
    return false;
  }

  public void removeTaxonomy(String name) {
    Taxonomy tax = null;
    for (Taxonomy t : getTaxonomies()) {
      if (!Strings.isNullOrEmpty(t.getName()) && t.getName().equals(name)) {
        tax = t;
        break;
      }
      if (Strings.isNullOrEmpty(t.getName()) && Strings.isNullOrEmpty(name)) {
        tax = t;
        break;
      }
    }
    if (tax != null) {
      getTaxonomies().remove(tax);
    }
  }
}
