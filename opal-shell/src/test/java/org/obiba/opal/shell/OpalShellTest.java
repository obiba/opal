/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell;

import java.lang.annotation.Annotation;

import org.easymock.EasyMock;
import org.junit.Test;
import org.obiba.opal.shell.commands.CommandUsage;

import com.google.common.collect.Sets;

/**
 *
 */
public class OpalShellTest {

  @Test
  public void testPrintUsageOutputsIsSortedOnCommandName() {
    CommandUsage cu = new CommandUsage() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return CommandUsage.class;
      }

      @Override
      public String syntax() {
        return "Test";
      }

      @Override
      public String description() {
        return "Test";
      }
    };
    CommandRegistry mockRegistry = EasyMock.createMock(CommandRegistry.class);
    EasyMock.expect(mockRegistry.getAvailableCommands()).andReturn(Sets.newHashSet("b", "a", "d", "c")).times(2);
    // Method call order is important (that's what we're testing).
    EasyMock.checkOrder(mockRegistry, true);
    EasyMock.expect(mockRegistry.getCommandUsage("a")).andReturn(cu);
    EasyMock.expect(mockRegistry.getCommandUsage("b")).andReturn(cu);
    EasyMock.expect(mockRegistry.getCommandUsage("c")).andReturn(cu);
    EasyMock.expect(mockRegistry.getCommandUsage("d")).andReturn(cu);
    EasyMock.replay(mockRegistry);

    TestOpalShell testOpalShell = new TestOpalShell(mockRegistry);
    testOpalShell.printUsage();
    EasyMock.verify(mockRegistry);
  }

  private class TestOpalShell extends AbstractOpalShell {

    /**
     * @param registry
     */
    public TestOpalShell(CommandRegistry registry) {
      super(registry);
    }

    @Override
    public char[] passwordPrompt(String format, Object... args) {
      return null;
    }

    @Override
    public void printf(String format, Object... args) {

    }

    @Override
    public String prompt(String format, Object... args) {
      return null;
    }

  }

}
