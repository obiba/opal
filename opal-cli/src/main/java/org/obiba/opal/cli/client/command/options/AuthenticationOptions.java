/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client.command.options;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * This interface declares authentication options -- <code>username</code> and <code>password</code> -- for commands
 * that use them requiring authentication.
 */
public interface AuthenticationOptions {

  @Option
  public String getUsername();

  public boolean isUsername();

  @Option
  public String getPassword();

  public boolean isPassword();
}
