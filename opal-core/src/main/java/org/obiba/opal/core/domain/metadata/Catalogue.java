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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.obiba.core.domain.AbstractEntity;

/**
 *
 */
@javax.persistence.Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "datasource", "name" }))
public class Catalogue extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  private String datasource;

  @Column(nullable = false)
  private String name;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false, updatable = false)
  private Date creationDate;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "catalogue")
  private List<Variable> variables;

  public Catalogue() {

  }

  public Catalogue(String datasource, String name) {
    this.datasource = datasource;
    this.name = name;
    this.creationDate = new Date();
  }

  public String getName() {
    return name;
  }

  public String getDatasource() {
    return datasource;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public List<Variable> getVariables() {
    return variables != null ? variables : (variables = new ArrayList<Variable>());
  }

  public Variable addVariable(String name) {
    if(name == null) throw new IllegalArgumentException("name cannot be null");
    Variable v = new Variable(this, name);
    getVariables().add(v);
    return v;
  }
}
