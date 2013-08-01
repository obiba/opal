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

import java.util.List;

import org.obiba.opal.core.domain.taxonomy.Taxonomy;

/**
 * Create, update and delete {@link Taxonomy}.
 */
public interface TaxonomyService {

  List<Taxonomy> getTaxonomies();

  boolean hasTaxonomy(String name);

  void removeTaxonomy(String name);

  void addOrReplaceTaxonomy(Taxonomy taxonomy);

  Taxonomy getOrCreateTaxonomy(String name);

  Taxonomy getTaxonomy(String name);
}
