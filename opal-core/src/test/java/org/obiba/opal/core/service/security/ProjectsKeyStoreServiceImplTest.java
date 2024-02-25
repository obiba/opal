/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service.security;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.security.KeyStoreState;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.OrientDbService;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit tests for {@link KeyStoreServiceImpl}.
 */
public class ProjectsKeyStoreServiceImplTest {

  private ProjectsKeyStoreServiceImpl projectsKeyStoreService;

  private OrientDbService mockOrientDbService;

  @Before
  public void setUp() {
    mockOrientDbService = createMock(OrientDbService.class);
    projectsKeyStoreService = new ProjectsKeyStoreServiceImpl();
    projectsKeyStoreService.setOrientDbService(mockOrientDbService);
    projectsKeyStoreService.setCallbackHandler(createPasswordCallbackHandler());
  }

  @SuppressWarnings("ConstantConditions")
  @Test(expected = IllegalArgumentException.class)
  public void testGetUnitKeyStoreThrowsExceptionOnNullUnitName() {
    projectsKeyStoreService.getKeyStore(null);
  }

  @Test
  public void testGetUnitKeyStore() throws IOException {
    KeyStoreState expectedKeyStoreStateTemplate = new KeyStoreState();
    expectedKeyStoreStateTemplate.setName("my-unit");
    KeyStoreState state = new KeyStoreState();
    state.setName("projects:my-unit");
    state.setKeyStore(getTestKeyStoreByteArray());
    expect(mockOrientDbService.findUnique(state)).andReturn(state).once();

    Project project = new Project("my-unit");

    replay(mockOrientDbService);

    OpalKeyStore opalKeyStore = projectsKeyStoreService.getKeyStore(project);
    verify(mockOrientDbService);

    assertThat(opalKeyStore).isNotNull();
    assertThat(opalKeyStore.getName()).isEqualTo(state.getName());
  }

  @Test
  public void testGetOrCreateUnitKeyStoreCreatesTheKeyStoreIfItDoesNotExist() throws Exception {

    KeyStoreState state = new KeyStoreState();
    state.setName("projects:my-unit");
    expect(mockOrientDbService.findUnique(state)).andReturn(null).times(2);

    mockOrientDbService.save(state, state);
    EasyMock.expectLastCall().once();

    Project project = new Project("my-unit");

    replay(mockOrientDbService);

    OpalKeyStore opalKeyStore = projectsKeyStoreService.getKeyStore(project);

    verify(mockOrientDbService);

    assertThat(opalKeyStore).isNotNull();
  }

  //
  // Helper Methods
  //

  static CallbackHandler createPasswordCallbackHandler() {
    return new CallbackHandler() {
      @Override
      public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for(Callback callback : callbacks) {
          if(callback instanceof PasswordCallback) {
            ((PasswordCallback) callback).setPassword("password".toCharArray());
          }
        }
      }
    };
  }

  private byte[] getTestKeyStoreByteArray() throws IOException {

    try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream testKeyStoreStream = new FileInputStream(
            FileUtil.getFileFromResource("DefaultUnitKeyStoreServiceImplTest/opal.jks"))
    ) {

      while(testKeyStoreStream.available() != 0) {
        byte[] buf = new byte[1024];
        int bytesRead = testKeyStoreStream.read(buf);
        baos.write(buf, 0, bytesRead);
      }

      return baos.toByteArray();
    }

  }

}
