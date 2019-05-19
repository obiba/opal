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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.presenter.VcsCommitHistoryModalPresenter;
import org.obiba.opal.web.gwt.app.client.ui.DiffTable;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.PropertiesTable;
import org.obiba.opal.web.gwt.datetime.client.FormatType;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.opal.VcsCommitInfoDto;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class VcsCommitHistoryModalView extends ModalPopupViewWithUiHandlers<ModalUiHandlers>
    implements VcsCommitHistoryModalPresenter.Display {

  interface DataShieldPackageViewUiBinder extends UiBinder<Widget, VcsCommitHistoryModalView> {}

  private static final DataShieldPackageViewUiBinder uiBinder = GWT.create(DataShieldPackageViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  PropertiesTable commitProperties;

  @UiField
  DiffTable diffTable;

  private final Widget widget;

  @UiField
  Modal dialog;

  @Inject
  public VcsCommitHistoryModalView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    dialog.setTitle(translations.vcsCommitHistoryModalTitle());
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setCommitInfo(VcsCommitInfoDto commitInfo) {
    commitProperties.clearProperties();
    commitProperties.addProperty(translations.commitInfoMap().get("id"), commitInfo.getCommitId());
    commitProperties.addProperty(translations.commitInfoMap().get("Author"), commitInfo.getAuthor());
    commitProperties.addProperty(translations.commitInfoMap().get("Date"),
        Moment.create(commitInfo.getDate()).format(FormatType.MONTH_NAME_TIME_SHORT));
    commitProperties.addProperty(translations.commitInfoMap().get("Comment"), commitInfo.getComment());
  }

  @Override
  public void setDiff(JsArrayString diffEntries) {
    if(diffEntries != null && diffEntries.length() > 0) {
      diffTable.setDiff(diffEntries.get(0));
    }

    diffTable.setVisible(diffEntries != null && diffEntries.length() > 0);
  }

  @UiHandler("closeButton")
  public void onCloseButton(ClickEvent event) {
    dialog.hide();
  }

}
