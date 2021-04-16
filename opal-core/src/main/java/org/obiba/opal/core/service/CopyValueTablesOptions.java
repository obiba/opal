/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;

import java.util.Set;

public class CopyValueTablesOptions {

  private final Set<ValueTable> sourceTables;
  private final Datasource destination;
  private final String idMapping;
  private final boolean allowIdentifierGeneration;
  private final boolean ignoreUnknownIdentifier;

  CopyValueTablesOptions(Set<ValueTable> sourceTables, Datasource destination, String idMapping, boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier) {
    this.sourceTables = Sets.filter(sourceTables, input -> input != null && !Strings.isNullOrEmpty(input.getName()));
    this.destination = destination;
    this.idMapping = idMapping;
    this.allowIdentifierGeneration = allowIdentifierGeneration;
    this.ignoreUnknownIdentifier = ignoreUnknownIdentifier;
  }

  public Set<ValueTable> getSourceTables() {
    return sourceTables;
  }

  public Datasource getDestination() {
    return destination;
  }

  public String getIdMapping() {
    return idMapping;
  }

  public boolean isAllowIdentifierGeneration() {
    return allowIdentifierGeneration;
  }

  public boolean isIgnoreUnknownIdentifier() {
    return ignoreUnknownIdentifier;
  }
}
