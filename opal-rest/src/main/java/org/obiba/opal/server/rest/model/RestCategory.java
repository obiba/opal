/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.rest.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

@XmlAccessorType(XmlAccessType.FIELD)
public class RestCategory {

  private final String name;

  private final boolean missing;

  private List<RestAttribute> attributes;

  public RestCategory() {
    this.name = null;
    this.missing = false;
  }

  public RestCategory(Category category) {
    this.name = category.getName();
    this.missing = category.isMissing();
    this.attributes = ImmutableList.copyOf(Iterables.transform(category.getAttributes(), new Function<Attribute, RestAttribute>() {

      @Override
      public RestAttribute apply(Attribute from) {
        return new RestAttribute(from);
      }
    }));
  }

  public String getName() {
    return name;
  }

  public boolean isMissing() {
    return missing;
  }

  public List<RestAttribute> getAttribtues() {
    return attributes;
  }
}
