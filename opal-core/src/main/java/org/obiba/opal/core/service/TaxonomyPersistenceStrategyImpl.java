/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.springframework.stereotype.Component;

@Component
public class TaxonomyPersistenceStrategyImpl implements TaxonomyPersistenceStrategy {

  @Override
  public void writeTaxonomy(@NotNull String name, @NotNull Taxonomy taxonomy, @Nullable String comment) {
    // TODO
  }

  @Override
  public void removeTaxonomy(@NotNull String name, @Nullable String comment) {
    // TODO
  }

  @Override
  @NotNull
  public Set<Taxonomy> readTaxonomies() {
    return new HashSet<>();
  }
}
