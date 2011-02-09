/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.rest.client.authorization;

/**
 *
 */
public class CompositeAuthorizer implements HasAuthorization {

  private HasAuthorization[] authorizers;

  public CompositeAuthorizer(HasAuthorization... authorizers) {
    super();
    this.authorizers = authorizers;
  }

  @Override
  public void beforeAuthorization() {
    for(HasAuthorization auth : authorizers) {
      auth.beforeAuthorization();
    }
  }

  @Override
  public void authorized() {
    for(HasAuthorization auth : authorizers) {
      auth.authorized();
    }
  }

  @Override
  public void unauthorized() {
    for(HasAuthorization auth : authorizers) {
      auth.unauthorized();
    }
  }

}
