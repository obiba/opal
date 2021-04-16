/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.identifiers;

/**
 * Provides a method to generate an identifier. Implementations should produce a random id of
 * their choosing. Clients should always check to ensure that the id produced is unique!
 */
public interface IdentifierGenerator {

  /**
   * Returns a new identifier. Check that it is unique before use!
   *
   * @return An identifier.
   */
  String generateIdentifier();
}
