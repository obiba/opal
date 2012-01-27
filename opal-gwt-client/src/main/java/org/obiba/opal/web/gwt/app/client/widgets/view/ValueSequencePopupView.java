/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.view;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.ValueSequencePopupPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 *
 */
public class ValueSequencePopupView extends PopupViewImpl implements ValueSequencePopupPresenter.Display {
  @UiTemplate("ValueSequencePopupView.ui.xml")
  interface ValueSequencePopupViewUiBinder extends UiBinder<Widget, ValueSequencePopupView> {
  }

  private static ValueSequencePopupViewUiBinder uiBinder = GWT.create(ValueSequencePopupViewUiBinder.class);

  private final Widget widget;

  @UiField
  DialogBox dialogBox;

  @UiField
  DockLayoutPanel content;

  @UiField
  Button closeButton;

  @UiField
  ResizeHandle resizeHandle;

  @Inject
  public ValueSequencePopupView(EventBus eventBus) {
    super(eventBus);
    this.widget = uiBinder.createAndBindUi(this);
    resizeHandle.makeResizable(content);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public HasClickHandlers getButton() {
    return closeButton;
  }

  @Override
  public void initialize(TableDto table, VariableDto variable, String entityIdentifier) {
    dialogBox.setText(variable.getName() + " - " + entityIdentifier);

  }
}
