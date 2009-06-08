/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datasource.onyx.configuration;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * 
 */
@XStreamAlias("keyVariable")
public class KeyVariable implements Serializable {

  private static final long serialVersionUID = 1L;

  @XStreamAsAttribute
  private String owner;

  @XStreamAsAttribute
  private String path;

  public KeyVariable() {
    super();
  }

  public KeyVariable(String owner, String path) {
    super();
    this.owner = owner;
    this.path = path;
  }

  public String getOwner() {
    return owner;
  }

  public String getPath() {
    return path;
  }

}
