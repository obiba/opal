/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.unit;

import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.views.SelectClause;

/**
 * Represents an organization that fulfils the role of a "functional unit" of a Biobank.
 */
public class FunctionalUnit {

  public static final String OPAL_INSTANCE = "OpalInstance";

  public static final FunctionalUnit OPAL = new FunctionalUnit(OPAL_INSTANCE, OPAL_INSTANCE);

  private String name;

  private String description;

  private String keyVariableName;

  private DatasourceEncryptionStrategy datasourceEncryptionStrategy;

  private SelectClause select;

  public FunctionalUnit() {
  }

  public FunctionalUnit(String name, String keyVariableName) {
    this.name = name;
    this.keyVariableName = keyVariableName;
  }

  boolean isOpal() {
    return getName().equals(OPAL_INSTANCE);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public boolean hasDescription() {
    return description != null;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getKeyVariableName() {
    return keyVariableName;
  }

  public void setKeyVariableName(String keyVariableName) {
    this.keyVariableName = keyVariableName;
  }

  public DatasourceEncryptionStrategy getDatasourceEncryptionStrategy() {
    return datasourceEncryptionStrategy;
  }

  public void setDatasourceEncryptionStrategy(DatasourceEncryptionStrategy datasourceEncryptionStrategy) {
    this.datasourceEncryptionStrategy = datasourceEncryptionStrategy;
  }

  public SelectClause getSelect() {
    return select;
  }

  public void setSelect(SelectClause select) {
    this.select = select;
  }

}
