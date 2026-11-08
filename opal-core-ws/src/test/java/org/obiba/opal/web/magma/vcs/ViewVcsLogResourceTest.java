/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma.vcs;

import java.lang.reflect.Field;
import java.util.List;

import com.thoughtworks.xstream.converters.ConversionException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.obiba.git.TagInfo;
import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTableStatus;
import org.obiba.magma.ValueView;
import org.obiba.magma.security.MagmaSecurityExtension;
import org.obiba.magma.views.ViewAwareDatasource;
import org.obiba.magma.views.ViewManager;
import org.obiba.magma.views.support.VariableOperationContext;
import org.obiba.opal.core.cfg.OpalViewPersistenceStrategy;
import org.obiba.opal.core.vcs.OpalGitUtils;
import org.obiba.opal.core.vcs.OpalVersionControlSystem;
import org.obiba.opal.web.magma.view.ValueViewDtoExtension;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ViewVcsLogResourceTest {

  private static final String COMMIT_ID = "0123456789";

  private final ViewManager viewManager = mock(ViewManager.class);

  private final OpalViewPersistenceStrategy strategy = mock(OpalViewPersistenceStrategy.class);

  private final OpalVersionControlSystem vcs = mock(OpalVersionControlSystem.class);

  private final ValueViewDtoExtension viewDtoExtension = mock(ValueViewDtoExtension.class);

  private final ValueView restored = mock(ValueView.class,
      Mockito.withSettings().extraInterfaces(Initialisable.class, Disposable.class));

  private final ValueView current = mock(ValueView.class);

  private ViewVcsLogResource resource;

  @Before
  public void setUp() throws Exception {
    new MagmaEngine();
    ViewAwareDatasource datasource = mock(ViewAwareDatasource.class);
    when(datasource.getName()).thenReturn("ds");
    when(current.getDatasource()).thenReturn(datasource);
    when(current.getName()).thenReturn("v");
    when(current.getStatus()).thenReturn(ValueTableStatus.READY);
    when(viewManager.getView("ds", "v")).thenReturn(current);
    when(strategy.readView("ds", "v", COMMIT_ID)).thenReturn(restored);
    when(restored.getStatus()).thenReturn(ValueTableStatus.READY);
    when(viewDtoExtension.isDtoOf(any(ValueView.class))).thenReturn(true);
    when(viewDtoExtension.asDto(restored)).thenReturn(Magma.ViewDto.newBuilder().addFrom("ds.T").buildPartial());
    when(viewDtoExtension.asDto(current)).thenReturn(Magma.ViewDto.newBuilder().addFrom("ds.T").buildPartial());
    ViewDtos viewDtos = new ViewDtos();
    viewDtos.setExtensions(List.of(viewDtoExtension));

    resource = new ViewVcsLogResource();
    setField("viewManager", viewManager);
    setField("viewPersistenceStrategy", strategy);
    setField("vcs", vcs);
    setField("viewDtos", viewDtos);
    setField("datasource", "ds");
    setField("view", "v");
  }

  @After
  public void tearDown() {
    ThreadContext.unbindSubject();
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_restore_saves_the_view() {
    Response response = resource.restore(COMMIT_ID);

    assertThat(response.getStatus()).isEqualTo(200);
    verify(viewManager).addView(eq("ds"), eq(restored), eq("Restore view from 0123456"),
        (VariableOperationContext) isNull());
    verify((Disposable) restored, never()).dispose();
  }

  @Test
  public void test_restore_rejects_view_in_error() {
    when(restored.getStatus()).thenReturn(ValueTableStatus.ERROR);

    assertRejected(resource.restore(COMMIT_ID), 400);
  }

  @Test
  public void test_restore_rejects_view_that_fails_to_initialise() {
    Mockito.doThrow(new IllegalStateException("no source table")).when((Initialisable) restored).initialise();

    assertRejected(resource.restore(COMMIT_ID), 400);
  }

  @Test
  public void test_restore_rejects_unreadable_definition() {
    when(strategy.readView("ds", "v", COMMIT_ID)).thenThrow(new ConversionException("no such class"));

    assertThat(resource.restore(COMMIT_ID).getStatus()).isEqualTo(400);
    verify(viewManager, never()).addView(anyString(), any(ValueView.class), anyString(),
        any(VariableOperationContext.class));
  }

  @Test
  public void test_restore_forbidden_disposes_the_view() {
    // can see the values of the view, but cannot edit the resource of the restored version
    Subject subject = mock(Subject.class);
    when(subject.isPermitted(anyString())).thenAnswer(
        call -> ((String) call.getArguments()[0]).endsWith("/valueSet:GET"));
    ThreadContext.bind(subject);
    MagmaEngine.get().extend(new MagmaSecurityExtension() {
      @Override
      public void initialise() {
      }
    });
    when(viewDtoExtension.asDto(restored)).thenReturn(Magma.ViewDto.newBuilder().addFrom("ds.res")
        .setExtension(Magma.ResourceViewDto.view, Magma.ResourceViewDto.getDefaultInstance()).buildPartial());

    assertRejected(resource.restore(COMMIT_ID), 403);
  }

  @Test
  public void test_restore_of_another_script_forbidden_without_source_values() {
    // cannot see any values, neither of the view nor of the resource behind it
    Subject subject = mock(Subject.class);
    ThreadContext.bind(subject);
    MagmaEngine.get().extend(new MagmaSecurityExtension() {
      @Override
      public void initialise() {
      }
    });
    when(viewDtoExtension.asDto(current)).thenReturn(resourceView("1"));
    when(viewDtoExtension.asDto(restored)).thenReturn(resourceView("2"));

    try {
      resource.restore(COMMIT_ID);
      org.junit.Assert.fail("expected forbidden");
    } catch(ForbiddenException e) {
      verify(viewManager, never()).addView(anyString(), any(ValueView.class), anyString(),
          any(VariableOperationContext.class));
      verify((Disposable) restored).dispose();
    }
  }

  private static Magma.ViewDto resourceView(String script) {
    return Magma.ViewDto.newBuilder().addFrom("ds.res").setExtension(Magma.ResourceViewDto.view,
        Magma.ResourceViewDto.newBuilder().addVariables(Magma.VariableDto.newBuilder().setName("x")
            .setEntityType("Participant").setValueType("integer").setIsRepeatable(false)
            .addAttributes(Magma.AttributeDto.newBuilder().setName("script").setValue(script))).build()).buildPartial();
  }

  @Test
  public void test_download_view_definition_at_tag() {
    when(vcs.getTags("ds", "v")).thenReturn(List.of(new TagInfo.Builder().name("t1").commitId(COMMIT_ID).build()));
    when(vcs.getBlob("ds", OpalGitUtils.getViewFilePath("v"), COMMIT_ID)).thenReturn("<view/>");

    Response response = resource.downloadViewDefinition("t1");

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getEntity()).isEqualTo("<view/>");
    assertThat(response.getHeaderString("Content-Disposition")).contains("filename=\"v-t1.xml\"");
  }

  @Test
  public void test_download_view_definition_of_unknown_tag_not_found() {
    when(vcs.getTags("ds", "v")).thenReturn(List.of());

    assertThat(resource.downloadViewDefinition("t1").getStatus()).isEqualTo(404);
  }

  @Test
  public void test_create_tag_of_removed_view_not_found() throws Exception {
    when(viewManager.getView("ds", "gone")).thenThrow(new org.obiba.magma.NoSuchValueTableException("gone"));
    setField("view", "gone");

    try {
      resource.createTag("t", null);
      org.junit.Assert.fail("expected no such table");
    } catch(org.obiba.magma.NoSuchValueTableException e) {
      verify(vcs, never()).createTag(anyString(), anyString(), anyString(), anyString());
    }
  }

  private void setField(String name, Object value) throws Exception {
    Field field = ViewVcsLogResource.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(resource, value);
  }

  private void assertRejected(Response response, int status) {
    assertThat(response.getStatus()).isEqualTo(status);
    verify(viewManager, never()).addView(anyString(), any(ValueView.class), anyString(),
        any(VariableOperationContext.class));
    verify((Disposable) restored).dispose();
  }
}
