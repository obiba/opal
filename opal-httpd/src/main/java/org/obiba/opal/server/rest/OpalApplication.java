/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.rest;

import java.util.Set;

import javax.ws.rs.core.Application;

import com.google.common.collect.Sets;

/**
 *
 */
public class OpalApplication extends Application {

  private Set<Class<?>> classes = Sets.newHashSet();

  @Override
  public Set<Class<?>> getClasses() {
    return classes;
  }

}
