/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.security;

/**
 * Converts a permission from one domain into a set of permissions in magma domain.
 */
public interface SubjectPermissionConverter {

  public boolean canConvert(String domain, String permission);

  public Iterable<String> convert(String domain, String node, String permission);
}
