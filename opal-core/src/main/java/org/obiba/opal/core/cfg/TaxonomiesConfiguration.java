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
import java.util.Map;

import org.obiba.opal.core.domain.taxonomy.Taxonomy;

import com.google.common.collect.Lists;

public class TaxonomiesConfiguration implements OpalConfigurationExtension, Serializable {

  private static final long serialVersionUID = -3948159539937931629L;

  private Map<String, Taxonomy> taxonomies;

  public List<Taxonomy> getTaxonomies() {
    return Lists.newArrayList(taxonomies.values());
  }

  public boolean hasTaxonomy(String name) {
    return taxonomies.containsKey(name);
  }

  public void removeTaxonomy(String name) {
    taxonomies.remove(name);
  }

  public Taxonomy getTaxonomy(String name) {
    return taxonomies.get(name);
  }

  public void addOrReplaceTaxonomy(Taxonomy taxonomy) {
    taxonomies.put(taxonomy.getName(), taxonomy);
  }

  public Taxonomy getOrCreateTaxonomy(String name) {
    if(!taxonomies.containsKey(name)) {
      taxonomies.put(name, new Taxonomy(name));
    }
    return getTaxonomy(name);
  }
}
