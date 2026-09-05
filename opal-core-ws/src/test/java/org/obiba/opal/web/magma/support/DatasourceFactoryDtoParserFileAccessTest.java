/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma.support;

import jakarta.validation.constraints.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.security.Authorizer;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.fs.impl.DefaultOpalFileSystem;
import org.obiba.opal.fs.security.SecuredOpalFileSystem;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A datasource factory parser hands a native file to Magma: an existing file must be readable by the current
 * subject, whatever the table or datasource permission that allowed the request.
 */
public class DatasourceFactoryDtoParserFileAccessTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private AbstractDatasourceFactoryDtoParser parser;

  @Before
  public void setUp() throws IOException {
    Path root = tmp.getRoot().toPath();
    Files.createDirectories(root.resolve("home/alice"));
    Files.createDirectories(root.resolve("home/victim"));
    Files.writeString(root.resolve("home/alice/data.csv"), "id\n1\n");
    Files.writeString(root.resolve("home/victim/participants.csv"), "id\n2\n");

    OpalFileSystemService fileSystemService = mock(OpalFileSystemService.class);
    when(fileSystemService.getFileSystem()).thenReturn(
        new SecuredOpalFileSystem(new DefaultOpalFileSystem(tmp.getRoot().getAbsolutePath()), new PrefixAuthorizer("rest:/files/home/alice")));

    parser = new AbstractDatasourceFactoryDtoParser() {
      @NotNull
      @Override
      protected DatasourceFactory internalParse(DatasourceFactoryDto dto, DatasourceEncryptionStrategy encryptionStrategy) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean canParse(DatasourceFactoryDto dto) {
        return false;
      }
    };
    Field field = ReflectionUtils.findField(AbstractDatasourceFactoryDtoParser.class, "opalFileSystemService");
    ReflectionUtils.makeAccessible(field);
    ReflectionUtils.setField(field, parser, fileSystemService);
  }

  @Test
  public void ownFileIsResolved() throws IOException {
    File file = parser.resolveLocalFile("/home/alice/data.csv");

    assertThat(Files.readString(file.toPath())).isEqualTo("id\n1\n");
  }

  @Test(expected = IllegalArgumentException.class)
  public void anotherUsersFileIsRefused() {
    parser.resolveLocalFile("/home/victim/participants.csv");
  }

  @Test
  public void fileThatDoesNotExistYetIsResolved() {
    assertThat(parser.resolveLocalFile("/home/alice/later.csv")).doesNotExist();
  }

  private static class PrefixAuthorizer implements Authorizer {

    private final String prefix;

    private PrefixAuthorizer(String prefix) {
      this.prefix = prefix;
    }

    @Override
    public boolean isPermitted(String permission) {
      return permission.startsWith(prefix);
    }

    @Override
    public <V> V silentSudo(Callable<V> callable) {
      try {
        return callable.call();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public <V> V sudo(Callable<V> callable) throws Exception {
      return callable.call();
    }
  }
}
