package org.obiba.opal.web.gwt.app.client.ui;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.math.FrequencyDto;

import com.google.common.collect.ImmutableList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
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
    getFlexCellFormatter().setWidth(row, 1, "70px");
    setWidget(row, 0, new Label(translations.value()));
    getFlexCellFormatter().setRowSpan(row, 1, 2);
    setWidget(row, 1, new Label(translations.frequency()));
    setWidget(row++, 2, new Label("Percentage"));

    getFlexCellFormatter().addStyleName(row, 0, "bold-table-header");
    getFlexCellFormatter().addStyleName(row, 0, "table-subheader");
    getFlexCellFormatter().setWidth(row, 0, "70px");
    getFlexCellFormatter().addStyleName(row, 1, "bold-table-header");
    getFlexCellFormatter().addStyleName(row, 1, "table-subheader");
    getFlexCellFormatter().setWidth(row, 1, "70px");

    setWidget(row, 0, new Label(translations.subtotal()));
    setWidget(row++, 1, new Label(translations.totalLabel()));

    return row;
  }

  public void drawValuesFrequencies(ImmutableList<FrequencyDto> frequencies, String title, String emptyValueLabel,
      double subtotal, double total) {
    getFlexCellFormatter().setColSpan(row, 0, 4);
    getFlexCellFormatter().addStyleName(row, 0, "table-subheader");
    setWidget(row++, 0, new Label(title));

    // If no frequencies, show no values...
    if(frequencies.isEmpty()) {
      drawRow(emptyValueLabel, "0", "0%", "0%");
    } else {
      for(FrequencyDto frequency : frequencies) {
        if(frequency.hasValue()) {
          drawRow("NOT_NULL".equals(frequency.getValue()) ? translations.notEmpty() : frequency.getValue(),
              String.valueOf(Math.round(frequency.getFreq())), getPercentage(frequency.getFreq(), subtotal),
              formatDecimal(frequency.getPct() * 100) + "%");
        }
      }
    }

    drawSubtotal(frequencies, subtotal, total);
  }

  private void drawRow(String col1, String col2, String col3, String col4) {
    setWidget(row, 0, new Label(col1));
    setWidget(row, 1, new Label(col2));
    setWidget(row, 2, new Label(col3));
    setWidget(row++, 3, new Label(col4));
  }

  private void drawSubtotal(ImmutableList<FrequencyDto> frequencies, double subtotal,
      double total) {// Do not show subtotal when there is only 1 frequency value
    if(frequencies.size() > 1) {
      getFlexCellFormatter().addStyleName(row, 0, "table-subtotal");
      drawRow(translations.subtotal(), String.valueOf(Math.round(subtotal)), getPercentage(subtotal, subtotal),
          getPercentage(subtotal, total));
    }
  }

  public void drawTotal(double total) {
    getFlexCellFormatter().addStyleName(row, 0, "property-key");
    drawRow(translations.totalLabel(), String.valueOf(Math.round(total)), "-", getPercentage(total, total));
  }

  private String getPercentage(double numerator, double denominator) {
    if(denominator > 0) {
      return formatDecimal(numerator / denominator * 100) + "%";
    }

    return "0%";
  }

  private String formatDecimal(double number) {
    NumberFormat nf = NumberFormat.getFormat("#.##");
    return nf.format(number);
  }
}
