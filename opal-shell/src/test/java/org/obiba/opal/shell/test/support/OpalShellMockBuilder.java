/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.test.support;

import org.easymock.IExpectationSetters;
import org.obiba.opal.shell.OpalShell;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;

/**
 * A builder for creating {@link OpalShell} mocks.
 */
public class OpalShellMockBuilder {
  //
  // Instance Variables
  //

  private final OpalShell opalShellMock;

  private IExpectationSetters<?> expectationSetters;

  //
  // Constructors
  //

  public OpalShellMockBuilder() {
    opalShellMock = createMock(OpalShell.class);
  }

  //
  // Methods
  //

  public static OpalShellMockBuilder newBuilder() {
    return new OpalShellMockBuilder();
  }

  public OpalShellMockBuilder printf(String format, Object... args) {
    opalShellMock.printf(format, args);
    expectationSetters = expectLastCall();

    return this;
  }

  public OpalShellMockBuilder once() {
    if(expectationSetters != null) {
      expectationSetters.once();
      expectationSetters = null;
    }

    return this;
  }

  public OpalShellMockBuilder atLeastOnce() {
    if(expectationSetters != null) {
      expectationSetters.atLeastOnce();
      expectationSetters = null;
    }

    return this;
  }

  public OpalShellMockBuilder anyTimes() {
    if(expectationSetters != null) {
      expectationSetters.anyTimes();
      expectationSetters = null;
    }

    return this;
  }

  public OpalShell build() {
    return opalShellMock;
  }
}
