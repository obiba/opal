package org.obiba.opal.core.runtime.upgrade;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewPersistenceStrategy;
import org.obiba.opal.core.cfg.OpalViewPersistenceStrategy;
import org.obiba.runtime.Version;

import com.google.common.collect.ImmutableSet;

public class ViewsUpgrade_1_4_0Test {

  private ViewsUpgrade_1_4_0 sut;

  private ViewPersistenceStrategy viewPersistenceStrategy;

  private File originalOpalConfigFile = new File(getTestFilesRoot() + File.separator + "original-opal-config.xml");

  private File workingOpalConfigFile = new File(getTestFilesRoot() + File.separator + "opal-config.xml");

  @Before
  public void setUp() throws Exception {
    System.setProperty(OpalViewPersistenceStrategy.OPAL_HOME_SYSTEM_PROPERTY_NAME, getTestFilesRoot());
    sut = new ViewsUpgrade_1_4_0();
    viewPersistenceStrategy = new OpalViewPersistenceStrategy();
    FileUtils.copyFile(originalOpalConfigFile, workingOpalConfigFile);
  }

  @After
  public void tearDown() throws Exception {
    // Delete view files.
    viewPersistenceStrategy.writeViews("starbucks", ImmutableSet.<View> of());
    viewPersistenceStrategy.writeViews("two words", ImmutableSet.<View> of());
    viewPersistenceStrategy.writeViews("robin", ImmutableSet.<View> of());
    viewPersistenceStrategy.writeViews("dylan", ImmutableSet.<View> of());
    workingOpalConfigFile.delete();
  }

  @Test(expected = RuntimeException.class)
  public void testNonExistentOpalConfigurationFile() throws Exception {
    sut.setConfigFile(new File(getTestFilesRoot() + File.separator + "non-existent-opal-config.xml"));
    sut.readOpalConfiguration();
  }

  @Test
  public void testExistingOpalConfigurationFile() throws Exception {
    sut.setConfigFile(workingOpalConfigFile);
    sut.readOpalConfiguration();
    assertThat(sut.getOpalConfiguration(), notNullValue());
  }

  @Test
  public void testUgradeWritesNewViews() throws Exception {
    File starbucksViews = new File(getTestFilesRoot() + File.separator + OpalViewPersistenceStrategy.CONF_DIRECTORY_NAME + File.separator + OpalViewPersistenceStrategy.VIEWS_DIRECTORY_NAME + File.separator + "starbucks.xml");
    assertThat(starbucksViews.exists(), is(false));

    sut.setConfigFile(workingOpalConfigFile);
    sut.execute(new Version("22.22"));
    assertThat(starbucksViews.exists(), is(true));
  }

  private String getTestFilesRoot() {
    return this.getClass().getResource("/" + this.getClass().getSimpleName()).getFile();
  }

}
