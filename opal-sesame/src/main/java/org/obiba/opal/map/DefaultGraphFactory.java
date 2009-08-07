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

import org.openrdf.model.Graph;
import org.openrdf.model.impl.GraphImpl;

/**
 *
 */
public class DefaultGraphFactory implements GraphFactory {

  public DefaultGraphFactory() {

  }

  public Graph newGraph() {
    return new GraphImpl();
  }

}
