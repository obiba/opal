/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.resource;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Represents a reference to a resource identified by a URI and with authentication parameters. This resource
 * can be data (file, remote database etc.) or some computation facility that can be executed remotely (launch of
 * a command through ssh for instance).
 */
public interface Resource {

  /**
   * Unique identifier, to get a resource or list the resources by name.
   *
   * @return
   */
  String getName();

  /**
   * The URI can either be a URL (a link to an external resource) or a URN (a resource identifier
   * that makes sense in the domain of interest). In order to be able to separate the resource description
   * from its access, the URI should not include credentials in it, use {@link #getCredentials} instead.
   *
   * @return
   */
  URI toURI() throws URISyntaxException;

  /**
   * Access to the data resource may be protected, in which case some credentials can be defined.
   *
   * @return null if not defined
   */
  Credentials getCredentials();

  /**
   * Get an associated data format name that can help when qualifying the resource in the R server (could be the data
   * format for instance (CSV, SQL table etc.)).
   *
   * @return
   */
  String getFormat();

  /**
   * Defines a generic way of authenticating against a data resource.
   */
  interface Credentials {

    /**
     * Can be a account user name or a system identifier or a public key, may be optional depending on the
     * underlying system.
     *
     * @return
     */
    String getIdentity();

    /**
     * Can be a password or a personal access token or a private key.
     *
     * @return
     */
    String getSecret();
  }

}
