/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import jakarta.ws.rs.ForbiddenException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTableStatus;
import org.obiba.magma.ValueView;
import org.obiba.magma.Variable;
import org.obiba.magma.security.MagmaSecurityExtension;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.web.magma.view.ValueViewDtoExtension;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma.AttributeDto;
import org.obiba.opal.web.model.Magma.FileViewDto;
import org.obiba.opal.web.model.Magma.JavaScriptViewDto;
import org.obiba.opal.web.model.Magma.ResourceViewDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Magma.VariableListViewDto;
import org.obiba.opal.web.model.Magma.ViewDto;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.obiba.opal.web.magma.ViewResourceImpl.ensureDerivationPermitted;
import static org.obiba.opal.web.magma.ViewResourceImpl.referencesOf;
import static org.obiba.opal.web.magma.ViewResourceImpl.sameDerivation;

public class ViewDerivationTest {

  @Before
  public void setUp() {
    new MagmaEngine();
  }

  @After
  public void tearDown() {
    ThreadContext.unbindSubject();
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_metadata_edit_keeps_derivation() {
    VariableDto x = variable("x", attr("script", "$('A')"));
    VariableDto relabelled = x.toBuilder().addAttributes(attr("label", "X")).build();

    assertThat(sameDerivation(list(x), list(relabelled))).isTrue();
    assertThat(sameDerivation(var(x), var(relabelled), false)).isTrue();
  }

  @Test
  public void test_rename_keeps_derivation_of_table_view_only() {
    VariableDto x = variable("x", attr("script", "$('A')"));
    VariableDto y = x.toBuilder().setName("y").build();

    assertThat(sameDerivation(var(x), var(y), false)).isTrue();
    assertThat(sameDerivation(var(x), var(y), true)).isFalse();
  }

  @Test
  public void test_value_sources_change_derivation() {
    VariableDto x = variable("x", attr("script", "$('A')"));

    assertThat(sameDerivation(var(x), var(variable("x", attr("script", "$('B')"))), false)).isFalse();
    assertThat(sameDerivation(var(variable("x")), var(variable("x", attr("sameAs", "B"))), false)).isFalse();
    assertThat(sameDerivation(var(x), var(x.toBuilder().addAttributes(attr("script", "$('B')").toBuilder()
        .setLocale("en")).build()), false)).isFalse();
    assertThat(sameDerivation(var(variable("x")), var(variable("x", attr("script", "1").toBuilder()
        .setNamespace("opal").setName("column").build())), true)).isFalse();
  }

  @Test
  public void test_new_variable_derives_only_with_script_or_in_resource_view() {
    assertThat(sameDerivation(null, var(variable("x")), false)).isTrue();
    assertThat(sameDerivation(null, var(variable("x", attr("script", "1"))), false)).isFalse();
    assertThat(sameDerivation(null, var(variable("x")), true)).isFalse();
  }

  @Test
  public void test_view_settings_change_derivation() {
    ViewDto view = list(variable("x", attr("script", "$('A')")));

    assertThat(sameDerivation(view, view.toBuilder().setWhere("true").build())).isFalse();
    assertThat(sameDerivation(view, view.toBuilder().clearFrom().addFrom("ds.U").build())).isFalse();
    assertThat(sameDerivation(view, view.toBuilder().addInnerFrom("ds.T").build())).isFalse();
    assertThat(sameDerivation(view, view.toBuilder().clearExtension(VariableListViewDto.view)
        .setExtension(JavaScriptViewDto.view, JavaScriptViewDto.getDefaultInstance()).build())).isFalse();
    assertThat(sameDerivation(view, view.toBuilder().setExtension(FileViewDto.view,
        FileViewDto.newBuilder().setFilename("/tmp/v.xml").build()).build())).isFalse();

    ViewDto js = base().setExtension(JavaScriptViewDto.view, JavaScriptViewDto.getDefaultInstance()).build();
    assertThat(sameDerivation(js, js.toBuilder().setExtension(JavaScriptViewDto.view,
        JavaScriptViewDto.newBuilder().setSelect("true").build()).build())).isFalse();

    ViewDto res = resource(true);
    assertThat(sameDerivation(res, resource(false))).isFalse();
    assertThat(sameDerivation(res, res.toBuilder().setExtension(ResourceViewDto.view,
        res.getExtension(ResourceViewDto.view).toBuilder().setIdColumn("other").build()).build())).isFalse();
  }

  @Test
  public void test_new_view_derives_only_with_scripts_or_filters() {
    assertThat(sameDerivation(null, list(variable("x")))).isTrue();
    assertThat(sameDerivation(null, base().setExtension(JavaScriptViewDto.view,
        JavaScriptViewDto.getDefaultInstance()).build())).isTrue();
    assertThat(sameDerivation(null, resource(true))).isTrue();

    assertThat(sameDerivation(null, list(variable("x", attr("script", "1"))))).isFalse();
    assertThat(sameDerivation(null, list(variable("x")).toBuilder().setWhere("true").build())).isFalse();
    assertThat(sameDerivation(null, resource(true).toBuilder().setExtension(ResourceViewDto.view,
        ResourceViewDto.newBuilder().addVariables(variable("x")).build()).build())).isFalse();
  }

  @Test
  public void test_derivation_change_needs_source_values() {
    Subject subject = mock(Subject.class);
    ThreadContext.bind(subject);
    MagmaEngine.get().extend(new MagmaSecurityExtension() {
      @Override
      public void initialise() {
      }
    });
    ViewDto view = resource(true);

    ensureDerivationPermitted(view, () -> false, java.util.List::of);
    try {
      ensureDerivationPermitted(view, () -> true, java.util.List::of);
      org.junit.Assert.fail("expected forbidden");
    } catch(ForbiddenException e) {
      assertThat(e.getResponse().getStatus()).isEqualTo(403);
    }

    when(subject.isPermitted("rest:/project/ds/resource/res:PUT")).thenReturn(true);
    ensureDerivationPermitted(view, () -> true, java.util.List::of);
  }

  @Test
  public void test_metadata_edit_permitted_when_source_is_not_visible() {
    // the datasource of the source is not even registered, as for a user without any permission on it
    Subject subject = mock(Subject.class);
    ThreadContext.bind(subject);
    MagmaEngine.get().extend(new MagmaSecurityExtension() {
      @Override
      public void initialise() {
      }
    });
    ViewDto view = list(variable("x", attr("script", "$('A')")));

    ensureDerivationPermitted(view, () -> false, java.util.List::of);
    try {
      ensureDerivationPermitted(view, () -> true, java.util.List::of);
      org.junit.Assert.fail("expected forbidden");
    } catch(ForbiddenException e) {
      assertThat(e.getResponse().getStatus()).isEqualTo(403);
    }

    when(subject.isPermitted("rest:/datasource/ds/table/T/valueSet:GET")).thenReturn(true);
    ensureDerivationPermitted(view, () -> true, java.util.List::of);
  }

  @Test
  public void test_added_script_needs_values_of_the_tables_it_reads() {
    Subject subject = secured();
    when(subject.isPermitted("rest:/datasource/ds/table/T/valueSet:GET")).thenReturn(true);
    ViewDto current = list(variable("x", attr("script", "$('A')")));

    assertForbidden(current, list(variable("x", attr("script", "$join('p2.B:secret', 'id')"))));
    assertForbidden(current, list(variable("x", attr("sameAs", "p2.B:secret"))));
    assertForbidden(current, current.toBuilder().setWhere("$('p2.B:secret').eq(1)").build());
    // without datasource, a table of the datasource of the sources
    assertForbidden(current, list(variable("x", attr("script", "$('B:secret')"))));

    when(subject.isPermitted("rest:/datasource/p2/table/B/valueSet:GET")).thenReturn(true);
    check(current, list(variable("x", attr("script", "$join('p2.B:secret', 'id')"))));
    when(subject.isPermitted("rest:/datasource/ds/table/B/valueSet:GET")).thenReturn(true);
    check(current, list(variable("x", attr("script", "$('B:secret')"))));
  }

  @Test
  public void test_dynamic_script_needs_administrator() {
    Subject subject = secured();
    when(subject.isPermitted("rest:/datasource/ds/table/T/valueSet:GET")).thenReturn(true);
    ViewDto current = list(variable("x", attr("script", "$('A')")));
    ViewDto dynamic = list(variable("x", attr("script", "$('p2.' + 'B:secret')")));

    assertForbidden(current, dynamic);
    when(subject.isPermitted("rest:/:POST")).thenReturn(true);
    check(current, dynamic);
  }

  @Test
  public void test_any_change_needs_values_of_the_tables_the_view_reads() {
    Subject subject = secured();
    when(subject.isPermitted("rest:/datasource/ds/table/T/valueSet:GET")).thenReturn(true);
    VariableDto x = variable("x", attr("script", "$join('p2.B:secret', 'id')"));
    ViewDto current = list(x);

    // metadata only
    check(current, list(x.toBuilder().addAttributes(attr("label", "X")).build()));
    // works on what x reads
    assertForbidden(current, list(x, variable("z", attr("script", "$('id').eq('1') ? $this('x') : null"))));
    assertForbidden(current, current.toBuilder().setWhere("$('id').eq('1')").build());
    assertForbidden(current, list(x, x.toBuilder().setName("y").build()));

    when(subject.isPermitted("rest:/datasource/p2/table/B/valueSet:GET")).thenReturn(true);
    check(current, current.toBuilder().setWhere("$('id').eq('1')").build());
  }

  @Test
  public void test_references_of_saved_variables() {
    ViewDto view = list(variable("x", attr("script", "$('ds.A:a')")), variable("y", attr("sameAs", "ds.B:b")));

    assertThat(variablesOf(referencesOf(view))).containsOnly("ds.A:a", "ds.B:b");
    // x replaced, y renamed to w
    assertThat(variablesOf(referencesOf(view, java.util.List.of(var(variable("x", attr("script", "$('ds.C:c')"))),
        var(variable("w"))), java.util.Set.of("y"))))
        .containsOnly("ds.C:c");
    assertThat(variablesOf(referencesOf(view.toBuilder().setWhere("$('ds.D:d')").build()))).contains("ds.D:d");
  }

  private static java.util.Set<String> variablesOf(java.util.List<org.obiba.magma.js.validation.ScriptReferences> refs) {
    return refs.stream().flatMap(r -> r.variables().stream()).collect(java.util.stream.Collectors.toSet());
  }

  private static Subject secured() {
    Subject subject = mock(Subject.class);
    ThreadContext.bind(subject);
    MagmaEngine.get().extend(new MagmaSecurityExtension() {
      @Override
      public void initialise() {
      }
    });
    return subject;
  }

  private static void check(ViewDto current, ViewDto updated) {
    ensureDerivationPermitted(updated, () -> !sameDerivation(current, updated), () -> referencesOf(updated));
  }

  private static void assertForbidden(ViewDto current, ViewDto updated) {
    try {
      check(current, updated);
      org.junit.Assert.fail("expected forbidden: " + updated);
    } catch(ForbiddenException e) {
      assertThat(e.getResponse().getStatus()).isEqualTo(403);
    }
  }

  @Test
  public void test_rename_onto_another_variable_needs_source_values() throws Exception {
    ThreadContext.bind(mock(Subject.class));
    MagmaEngine.get().extend(new MagmaSecurityExtension() {
      @Override
      public void initialise() {
      }
    });
    VariableDto x = variable("x", attr("script", "$('A')"));
    VariableDto y = variable("y", attr("script", "$('B')"));
    Datasource datasource = mock(Datasource.class);
    when(datasource.getName()).thenReturn("ds");
    ValueView view = mock(ValueView.class);
    when(view.getDatasource()).thenReturn(datasource);
    when(view.getName()).thenReturn("v");
    when(view.getStatus()).thenReturn(ValueTableStatus.READY);
    when(view.getVariable("x")).thenReturn(var(x));
    when(view.hasVariable("y")).thenReturn(true);
    when(view.getVariable("y")).thenReturn(var(y));
    ViewManager viewManager = mock(ViewManager.class);
    when(viewManager.getView("ds", "v")).thenReturn(view);
    ValueViewDtoExtension extension = mock(ValueViewDtoExtension.class);
    when(extension.isDtoOf(view)).thenReturn(true);
    when(extension.asDto(view)).thenReturn(list(x, y));
    ViewDtos viewDtos = new ViewDtos();
    viewDtos.setExtensions(java.util.List.of(extension));

    VariableViewResourceImpl resource = new VariableViewResourceImpl();
    inject(resource, "viewManager", viewManager);
    inject(resource, "viewDtos", viewDtos);
    resource.setValueTable(view);
    resource.setName("x");

    // x keeps its script, but y would get it in place of its own
    try {
      resource.createOrUpdateVariable(x.toBuilder().setName("y").build(), null);
      org.junit.Assert.fail("expected forbidden");
    } catch(ForbiddenException e) {
      verify(viewManager, never()).addView(anyString(), any(ValueView.class), anyString(), any(org.obiba.magma.views.support.VariableOperationContext.class));
    }
  }

  private static void inject(Object target, String field, Object value) throws Exception {
    java.lang.reflect.Field f = VariableViewResourceImpl.class.getDeclaredField(field);
    f.setAccessible(true);
    f.set(target, value);
  }

  //
  // Builders
  //

  private static ViewDto.Builder base() {
    return ViewDto.newBuilder().setName("v").addFrom("ds.T");
  }

  private static ViewDto list(VariableDto... variables) {
    return base().setExtension(VariableListViewDto.view,
        VariableListViewDto.newBuilder().addAllVariables(java.util.List.of(variables)).build()).build();
  }

  private static ViewDto resource(boolean allColumns) {
    return ViewDto.newBuilder().setName("v").addFrom("ds.res").setExtension(ResourceViewDto.view,
        ResourceViewDto.newBuilder().setAllColumns(allColumns).build()).build();
  }

  private static VariableDto variable(String name, AttributeDto... attributes) {
    return VariableDto.newBuilder().setName(name).setEntityType("Participant").setValueType("integer")
        .setIsRepeatable(false).addAllAttributes(java.util.List.of(attributes)).build();
  }

  private static AttributeDto attr(String name, String value) {
    return AttributeDto.newBuilder().setName(name).setValue(value).build();
  }

  private static Variable var(VariableDto dto) {
    return Dtos.fromDto(dto);
  }
}
