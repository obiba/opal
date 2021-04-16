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

/**
 * Thrown when a method argument refers to a non-existing identifiers mapping.
 */
public abstract class NoSuchIdentifierMappingException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final String idMapping;

  private final String entityType;

  public NoSuchIdentifierMappingException(String message, String idMapping, String entityType) {
    super(message);
    this.idMapping = idMapping;
    this.entityType = entityType;
  }

  public String getIdentifiersMapping() {
    return idMapping;
  }

  public String getEntityType() {
    return entityType;
  }
}
