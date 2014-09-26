/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain.taxonomy;

public class Term extends TaxonomyEntity {

  public Term() {}

  public Term(String name) { setName(name); }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(!(o instanceof Term)) return false;
    Term term = (Term) o;
    return getName().equals(term.getName());
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }
}
