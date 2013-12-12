/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.keystore.view;

import java.util.List;

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.keystore.presenter.EncryptionKeysPresenter;
import org.obiba.opal.web.gwt.app.client.keystore.presenter.EncryptionKeysUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.model.client.opal.KeyDto;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class EncryptionKeysView extends ViewWithUiHandlers<EncryptionKeysUiHandlers>
    implements EncryptionKeysPresenter.Display {

  @UiField
  SimplePager tablePager;

  @UiField
  CellTable<KeyDto> encryptionKeysTable;

  interface Binder extends UiBinder<Widget, EncryptionKeysView> {}

  private final Translations translations;

  private final ListDataProvider<KeyDto> keyPairsDataProvider = new ListDataProvider<KeyDto>();


  @Inject
  public EncryptionKeysView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    initializeTable();
  }

  @Override
  public HasActionHandler<KeyDto> getActions() {
    return EncryptionKeysColumns.ACTIONS;
  }

  @Override
  public void setData(@Nonnull List<KeyDto> keyPairs) {
    renderTable(keyPairs);
  }

  private void renderTable(List<KeyDto> keyPairs) {
    keyPairsDataProvider.setList(keyPairs);
    tablePager.firstPage();
    keyPairsDataProvider.refresh();
    tablePager.setVisible(keyPairsDataProvider.getList().size() > tablePager.getPageSize());
  }

  private void initializeTable() {
    tablePager.setDisplay(encryptionKeysTable);
    encryptionKeysTable.addColumn(EncryptionKeysColumns.NAME, translations.nameLabel());
    encryptionKeysTable.addColumn(EncryptionKeysColumns.TYPE, translations.typeLabel());
    encryptionKeysTable.addColumn(EncryptionKeysColumns.ACTIONS, translations.actionsLabel());
    keyPairsDataProvider.addDataDisplay(encryptionKeysTable);
  }

  private static final class EncryptionKeysColumns {

    static final Column<KeyDto, String> NAME = new TextColumn<KeyDto>() {

      @Override
      public String getValue(KeyDto keyPair) {
        return keyPair.getAlias();
      }
    };

    static final Column<KeyDto, String> TYPE = new TextColumn<KeyDto>() {

      @Override
      public String getValue(KeyDto keyPair) {
        return keyPair.getKeyType().getName();
      }
    };

    static final ActionsColumn<KeyDto> ACTIONS = new ActionsColumn<KeyDto>(new ActionsProvider<KeyDto>() {

      @Override
      public String[] allActions() {
        return new String[] { DOWNLOAD_CERTIFICATE_ACTION, ActionsColumn.DELETE_ACTION };
      }

      @Override
      public String[] getActions(KeyDto value) {
        return allActions();
      }
    });

  }

}
