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

import jakarta.annotation.Nullable;

/**
 * Thrown when a system identifier cannot be mapped to a private identifier.
 */
public class NoSuchSystemIdentifierMappingException extends NoSuchIdentifierMappingException {

  private static final long serialVersionUID = 1L;

  public NoSuchSystemIdentifierMappingException(@Nullable String idMapping, String identifier, String entityType) {
    super("No private ID found in identifiers mapping '" + idMapping + "' for entity '" +
        identifier + "' of type '" + entityType + "'", idMapping, entityType);
  }

}
