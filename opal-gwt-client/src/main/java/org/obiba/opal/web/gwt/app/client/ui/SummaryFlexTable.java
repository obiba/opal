package org.obiba.opal.web.gwt.app.client.ui;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;

public class SummaryFlexTable extends DefaultFlexTable {

  private static final Translations translations = GWT.create(Translations.class);

  private int row = 0;

  public int drawHeader() {

    getFlexCellFormatter().addStyleName(row, 0, "bold-table-header");
    getFlexCellFormatter().addStyleName(row, 1, "bold-table-header");
    getFlexCellFormatter().addStyleName(row, 2, "bold-table-header");
    getFlexCellFormatter().setColSpan(row, 2, 2);
    getFlexCellFormatter().setRowSpan(row, 0, 2);
    setWidget(row, 0, new Label(translations.value()));
    getFlexCellFormatter().setRowSpan(row, 1, 2);
    setWidget(row, 1, new Label(translations.frequency()));
    setWidget(row++, 2, new Label("Percentage"));

    getFlexCellFormatter().addStyleName(row, 0, "bold-table-header");
    getFlexCellFormatter().addStyleName(row, 0, "table-subheader");
    getFlexCellFormatter().setWidth(row, 0, "20%");
    getFlexCellFormatter().addStyleName(row, 1, "bold-table-header");
    getFlexCellFormatter().addStyleName(row, 1, "table-subheader");
    getFlexCellFormatter().setWidth(row, 1, "20%");

    setWidget(row, 0, new Label(translations.subtotal()));
    setWidget(row++, 1, new Label(translations.totalLabel()));

    return row;
  }
}
