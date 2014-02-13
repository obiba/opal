package org.obiba.opal.web.gwt.app.client.magma.variable.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.DefaultFlexTable;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Label;

public abstract class ContingencyTable {

  protected static final int DEFAULT_WIDTH = 60;

  protected DefaultFlexTable parentTable = new DefaultFlexTable();

  protected QueryResultDto queryResult;

  protected final VariableDto variable;

  protected List<String> variableCategories;

  protected final VariableDto crossWithVariable;

  protected final Translations translations;

  public ContingencyTable(QueryResultDto queryResult, VariableDto variableDto, List<String> variableCategories,
      VariableDto crossWithVariableDto, Translations translations) {
    this.queryResult = queryResult;
    variable = variableDto;
    this.variableCategories = variableCategories;
    crossWithVariable = crossWithVariableDto;
    this.translations = translations;
  }

  public DefaultFlexTable buildFlexTable() {
    populate();
    return parentTable;
  }

  abstract void populate();

  protected String formatDecimal(double number) {
    NumberFormat nf = NumberFormat.getFormat("#.##");
    return nf.format(number);
  }

  protected void writeCategoryHeader(DefaultFlexTable parentTable, String name, int col) {
    parentTable.setWidget(1, col, new Label(name));
    parentTable.getFlexCellFormatter().addStyleName(1, col, "cross-table-header");
  }

  protected void prepareTable() {
    parentTable.setWidget(0, 0, new Label(crossWithVariable.getName()));
    parentTable.setWidget(0, 1, new Label(variable.getName()));
    parentTable.setWidget(0, 2, new Label(translations.totalLabel()));
    parentTable.setWidget(0, 4, new Label(translations.NALabel()));

    parentTable.getFlexCellFormatter().setRowSpan(0, 0, 2);
    parentTable.getFlexCellFormatter().setRowSpan(0, 2, 2);

    parentTable.getFlexCellFormatter().setRowSpan(0, 4, 2);

    int size = variableCategories.size();
    parentTable.getFlexCellFormatter().setColSpan(0, 1, size);
    parentTable.getFlexCellFormatter().setWidth(0, 1, DEFAULT_WIDTH + "%");
    parentTable.getFlexCellFormatter().setWidth(0, 2, "10%");

    parentTable.getFlexCellFormatter().addStyleName(0, 0, "cross-table-header");
    parentTable.getFlexCellFormatter().addStyleName(0, 1, "cross-table-header");
    parentTable.getFlexCellFormatter().addStyleName(0, 2, "cross-table-header");
    parentTable.getFlexCellFormatter().addStyleName(0, 4, "cross-table-header");

    for(int i = 0; i < size; i++) {
      writeCategoryHeader(parentTable, variableCategories.get(i), i);
    }

    parentTable.getFlexCellFormatter().addStyleName(0, 3, "empty-col");
  }
}
