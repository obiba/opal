/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.domain.metadata;

import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.ManyToOne;

import org.obiba.core.domain.AbstractEntity;

/**
 * 
 */
@javax.persistence.Entity
public class DataItemAttribute extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @ManyToOne(optional = false)
  private DataItem dataItem;

  @Column(nullable = false, length = 2000)
  private String name;

  @Column(length = Integer.MAX_VALUE)
  private String value;

  @Column(length = 10)
  private Locale locale;

  @Column(length = 10)
  private String type;

  public DataItemAttribute() {
  }

  public DataItemAttribute(DataItem dataItem, String name, Object value) {
    this(dataItem, name, value, null);
  }

  public DataItemAttribute(DataItem dataItem, String name, Object value, Locale locale) {
    this.dataItem = dataItem;
    this.name = name;
    this.locale = locale;
    if(value != null) {
      this.value = value.toString();
      this.type = value.getClass().getSimpleName();
      // case of anonymous classes
      if(type.length() == 0) {
        this.type = "String";
      }
    }
  }

  public DataItem getDataItem() {
    return dataItem;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public Locale getLocale() {
    return locale;
  }

  public String getType() {
    return type;
  }
}
