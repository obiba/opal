/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain.security;

import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import static com.google.common.base.MoreObjects.toStringHelper;

public class Bookmark {

  public static final Comparator<Bookmark> RESOURCE_COMPARATOR = new Comparator<Bookmark>() {
    @Override
    public int compare(Bookmark bookmark1, Bookmark bookmark2) {
      return bookmark1.getResource().compareTo(bookmark2.getResource());
    }
  };

  private Date created;

  @NotNull
  @NotBlank
  private String resource;

  public Bookmark() {
  }

  public Bookmark(@NotNull String resource) {
    this.resource = resource;
    created = new Date();
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  @NotNull
  public String getResource() {
    return resource;
  }

  public void setResource(@NotNull String resource) {
    this.resource = resource;
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("resource", resource).add("created", created).toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(resource);
  }

  @Override
  @SuppressWarnings("SimplifiableIfStatement")
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj == null || getClass() != obj.getClass()) return false;
    return Objects.equals(resource, ((Bookmark) obj).resource);
  }
}
