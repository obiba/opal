/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.domain.data;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.obiba.core.domain.AbstractEntity;

/**
 * 
 */
@javax.persistence.Entity
public class DataItem extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @ManyToOne
  private Dataset dataset;

  @Column(length = 2000)
  private String variable;

  @Lob
  @Column(length = Integer.MAX_VALUE)
  private String value;

  private Integer occurrence;

  public DataItem() {

  }

  public DataItem(Dataset dataset, String variable, String value) {
    this(dataset, variable, value, null);
  }

  public DataItem(Dataset dataset, String variable, String value, Integer occurrence) {
    this.dataset = dataset;
    this.variable = variable;
    this.value = value;
    this.occurrence = occurrence;
  }

  public Dataset getDataset() {
    return dataset;
  }

  public String getVariable() {
    return variable;
  }

  public String getValue() {
    return value;
  }

  public Integer getOccurrence() {
    return occurrence;
  }
}
