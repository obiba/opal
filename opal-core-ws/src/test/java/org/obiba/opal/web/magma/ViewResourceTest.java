/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.service.ImportService;
import org.obiba.opal.search.StatsIndexManager;
import org.obiba.opal.search.es.ElasticSearchConfigurationService;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.magma.view.JavaScriptViewDtoExtension;
import org.obiba.opal.web.magma.view.VariableListViewDtoExtension;
import org.obiba.opal.web.magma.view.ViewDtoExtension;
import org.obiba.opal.web.magma.view.ViewDtos;

import com.google.common.collect.ImmutableSet;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link ViewResource}.
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
    View view = new View();
    ViewManager mockViewManager = createMock(ViewManager.class);
    ImportService importService = createMock(ImportService.class);
    OpalSearchService opalSearchService = new OpalSearchService(
        new ElasticSearchConfigurationService(createMock(OpalConfigurationService.class)));
    StatsIndexManager statsIndexManager = createMock(StatsIndexManager.class);
    ElasticSearchProvider esProvider = createMock(ElasticSearchProvider.class);

    // Exercise
    ViewResource sut = new ViewResource(mockViewManager, view, newViewDtos(), opalSearchService, statsIndexManager,
        esProvider, importService);

    // Verify
    assertEquals(view, sut.getValueTable());
  }

  @Test
  public void testGetFrom_ReturnsTableResourceForWrappedTable() {
    // Setup
    ViewManager mockViewManager = createMock(ViewManager.class);
    ImportService importService = createMock(ImportService.class);
    OpalSearchService opalSearchService = new OpalSearchService(
        new ElasticSearchConfigurationService(createMock(OpalConfigurationService.class)));
    StatsIndexManager statsIndexManager = createMock(StatsIndexManager.class);
    ElasticSearchProvider esProvider = createMock(ElasticSearchProvider.class);

    ValueTable fromTableMock = createMock(ValueTable.class);
    expect(fromTableMock.getName()).andReturn("fromTable").atLeastOnce();

    View view = new View("testView", fromTableMock);
    ViewResource sut = new ViewResource(mockViewManager, view, newViewDtos(), ImmutableSet.of(new Locale("en")),
        importService, opalSearchService, statsIndexManager, esProvider);

    replay(fromTableMock);

    // Exercise
    TableResource fromTableResource = sut.getFrom();

    // Verify state
    assertEquals("fromTable", fromTableResource.getValueTable().getName());
  }

  private ViewDtos newViewDtos() {
    return new ViewDtos(
        ImmutableSet.<ViewDtoExtension>of(new JavaScriptViewDtoExtension(), new VariableListViewDtoExtension()));
  }
}
