/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.validator;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;

/**
 * Helper class, defining a dummy implementation of {@link HasValue<Boolean>}.
 */
public class HasBooleanValue implements HasValue<Boolean> {

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
    return null;
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
  }

  @Override
  public Boolean getValue() {
    return true;
  }

  @Override
  public void setValue(Boolean value) {
  }

  @Override
  public void setValue(Boolean value, boolean fireEvents) {
  }

}