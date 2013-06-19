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

import org.obiba.opal.web.gwt.app.client.navigator.event.GeoValueDisplayEvent;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ValueOccurrenceColumn.ValueOccurrence;
import org.obiba.opal.web.gwt.app.client.widgets.maps.PointValueMap;
import org.obiba.opal.web.gwt.app.client.widgets.maps.ValueMap;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ValueMapPopupPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
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
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 *
 */
public class ValueMapPopupView extends PopupViewImpl implements ValueMapPopupPresenter.Display {

  @UiTemplate("ValueMapPopupView.ui.xml")
  interface ValueMapPopupViewUiBinder extends UiBinder<Widget, ValueMapPopupView> {}

  private static final ValueMapPopupViewUiBinder uiBinder = GWT.create(ValueMapPopupViewUiBinder.class);

  private final Widget widget;

  @UiField
  DialogBox dialogBox;

  @UiField
  DockLayoutPanel content;

  @UiField
  Panel panel;

  @UiField
  Button closeButton;

  @Inject
  public ValueMapPopupView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
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
  public void initialize(GeoValueDisplayEvent event) {
    dialogBox.setModal(true);
    dialogBox.setText(event.getVariable().getName() + " - " + event.getEntityIdentifier());
    panel.clear();
    ValueMap map = new PointValueMap(event.getVariable(), event.getValue(), event.getIndex());
    panel.add(map);
  }

  //
  // Private methods
  //

}
