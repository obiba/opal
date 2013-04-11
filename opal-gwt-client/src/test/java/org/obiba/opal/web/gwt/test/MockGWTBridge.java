/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.test;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWTBridge;
import com.google.gwt.dev.About;

/**
 * A mock implementation of {@code GWTBridge} that allow to return mock implementations of types passed-in to
 * GWT.create() calls.
 * <p/>
 * To use this class, first extend the {@code AbstractGwtTestSetup} class to setup this instance during test runtime.
 * Then invoke the {@code #addMock(Class)} method to obtain a mock instance of the specified type. The returned instance
 * is ready for expectation setup using EasyMock expectations.
 */
public class MockGWTBridge extends GWTBridge {

  private static final Logger log = LoggerFactory.getLogger(MockGWTBridge.class);

  private final Map<Class<?>, Object> mocks = new HashMap<Class<?>, Object>();

  public <T> T addMock(Class<T> type) {
    T mock = EasyMock.createMock(type);
    mocks.put(type, mock);
    return mock;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T create(Class<?> classLiteral) {
    return (T) mocks.get(classLiteral);
  }

  @Override
  public String getVersion() {
    return About.getGwtVersionNum();
  }

  @Override
  public boolean isClient() {
    return false;
  }

  @Override
  public void log(String message, Throwable e) {
    log.warn(message, e);
  }

}
