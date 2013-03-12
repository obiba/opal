package org.obiba.opal.core.cfg;

import java.io.File;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewPersistenceStrategy;
import org.obiba.magma.xstream.MagmaXStreamExtension;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.thoughtworks.xstream.io.StreamException;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OpalViewPersistenceStrategyTest {

  private static final String EMPTY_DIRECTORY = "empty";

  private ViewPersistenceStrategy viewPersistenceStrategy;

  @Before
  public void setUp() throws Exception {
    System.setProperty(OpalViewPersistenceStrategy.OPAL_HOME_SYSTEM_PROPERTY_NAME, getTestFilesRoot());
    viewPersistenceStrategy = new OpalViewPersistenceStrategy();
    new MagmaEngine().extend(new MagmaXStreamExtension());
  }

  @After
  public void tearDown() throws Exception {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void testReadWithNonExistentDirectory() throws Exception {
    System.setProperty(OpalViewPersistenceStrategy.OPAL_HOME_SYSTEM_PROPERTY_NAME,
        getTestFilesRoot() + File.separator + EMPTY_DIRECTORY);
    // Re-initialise to pick up the the OPAL_HOME specified for this test.
    viewPersistenceStrategy = new OpalViewPersistenceStrategy();
    Set<View> result = viewPersistenceStrategy.readViews("datasourceName");
    assertThat(result.isEmpty(), is(true));
  }

  @Test
  public void testReadWithNoViewsFileReturnsEmptySet() throws Exception {
    Set<View> result = viewPersistenceStrategy.readViews("datasourceName");
    assertThat(result.isEmpty(), is(true));
  }

  @Test
  public void testReadOfSimpleView() throws Exception {
    Set<View> result = viewPersistenceStrategy.readViews("simple-views");
    assertThat(result.size(), is(1));
  }

  @Test(expected = StreamException.class)
  public void testReadofEmptyViewFileThrowsStreamException() throws Exception {
    Set<View> result = viewPersistenceStrategy.readViews("empty-views");
    assertThat(result.isEmpty(), is(true));
  }

  @Ignore
  @Test
  public void testWriteEmptySetRemovesFile() throws Exception {
    Datasource datasourceMock = createMock(Datasource.class);
    expect(datasourceMock.getName()).andReturn("opal-data").atLeastOnce();

    ValueTable valueTableMock = createMock(ValueTable.class);
    expect(valueTableMock.getName()).andReturn("Participants").atLeastOnce();
    expect(valueTableMock.getDatasource()).andReturn(datasourceMock).atLeastOnce();

    replay(datasourceMock, valueTableMock);

    // Write a temporary views file with a single view.
    Set<View> views = Sets.<View>newHashSet();
    View view = new View("aView", valueTableMock);
    views.add(view);
    viewPersistenceStrategy.writeViews("temporary-views", views);
    // Verify the temporary views file exists.
    Set<View> singleViewResult = viewPersistenceStrategy.readViews("temporary-views");
    assertThat(singleViewResult.size(), is(1));
    // Write the temporary views file with an empty views set. This will remove the file.
    viewPersistenceStrategy.writeViews("temporary-views", ImmutableSet.<View>of());
    // Verify that the temporary file has been removed, by ensuring that an empty set has been returned.
    Set<View> noViewsResult = viewPersistenceStrategy.readViews("temporary-views");
    assertThat(noViewsResult.isEmpty(), is(true));
  }

  @Ignore
  @Test
  public void testWriteSingleView() throws Exception {
    Datasource datasourceMock = createMock(Datasource.class);
    expect(datasourceMock.getName()).andReturn("opal-data").atLeastOnce();

    ValueTable valueTableMock = createMock(ValueTable.class);
    expect(valueTableMock.getName()).andReturn("Participants").atLeastOnce();
    expect(valueTableMock.getDatasource()).andReturn(datasourceMock).atLeastOnce();

    replay(datasourceMock, valueTableMock);

    Set<View> views = Sets.<View>newHashSet();
    View view = new View("aView", valueTableMock);
    views.add(view);
    viewPersistenceStrategy.writeViews("single-views", views);
  }

  private String getTestFilesRoot() {
    return this.getClass().getResource("/" + this.getClass().getSimpleName()).getFile();
  }
}
