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
import org.obiba.opal.core.domain.metadata.DataItem;

/**
 * 
 */
@javax.persistence.Entity
public class DataPoint extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @ManyToOne(optional = false)
  private Dataset dataset;

  @ManyToOne(optional = false)
  private DataItem dataItem;

  @Lob
  @Column(length = Integer.MAX_VALUE)
  private String value;

  private Integer occurrence;

  public DataPoint() {

  }

  public DataPoint(Dataset dataset, DataItem item, String value) {
    this(dataset, item, value, null);
  }

  public DataPoint(Dataset dataset, DataItem item, String value, Integer occurrence) {
    this.dataset = dataset;
    this.dataItem = item;
    this.value = value;
    this.occurrence = occurrence;
  }

  public Dataset getDataset() {
    return dataset;
  }

  public DataItem getDataItem() {
    return dataItem;
  }

  public String getValue() {
    return value;
  }

  public Integer getOccurrence() {
    return occurrence;
  }
}
