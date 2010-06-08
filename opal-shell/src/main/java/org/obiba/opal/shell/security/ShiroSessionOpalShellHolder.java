/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.security;

import org.apache.shiro.SecurityUtils;
import org.obiba.opal.shell.OpalShell;
import org.obiba.opal.shell.OpalShellHolder;
import org.springframework.stereotype.Component;

/**
 * Implements {@code OpalShellHolder} by storing the {@code OpalShell} in the current Shiro {@code Session}.
 */
@Component
public class ShiroSessionOpalShellHolder implements OpalShellHolder {

  public OpalShell getCurrentShell() {
    return (OpalShell) SecurityUtils.getSubject().getSession().getAttribute(OpalShell.class);
  }

  public void bind(OpalShell shell) {
    SecurityUtils.getSubject().getSession().setAttribute(OpalShell.class, shell);
  }

}
