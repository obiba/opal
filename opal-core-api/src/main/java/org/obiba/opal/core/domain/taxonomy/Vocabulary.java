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

import javax.annotation.Nonnull;

public class Vocabulary {

  @Nonnull
  private Term root;

  private boolean repeatable;

  public Vocabulary() {
  }

  public Vocabulary(@Nonnull Term root) {
    this.root = root;
  }

  public String getName() {
    return root.getName();
  }

  @Nonnull
  public Term getRoot() {
    return root;
  }

  public void setRoot(@Nonnull Term root) {
    this.root = root;
  }

  public boolean isRepeatable() {
    return repeatable;
  }

  public void setRepeatable(boolean repeatable) {
    this.repeatable = repeatable;
  }
}
