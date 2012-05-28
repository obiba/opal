/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.rest.client.magma;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionManagerFactory;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

public class OpalClientConnectionManagerFactory implements ClientConnectionManagerFactory {

  @Override
  public ClientConnectionManager newInstance(HttpParams params, SchemeRegistry schemeRegistry) {
    ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(schemeRegistry);
    cm.setDefaultMaxPerRoute(20);
    cm.setMaxTotal(20);
    return cm;
  }

}
