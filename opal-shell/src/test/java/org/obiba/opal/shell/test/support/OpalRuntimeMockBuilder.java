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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;

import org.easymock.IExpectationSetters;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.fs.OpalFileSystem;

/**
 * A builder for creating {@link OpalRuntime} mocks.
 */
public class OpalRuntimeMockBuilder {
  //
  // Instance Variables
  //

  private OpalRuntime opalRuntimeMock;

  private IExpectationSetters<?> expectationSetters;

  //
  // Constructors
  //

  public OpalRuntimeMockBuilder() {
    opalRuntimeMock = createMock(OpalRuntime.class);
  }

  //
  // Methods
  //

  public static OpalRuntimeMockBuilder newBuilder() {
    return new OpalRuntimeMockBuilder();
  }

  public OpalRuntimeMockBuilder withOpalConfiguration(OpalConfiguration opalConfiguration) {
    expect(opalRuntimeMock.getOpalConfiguration()).andReturn(opalConfiguration).anyTimes();

    return this;
  }

  public OpalRuntimeMockBuilder withOpalFileSystem(OpalFileSystem opalFileSystem) {
    expect(opalRuntimeMock.getFileSystem()).andReturn(opalFileSystem).anyTimes();

    return this;
  }

  public OpalRuntime build() {
    return opalRuntimeMock;
  }
}
