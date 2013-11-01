/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.impl;

import org.easymock.EasyMock;
import org.obiba.opal.core.service.SubjectAclService;
import org.obiba.opal.core.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserServiceTestConfig extends AbstractOrientDbTestConfig {

  @Bean
  public UserService userService() {
    return new UserServiceImpl();
  }

  @Bean
  public SubjectAclService subjectAclService() {
    return EasyMock.createMock(SubjectAclService.class);
  }

}
