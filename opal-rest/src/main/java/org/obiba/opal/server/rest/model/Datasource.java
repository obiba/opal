/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.rest.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.obiba.magma.ValueTable;

@XmlRootElement
public class Datasource {

  @XmlElement
  private final String name;

  @XmlElement
  private final String link;

  @XmlElement
  private final List<String> tables = new ArrayList<String>();

  public Datasource() {
    this.name = null;
    this.link = null;
  }

  public Datasource(URI link, org.obiba.magma.Datasource magmaDs) {
    this.name = magmaDs.getName();
    this.link = link.toString();
    for(ValueTable table : magmaDs.getValueTables()) {
      tables.add(table.getName());
    }
  }

  public String getName() {
    return name;
  }

  public String getLink() {
    return link;
  }

  public List<String> getTables() {
    return tables;
  }

}
