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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.obiba.core.domain.AbstractEntity;

/**
 * 
 */
@javax.persistence.Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "type", "identifier" }))
public class Entity extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  private String type;

  private String identifier;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "entity")
  private List<Dataset> datasets;

  public Entity() {

  }

  public Entity(String type, String identifier) {
    this.identifier = identifier;
    this.type = type;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getType() {
    return type;
  }

  public List<Dataset> getDatasets() {
    return datasets != null ? datasets : (datasets = new ArrayList<Dataset>());
  }
}
