/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.upgrade.binary;

/**
 *
 */
class BinaryToMove {

  final String datasourceName;

  final String tableName;

  final String variableName;

  final String entityId;

  BinaryToMove(String datasourceName, String tableName, String variableName, String entityId) {
    this.datasourceName = datasourceName;
    this.tableName = tableName;
    this.variableName = variableName;
    this.entityId = entityId;
  }

}
