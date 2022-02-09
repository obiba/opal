/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.web.magma.view.JavaScriptViewDtoExtension;
import org.obiba.opal.web.magma.view.VariableListViewDtoExtension;
import org.obiba.opal.web.magma.view.ValueViewDtoExtension;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.ImmutableSet;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit tests for {@link ViewResourceImpl}.
 */
public class ViewResourceTest {

  @Before
  public void start() {
    new MagmaEngine();
  }

  @After
  public void stop() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void testConstructor_CallsSuperConstructorWithViewArgument() {
    // Setup
    ValueTable view = new View();

    // Exercise
    ViewResourceImpl sut = new ViewResourceImpl();
    sut.setValueTable(view);
    sut.setViewManager(createMock(ViewManager.class));
    sut.setViewDtos(newViewDtos());

    // Verify
    assertThat(sut.getValueTable()).isEqualTo(view);
  }

  @Test
  public void testGetFrom_ReturnsTableResourceForWrappedTable() {
    // Setup
    ValueTable fromTableMock = createMock(ValueTable.class);
    expect(fromTableMock.getName()).andReturn("fromTable").atLeastOnce();

    ApplicationContext mockContext = createMock(ApplicationContext.class);
    expect(mockContext.getBean("tableResource", TableResource.class)).andReturn(new TableResourceImpl()).atLeastOnce();

    ValueTable view = new View("testView", fromTableMock);
    ViewResourceImpl viewResource = new ViewResourceImpl();
    viewResource.setValueTable(view);
    viewResource.setViewManager(createMock(ViewManager.class));
    viewResource.setViewDtos(newViewDtos());
    viewResource.setApplicationContext(mockContext);

    replay(fromTableMock, mockContext);

    // Exercise
    TableResource fromTableResource = viewResource.getFrom();

    // Verify state
    assertThat(fromTableResource.getValueTable().getName()).isEqualTo("fromTable");
  }

  private ViewDtos newViewDtos() {
    ViewDtos viewDtos = new ViewDtos();
    viewDtos.setExtensions(
        ImmutableSet.<ValueViewDtoExtension>of(new JavaScriptViewDtoExtension(), new VariableListViewDtoExtension()));
    return viewDtos;
  }
}
