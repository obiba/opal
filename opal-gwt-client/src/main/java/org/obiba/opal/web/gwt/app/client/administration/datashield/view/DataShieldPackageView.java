/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.datashield.view;

import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldPackagePresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 *
 */
public class DataShieldPackageView extends PopupViewImpl implements DataShieldPackagePresenter.Display {

  @UiTemplate("DataShieldPackageView.ui.xml")
  interface DataShieldPackageViewUiBinder extends UiBinder<DialogBox, DataShieldPackageView> {}

  private static DataShieldPackageViewUiBinder uiBinder = GWT.create(DataShieldPackageViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  DialogBox dialog;

  @UiField
  DockLayoutPanel contentLayout;

  @UiField
  ResizeHandle resizeHandle;

  @UiField
  Button closeButton;

  @UiField
  Label name;

  @UiField
  Label packageName;

  @UiField
  Label version;

  @UiField
  Label title;

  @UiField
  Label author;

  @UiField
  Label maintainer;

  @UiField
  Label depends;

  @UiField
  Label description;

  @UiField
  Label license;

  @UiField
  Label opalVersion;

  @UiField
  Anchor url;

  @UiField
  Anchor bugReports;

  //
  // Constructors
  //

  @Inject
  public DataShieldPackageView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    initWidgets();
  }

  private void initWidgets() {
    dialog.hide();
    resizeHandle.makeResizable(contentLayout);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  protected PopupPanel asPopupPanel() {
    return dialog;
  }

  @Override
  public void show() {
    dialog.setText(translations.dataShieldPackageDescription());
    super.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public HasClickHandlers getCloseButton() {
    return closeButton;
  }

  @Override
  public void setName(String name) {
    this.name.setText(name != null ? name : "");
  }

  @Override
  public void setPackageName(String packageName) {
    this.packageName.setText(packageName != null ? packageName : "");
  }

  @Override
  public void setVersion(String version) {
    this.version.setText(version != null ? version : "");
  }

  @Override
  public void setTitle(String title) {
    this.title.setText(title != null ? title : "");
  }

  @Override
  public void setAuthor(String author) {
    this.author.setText(author != null ? author : "");
  }

  @Override
  public void setMaintainer(String maintainer) {
    this.maintainer.setText(maintainer != null ? maintainer : "");
  }

  @Override
  public void setDepends(String depends) {
    this.depends.setText(depends != null ? depends : "");
  }

  @Override
  public void setDescription(String description) {
    this.description.setText(description != null ? description : "");
  }

  @Override
  public void setLicense(String license) {
    this.license.setText(license != null ? license : "");
  }

  @Override
  public void setOpalVersion(String opalVersion) {
    this.opalVersion.setText(opalVersion != null ? opalVersion : "");
  }

  @Override
  public void setUrl(String url, String href) {
    if(url != null) {
      this.url.setHref(href);
      this.url.setText(url);
      this.url.setTarget("_blank");
    } else {
      this.url.setText("");
    }
  }

  @Override
  public void setBugReports(String bugReports, String href) {
    if(bugReports != null) {
      this.bugReports.setHref(href);
      this.bugReports.setText(bugReports);
      this.bugReports.setTarget("_blank");
    } else {
      this.bugReports.setText("");
    }
  }

}
