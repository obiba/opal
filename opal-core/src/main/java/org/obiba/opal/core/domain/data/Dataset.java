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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.opal.core.domain.metadata.Catalogue;

/**
 * 
 */
@javax.persistence.Entity
public class Dataset extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @ManyToOne(optional = false, cascade = CascadeType.ALL)
  private Entity entity;

  @ManyToOne(optional = false)
  private Catalogue catalogue;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
  private List<DataPoint> dataPoints;

  @Column(nullable = false)
  @Temporal(TemporalType.DATE)
  private Date extractionDate;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(updatable = false)
  private Date creationDate;

  public Dataset() {

  }

  public Dataset(Entity entity, Catalogue catalogue, Date extractionDate) {
    this.entity = entity;
    this.catalogue = catalogue;
    this.extractionDate = extractionDate;
    this.creationDate = new Date();
  }

  public Entity getEntity() {
    return entity;
  }

  public Catalogue getCatalogue() {
    return catalogue;
  }

  public Date getExtractionDate() {
    return extractionDate;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public List<DataPoint> getDataPoints() {
    return dataPoints != null ? dataPoints : (dataPoints = new ArrayList<DataPoint>());
  }

}
