/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.map;

import org.openrdf.model.URI;

/**
 *
 */
public interface ResourceFactory {

  public URI findResource(String property, String value);

  public URI findResource(URI type, String identifier);

  public URI getResource(URI type, String identifier);

}
