/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service.impl;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.obiba.opal.core.service.OnyxImportService;

/**
 * Default <code>OnyxImportService</code> implementation.
 */
public class DefaultOnyxImportServiceImpl implements OnyxImportService {

  public void importData(String username, String password) {
    // TODO Auto-generated method stub
    System.out.println("<importData(user: " + username + ", password: " + password + ")>");
  }

  public void importData(String username, String password, Date date, String site, List<String> tags) {
    // TODO Auto-generated method stub
    System.out.println("<importData(user: " + username + ", password: " + password + ", date: " + date.toString() + ", site: " + site + ", tags: " + tags + ")>");
  }

  public void importData(String username, String password, List<String> tags, File source) {
    // TODO Auto-generated method stub
    System.out.println("<importData(user: " + username + ", password: " + password + ", tags: " + tags + ", file: " + source.getPath() + ")>");
  }

}
