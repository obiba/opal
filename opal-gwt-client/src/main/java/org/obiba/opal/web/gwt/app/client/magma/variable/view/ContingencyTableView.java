/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.variable.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.ContingencyTablePresenter;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.NavPills;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class ContingencyTableView extends ViewImpl implements ContingencyTablePresenter.Display {

  private QueryResultDto queryResult;

  private VariableDto variable;

  private VariableDto crossWithVariable;

  private List<String> variableCategories;

  private List<String> crossWithCategories;

  private boolean showFrequencies = true;

  interface Binder extends UiBinder<Widget, ContingencyTableView> {}

  @UiField
  FlowPanel crossTable;

  @UiField
  NavPills pills;

  @UiField
  NavLink frequency;

  @UiField
  NavLink percentage;

  private final Translations translations;

  @Inject
  public ContingencyTableView(Binder uiBinder, Translations translations) {
    this.translations = translations;

    initWidget(uiBinder.createAndBindUi(this));

  }

  @UiHandler("percentage")
  public void onPercentage(ClickEvent event) {
    percentage.setActive(true);
    frequency.setActive(false);
    showFrequencies = false;
    crossTable.clear();
    draw();
  }

  @UiHandler("frequency")
  public void onFrequency(ClickEvent event) {
    frequency.setActive(true);
    percentage.setActive(false);
    showFrequencies = true;
    crossTable.clear();
    draw();
  }

  @Override
  public void init(QueryResultDto resource, VariableDto variableDto, List<String> variableCategories,
      VariableDto crossWithVariableDto, List<String> crossWithCategories) {
    queryResult = resource;
    variable = variableDto;
    crossWithVariable = crossWithVariableDto;
    this.variableCategories = variableCategories;
    this.crossWithCategories = crossWithCategories;
  }

  @Override
  public void draw() {

    // data
    if(queryResult.getFacetsArray().get(0).hasStatistics()) {
      pills.setVisible(false);
      ContinuousContingencyTable contingencyTable = new ContinuousContingencyTable(queryResult, variable,
          variableCategories, crossWithVariable, translations);
      crossTable.add(contingencyTable.buildFlexTable());
    } else {
      pills.setVisible(true);
      CategoricalContingencyTable contingencyTable = new CategoricalContingencyTable(queryResult, variable,
          variableCategories, crossWithVariable, crossWithCategories, translations, showFrequencies);
      crossTable.add(contingencyTable.buildFlexTable());
    }
  }

}
