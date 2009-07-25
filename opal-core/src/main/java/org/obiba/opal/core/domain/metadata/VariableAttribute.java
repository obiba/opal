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
public class VariableAttribute extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @ManyToOne
  private Variable variable;

  @Column(nullable = false, length = 2000)
  private String name;

  @Column(length = Integer.MAX_VALUE)
  private String value;

  @Column(length = 10)
  private Locale locale;

  public VariableAttribute() {
  }

  public VariableAttribute(Variable variable, String name, String value) {
    this(variable, name, value, null);
  }

  public VariableAttribute(Variable variable, String name, String value, Locale locale) {
    this.variable = variable;
    this.name = name;
    this.value = value;
    this.locale = locale;
  }

  public Variable getVariable() {
    return variable;
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
}
