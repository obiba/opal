/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.identifiersmappings;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.project.identifiersmappings.event.IdentifiersMappingAddedEvent;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.ProjectDto;

public class ProjectIdentifiersMappingsModalView extends
  ModalPopupViewWithUiHandlers<ProjectIdentifiersMappingsModalUiHandlers> implements
  ProjectIdentifiersMappingsModalPresenter.Display {

  interface Binder extends UiBinder<Widget, ProjectIdentifiersMappingsModalView> { }

  private final Translations translations;

  @UiField
  Modal modal;

  @UiField
  ControlGroup entityTypeGroup;

  @UiField
  Chooser entityTypes;

  @UiField
  ControlGroup mappingsGroups;

  @UiField
  Chooser mappings;

  @Inject
  public ProjectIdentifiersMappingsModalView(EventBus eventBus,
                                             Binder binder,
                                             Translations translations) {
    super(eventBus);
    initWidget(binder.createAndBindUi(this));
    this.translations = translations;
    modal.setTitle("Test");
  }

  @Override
  public void initialize(ProjectDto.IdentifiersMappingDto identifiersMappingDto) {
  }

  @Override
  public void clearEntityTypes() {
    entityTypes.clear();
  }

  @Override
  public void clearMappings() {
    mappings.clear();
  }

  @Override
  public void addEntityType(TableDto table) {
    entityTypes.addItem(table.getEntityType(), table.getName());
  }

  @Override
  public void addMapping(String name) {
    mappings.addItem(name);
  }

  @Override
  public void selectEntityType(String entityType) {
    entityTypes.setSelectedValue(entityType);
  }

  @Override
  public void selectMapping(String variableName) {
    mappings.setSelectedValue(variableName);
  }

  @Override
  public void hideDialog() {
    modal.hide();
  }

  @UiHandler("cancelButton")
  public void cancelButtonClick(ClickEvent event) {
    hideDialog();
  }

  @UiHandler("entityTypes")
  public void entityTypesChange(ChangeEvent event) {
    getUiHandlers().updateMappings(entityTypes.getSelectedValue());
  }

  @UiHandler("saveButton")
  public void saveButtonClick(ClickEvent event) {
    ProjectDto.IdentifiersMappingDto mapping = ProjectDto.IdentifiersMappingDto.create();
    mapping.setEntityType(entityTypes.getSelectedItemText());
    mapping.setName(entityTypes.getSelectedValue());
    mapping.setMapping(mappings.getSelectedValue());
    getUiHandlers().save(mapping);
  }
}
