/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.spi.r;

import org.obiba.opal.spi.resource.Resource;

import java.net.URISyntaxException;
import java.util.List;

/**
 * Bind the rows of R objects using dplyr and assign result to a symbol in R.
 */
public class ResourceAssignROperation extends AbstractROperation {

  private final String symbol;

  private final String project;

  private final Resource resource;

  private final List<String> requiredPackages;

  public ResourceAssignROperation(String symbol, String project, Resource resource, List<String> requiredPackages) {
    this.symbol = symbol;
    this.project = project;
    this.resource = resource;
    this.requiredPackages = requiredPackages;
  }

  public List<String> getRequiredPackages() {
    return requiredPackages;
  }

  @Override
  public void doWithConnection() {
    if (symbol == null) return;
    Resource.Credentials credentials = resource.getCredentials();

    try {
      ensurePackage("resourcer");
      if (requiredPackages != null && !requiredPackages.isEmpty())
        requiredPackages.forEach(this::loadPackage);
      String script = String.format("resourcer::newResourceClient(resourcer::newResource(name='%s', url='%s', identity=%s, secret=%s, format=%s))",
          resource.getName(),
          resource.toURI().toString(),
          credentials.getIdentity() == null ? "NULL" : "'" + credentials.getIdentity() + "'",
          credentials.getSecret() == null ? "NULL" : "'" + credentials.getSecret() + "'",
          resource.getFormat() == null ? "NULL" : "'" + resource.getFormat() + "'");
      eval(String.format("is.null(base::assign('%s', %s))", symbol, script), RSerialize.NATIVE);
    } catch (URISyntaxException e) {
      throw new RRuntimeException(e);
    }
  }

  @Override
  public String toString() {
    return String.format("%s <- resource[%s.%s]", symbol, project, resource.getName());
  }

}
