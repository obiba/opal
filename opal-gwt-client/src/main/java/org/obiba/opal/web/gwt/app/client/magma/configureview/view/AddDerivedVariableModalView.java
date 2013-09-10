/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.configureview.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.configureview.presenter.AddDerivedVariableModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.configureview.presenter.AddDerivedVariableModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class AddDerivedVariableModalView extends ModalPopupViewWithUiHandlers<AddDerivedVariableModalUiHandlers>
    implements AddDerivedVariableModalPresenter.Display {

  interface AddDerivedVariableModalUiBinder extends UiBinder<Widget, AddDerivedVariableModalView> {}

  private static final AddDerivedVariableModalUiBinder uiBinder = GWT.create(AddDerivedVariableModalUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  Modal dialog;

  @UiField(provided = true)
  SuggestBox variableNameSuggestBox;

  MultiWordSuggestOracle suggestions;

  @Inject
  public AddDerivedVariableModalView(EventBus eventBus) {
    super(eventBus);
    variableNameSuggestBox = new SuggestBox(suggestions = new MultiWordSuggestOracle());
    widget = uiBinder.createAndBindUi(this);
    dialog.setTitle(translations.entityDetailsModalTitle());
  }

  @Override
  public Widget asWidget() {
    return widget ;
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @UiHandler("cancelButton")
  public void onCancelClicked(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("addButton")
  public void onAddVariableClicked(ClickEvent event) {
    getUiHandlers().addVariable(getVariableName().getText());
  }

  @Override
  public HasText getVariableName() {
    return variableNameSuggestBox;
  }

  @Override
  public void clearVariableSuggestions() {
    suggestions.clear();
  }
}
