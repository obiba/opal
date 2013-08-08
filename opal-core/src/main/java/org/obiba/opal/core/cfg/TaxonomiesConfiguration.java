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
import java.util.HashMap;
import java.util.List;

import org.obiba.opal.core.domain.taxonomy.Taxonomy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class TaxonomiesConfiguration implements OpalConfigurationExtension, Serializable {

  private static final long serialVersionUID = -3948159539937931629L;

  private final HashMap<String, Taxonomy> taxonomies = Maps.newHashMap();

  public List<Taxonomy> getTaxonomies() {
    return ImmutableList.copyOf(taxonomies.values());
  }

  public boolean has(String name) {
    return taxonomies.containsKey(name);
  }

  public void remove(String name) {
    taxonomies.remove(name);
  }

  public Taxonomy get(String name) {
    return taxonomies.get(name);
  }

  public void put(Taxonomy taxonomy) {
    taxonomies.put(taxonomy.getName(), taxonomy);
  }

}
