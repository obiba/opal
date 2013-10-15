package org.obiba.opal.web.gwt.app.client.project.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.project.presenter.AddProjectModalPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.AddProjectModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.opal.ProjectFactoryDto;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class AddProjectModalView extends ModalPopupViewWithUiHandlers<AddProjectModalUiHandlers>
    implements AddProjectModalPresenter.Display {

  interface Binder extends UiBinder<Widget, AddProjectModalView> {}

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Modal modal;

  @UiField
  ControlGroup labelGroup;

  @UiField
  HasText nameTxt;

  @UiField
  HasText titleTxt;

  @UiField
  HasText descriptionTxt;

  @Inject
  public AddProjectModalView(EventBus eventBus, Binder uiBinder) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    modal.setTitle(translations.addProject());
  }

  @UiHandler("save")
  void onSaveProject(ClickEvent event) {
    // validate
    ProjectFactoryDto p = ProjectFactoryDto.create();
    p.setName(nameTxt.getText());
    p.setTitle(Strings.isNullOrEmpty(titleTxt.getText()) ? nameTxt.getText() : titleTxt.getText());
    p.setDescription(descriptionTxt.getText());
    if(getUiHandlers().addProject(p)) {
      modal.hide();
    }
  }

  @UiHandler("cancel")
  void onCancelAddProject(ClickEvent event) {
    modal.hide();
  }

  @Override
  public void setNameError(String message) {
    modal.clearAlert();
    labelGroup.setType(ControlGroupType.ERROR);
    modal.addAlert(message, AlertType.ERROR, labelGroup);
  }

}
