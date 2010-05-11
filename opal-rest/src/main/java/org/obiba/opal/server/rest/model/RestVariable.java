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

import java.net.URI;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

@XmlRootElement(name = "variable")
@XmlAccessorType(XmlAccessType.FIELD)
public class RestVariable {

  private final String name;

  private final String valueType;

  private final String link;

  // @XmlElementWrapper()
  // @XmlElement(name = "attribute")
  private List<RestAttribute> attributes;

  // @XmlElementWrapper
  // @XmlElement(name = "category")
  private List<RestCategory> categories;

  public RestVariable() {
    this.name = null;
    this.valueType = null;
    this.link = null;
  }

  public RestVariable(URI link, org.obiba.magma.Variable magmaVariable) {
    this.name = magmaVariable.getName();
    this.valueType = magmaVariable.getValueType().getName();
    this.link = link.toString();
    this.attributes = ImmutableList.copyOf(Iterables.transform(magmaVariable.getAttributes(), new Function<Attribute, RestAttribute>() {

      @Override
      public RestAttribute apply(Attribute from) {
        return new RestAttribute(from);
      }
    }));
    this.categories = ImmutableList.copyOf(Iterables.transform(magmaVariable.getCategories(), new Function<Category, RestCategory>() {

      @Override
      public RestCategory apply(Category from) {
        return new RestCategory(from);
      }
    }));
  }

  public String getName() {
    return name;
  }

  public List<RestAttribute> getAttribtues() {
    return attributes;
  }

  public String getLink() {
    return link;
  }

  public String getValueType() {
    return valueType;
  }

  public List<RestCategory> getCategories() {
    return categories;
  }
}
