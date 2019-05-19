/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.view;

import javax.validation.constraints.NotNull;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.presenter.ScriptEvaluationPopupPresenter.Display;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class ScriptEvaluationPopupView extends ModalPopupViewWithUiHandlers<ModalUiHandlers> implements Display {

  interface Binder extends UiBinder<Widget, ScriptEvaluationPopupView> {}

  private static final int MINIMUM_WIDTH = 610;

  private static final int MINIMUM_HEIGHT = 585;

  @UiField
  Modal dialogBox;

  @UiField
  FlowPanel evaluation;

  @Inject
  public ScriptEvaluationPopupView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    dialogBox.setTitle(translations.scriptEvaluationLabel());
    dialogBox.setMinWidth(MINIMUM_WIDTH);
    dialogBox.setMinHeight(MINIMUM_HEIGHT);
  }

  @UiHandler("closeButton")
  public void onClosedClicked(ClickEvent event) {
    dialogBox.hide();
  }

  @Override
  public void setInSlot(Object slot, IsWidget display) {
    if(slot == Slots.Evaluation) {
      evaluation.add(display);
    }
  }

  @Override
  public void showError(@NotNull String error) {
    dialogBox.addAlert(error, AlertType.ERROR);
  }

}
