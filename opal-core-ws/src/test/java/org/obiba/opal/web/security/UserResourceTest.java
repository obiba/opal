/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.security;

import java.net.URISyntaxException;
import java.util.List;

import org.apache.shiro.realm.SimpleAccountRealm;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.service.PersistenceManagerAware;
import org.obiba.opal.core.service.impl.UserService;
import org.obiba.opal.core.service.impl.UserServiceHibernateImpl;
import org.obiba.opal.web.model.Opal;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;

public class UserResourceTest {

  private UserService mockUserService;

  private SimpleAccountRealm mockRealm;

  private UserResource userResource;

  String testSessionId = "test-session-id";

  @Before
  public void setUp() throws URISyntaxException {
    mockUserService = new UserServiceHibernateImpl();
    ((PersistenceManagerAware) mockUserService).setPersistenceManager(createMock(PersistenceManager.class));
    ((UserServiceHibernateImpl) mockUserService).setSessionFactory(createMock(SessionFactory.class));
  }

  @Test
  public void testUsersGET() {
    UserService userServiceMock = createMock(UserService.class);
    UserResource resource = new UserResource(userServiceMock);

    List<Opal.UserDto> dtos = resource.getUsers();
    assertEquals(0, dtos.size());

    resource.createUser(Opal.UserDto.newBuilder().setName("pwel").build());
    dtos = resource.getUsers();
    assertEquals(21, dtos.size());
  }

}
