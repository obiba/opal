/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma;

import org.obiba.opal.web.gwt.app.client.magma.event.GeoValueDisplayEvent;
import org.obiba.opal.web.gwt.app.client.ui.LineStringValueMap;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.PointValueMap;
import org.obiba.opal.web.gwt.app.client.ui.PolygonValueMap;
import org.obiba.opal.web.gwt.app.client.ui.ValueMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 *
 */
public class ValueMapPopupView extends ModalPopupViewWithUiHandlers<ModalUiHandlers>
    implements ValueMapPopupPresenter.Display {

  private static final int MINIMUM_WIDTH = 500;

  private static final int MINIMUM_HEIGHT = 500;

  interface ValueMapPopupViewUiBinder extends UiBinder<Widget, ValueMapPopupView> {}

  private static final ValueMapPopupViewUiBinder uiBinder = GWT.create(ValueMapPopupViewUiBinder.class);

  private final Widget widget;

  @UiField
  Modal dialog;

  @UiField
  Panel panel;

  @Inject
  public ValueMapPopupView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    dialog.setMinWidth(MINIMUM_WIDTH);
    dialog.setMinHeight(MINIMUM_HEIGHT);
    dialog.setPadding(0);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @UiHandler("closeButton")
  public void onCloseButtonClicked(ClickEvent event) {
    dialog.hide();
  }

  @Override
  public void initialize(GeoValueDisplayEvent event) {
    dialog.setTitle(event.getVariable().getName() + " - " + event.getEntityIdentifier());
    panel.clear();
    ValueMap map = null;
    if("point".equals(event.getVariable().getValueType())) {
      map = new PointValueMap(event.getVariable(), event.getValue(), event.getIndex());
    } else if("linestring".equals(event.getVariable().getValueType())) {
      map = new LineStringValueMap(event.getVariable(), event.getValue(), event.getIndex());
    } else if("polygon".equals(event.getVariable().getValueType())) {
      map = new PolygonValueMap(event.getVariable(), event.getValue(), event.getIndex());
    }
    if(map != null) {
      // TODO needs more refining and also a resize handler on the dialog
      map.setHeight(String.valueOf(dialog.getBodyHeight()) + "px");
      map.setWidth("100%");
      panel.add(map);
    }
  }

  //
  // Private methods
  //

}
