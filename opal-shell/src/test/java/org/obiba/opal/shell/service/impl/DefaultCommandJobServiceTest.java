/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.service.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.audit.UserProvider;
import org.obiba.opal.shell.CommandJob;

/**
 * Unit tests for {@link DefaultCommandJobService}.
 */
public class DefaultCommandJobServiceTest {
  //
  // Instance Variables
  //

  private DefaultCommandJobService sut;

  private ExecutorService mockExecutorService;

  private UserProvider mockUserProvider;

  private CommandJob commandJob;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    commandJob = new CommandJob();

    // Expect commandJob executed once.
    mockExecutorService = createMock(ExecutorService.class);
    mockExecutorService.execute(commandJob);
    expectLastCall().once();

    // Expect current user is "testUser".
    mockUserProvider = createMock(UserProvider.class);
    expect(mockUserProvider.getUsername()).andReturn("testUser").atLeastOnce();

    sut = new DefaultCommandJobService();
    sut.setExecutorService(mockExecutorService);
    sut.setUserProvider(mockUserProvider);

    replay(mockExecutorService, mockUserProvider);
  }

  //
  // Test Methods
  //

  @Test
  public void testLaunchCommand_AssignsIdToCommandJob() {
    sut.launchCommand(commandJob);

    assertEquals((Long) 1l, commandJob.getId());
  }

  @Test
  public void testLaunchCommand_MakesTheCurrentUserTheOwnerOfTheCommandJob() {
    sut.launchCommand(commandJob);

    verify(mockExecutorService);

    assertEquals("testUser", commandJob.getOwner());
  }

  @Test
  public void testLaunchCommand_ExecutesCommandJob() {
    sut.launchCommand(commandJob);

    verify(mockExecutorService);
  }
}
