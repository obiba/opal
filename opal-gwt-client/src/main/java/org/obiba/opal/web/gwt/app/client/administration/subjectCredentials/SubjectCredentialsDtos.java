/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.subjectCredentials;

import org.obiba.opal.web.model.client.opal.SubjectCredentialsDto;
import org.obiba.opal.web.model.client.opal.SubjectCredentialsType;

public class SubjectCredentialsDtos {

  public static boolean isUser(SubjectCredentialsType type) {
    return type.getName().equals(SubjectCredentialsType.USER.getName());
  }

  public static boolean isApplication(SubjectCredentialsType type) {
    return type.getName().equals(SubjectCredentialsType.APPLICATION.getName());
  }

  public static boolean isUser(SubjectCredentialsDto dto) {
    return isUser(dto.getType());
  }

  public static boolean isApplication(SubjectCredentialsDto dto) {
    return isApplication(dto.getType());
  }

}
