/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.variable.view;

import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaxonomyAttributes extends HashMap<String, Map<String, List<String>>> {

  void put(TaxonomyDto taxonomy, VocabularyDto vocabulary, TermDto term) {
    put(taxonomy, vocabulary, term.getName());
  }

  void put(TaxonomyDto taxonomy, VocabularyDto vocabulary, String value) {
    if (!containsKey(taxonomy.getName()))
      put(taxonomy.getName(), new HashMap<String, List<String>>());
    if (!get(taxonomy.getName()).containsKey(vocabulary.getName()))
      get(taxonomy.getName()).put(vocabulary.getName(), new ArrayList<String>());
    if (!get(taxonomy.getName()).get(vocabulary.getName()).contains(value))
      get(taxonomy.getName()).get(vocabulary.getName()).add(value);
  }

}
