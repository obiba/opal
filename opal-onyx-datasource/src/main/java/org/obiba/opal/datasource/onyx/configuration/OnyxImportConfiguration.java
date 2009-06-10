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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * 
 */
@XStreamAlias("onyxImportConfiguration")
public class OnyxImportConfiguration implements Serializable {

  private static final long serialVersionUID = 1L;

  @XStreamAsAttribute
  private boolean includeAll = true;

  private List<String> filteredOutVariables;

  private List<String> filteredInVariables;

  private List<KeyVariable> keyVariables;

  @XStreamAsAttribute
  private String catalog;

  /**
   * Get if the variable is part of the import, given its path.
   * @param path
   * @return
   */
  public boolean isIncluded(String path) {
    boolean included;
    if(includeAll) {
      included = true;
      // check if it is excluded
      if(isFilteredOut(path)) {
        included = false;
        // check if it is reincluded
        if(isFilteredIn(path)) {
          included = true;
        }
      }

    } else {
      included = false;
      // check it is included
      if(isFilteredIn(path)) {
        included = true;
        // check if it is excluded
        if(isFilteredOut(path)) {
          included = false;
        }
      }
    }

    return included;
  }

  private boolean isFilteredOut(String path) {
    if(filteredOutVariables != null) {
      for(String pattern : filteredOutVariables) {
        if(getPathPattern(pattern).matcher(path).matches()) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isFilteredIn(String path) {
    if(filteredInVariables != null) {
      for(String pattern : filteredInVariables) {
        if(getPathPattern(pattern).matcher(path).matches()) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Get if the variable is configured to be a participant key variable.
   * @param path
   * @return
   */
  public boolean isKeyVariable(String path) {
    if(keyVariables != null) {
      for(KeyVariable kv : keyVariables) {
        if(getPathPattern(kv.getPath()).matcher(path).matches()) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Get the owner of the variable if it is configured to be a participant key variable.
   * @param path
   * @return null if not a participant key variable
   */
  public String getKeyVariableOwner(String path) {
    if(keyVariables != null) {
      for(KeyVariable kv : keyVariables) {
        if(getPathPattern(kv.getPath()).matcher(path).matches()) {
          return kv.getOwner();
        }
      }
    }

    return null;
  }

  private Pattern getPathPattern(String path) {
    return Pattern.compile(path);
  }

  public void addFilteredInVariables(String... paths) {
    if(paths != null) {
      if(filteredInVariables == null) {
        filteredInVariables = new ArrayList<String>();
      }
      for(String path : paths) {
        filteredInVariables.add(path);
      }
    }
  }

  public void addFilteredOutVariables(String... paths) {
    if(paths != null) {
      if(filteredOutVariables == null) {
        filteredOutVariables = new ArrayList<String>();
      }
      for(String path : paths) {
        filteredOutVariables.add(path);
      }
    }
  }

  public OnyxImportConfiguration addKeyVariable(String owner, String path) {
    if(keyVariables == null) {
      keyVariables = new ArrayList<KeyVariable>();
    }

    keyVariables.add(new KeyVariable(owner, path));

    return this;
  }

  public void setIncludeAll(boolean includeAll) {
    this.includeAll = includeAll;
  }

  public Resource getCatalogResource() {
    if(catalog == null) return null;

    DefaultResourceLoader loder = new DefaultResourceLoader();
    return loder.getResource(catalog);
  }

}
