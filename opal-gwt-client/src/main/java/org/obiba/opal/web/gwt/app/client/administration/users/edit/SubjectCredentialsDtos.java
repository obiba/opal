/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.users.edit;

import org.obiba.opal.web.model.client.opal.SubjectCredentialsDto;

public class SubjectCredentialsDtos {

  private SubjectCredentialsDtos() {}

  public static boolean isPassword(SubjectCredentialsDto.AuthenticationType type) {
    return type.getName().equals(SubjectCredentialsDto.AuthenticationType.PASSWORD.getName());
  }

  public static boolean isCertificate(SubjectCredentialsDto.AuthenticationType type) {
    return type.getName().equals(SubjectCredentialsDto.AuthenticationType.CERTIFICATE.getName());
  }

}
