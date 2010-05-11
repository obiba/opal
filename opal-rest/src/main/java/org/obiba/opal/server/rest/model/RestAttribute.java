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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class RestAttribute {

  private final String name;

  private final String value;

  private final String locale;

  public RestAttribute() {
    this.name = null;
    this.value = null;
    this.locale = null;
  }

  public RestAttribute(org.obiba.magma.Attribute attribute) {
    this.name = attribute.getName();
    this.value = attribute.getValue().toString();
    this.locale = attribute.isLocalised() ? attribute.getLocale().toString() : null;
  }

  public String getName() {
    return name;
  }

  public String getLocale() {
    return locale;
  }

  public String getValue() {
    return value;
  }
}
