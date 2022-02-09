/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.resource;

import org.obiba.magma.*;

import java.util.List;

/**
 * Establishes the connection with a resource and performs basic operations on its tabular representation.
 */
public interface TabularResourceConnector extends Initialisable, Disposable {

  String getSymbol();

  List<Column> getColumns();

  boolean hasColumn(String name);

  Column getColumn(String name);

  interface Column {

    String getName();

    int getPosition();

    List<Value> asVector(ValueType valueType);

    Variable asVariable(String entityType);
  }
}
