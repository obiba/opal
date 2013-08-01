package org.obiba.opal.web.gwt.app.client.project.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.project.presenter.AddProjectModalPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.AddProjectModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.opal.ProjectFactoryDto;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.github.gwtbootstrap.client.ui.event.ClosedEvent;
import com.github.gwtbootstrap.client.ui.event.ClosedHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class AddProjectModalView extends ModalPopupViewWithUiHandlers<AddProjectModalUiHandlers>
    implements AddProjectModalPresenter.Display {

  interface AddProjectModalViewUiBinder extends UiBinder<Widget, AddProjectModalView> {}

  private static final AddProjectModalViewUiBinder uiBinder = GWT.create(AddProjectModalViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Modal modal;

  @UiField
  Panel alertPlace;

  @UiField
  ControlGroup labelGroup;

  @UiField
  HasText nameTxt;

  @UiField
  HasText descriptionTxt;

  Alert alert;

  @Inject
  public AddProjectModalView(EventBus eventBus) {
    super(eventBus);
    uiBinder.createAndBindUi(this);
    modal.setTitle(translations.addProject());
  }

  @Override
  public Widget asWidget() {
    return modal;
  }

  @UiHandler("save")
  void onSaveProject(ClickEvent event) {
    // validate
    ProjectFactoryDto p = ProjectFactoryDto.create();
    p.setName(nameTxt.getText());
    p.setDescription(descriptionTxt.getText());
    if(getUiHandlers().addProject(p)) {
      modal.hide();
    }
  }

  @UiHandler("cancel")
  void onCancelAddProject(ClickEvent event) {
    modal.hide();
  }

  private void clearAlert() {
    labelGroup.setType(ControlGroupType.NONE);
    if(alert != null && alert.getElement().hasParentElement()) {
      alert.removeFromParent();
    }
    alertPlace.clear();
  }

  @Override
  public void setNameError(String message) {
    clearAlert();
    labelGroup.setType(ControlGroupType.ERROR);
    alert = new Alert(message);
    alert.setType(AlertType.ERROR);
    alert.setAnimation(true);
    alert.setClose(true);
    alert.addClosedHandler(new ClosedHandler() {
      @Override
      public void onClosed(ClosedEvent closedEvent) {
        clearAlert();
      }
    });
    alertPlace.add(alert);
  }

}
