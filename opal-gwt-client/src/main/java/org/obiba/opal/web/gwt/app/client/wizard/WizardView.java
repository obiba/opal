/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.PopupView;

public interface WizardView extends PopupView {

  HandlerRegistration addCancelClickHandler(ClickHandler handler);

  HandlerRegistration addFinishClickHandler(ClickHandler handler);

  HandlerRegistration addCloseClickHandler(ClickHandler handler);

}
