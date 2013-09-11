/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.java.view;

import org.obiba.opal.web.gwt.app.client.administration.java.presenter.JVMPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.DefaultFlexTable;
import org.obiba.opal.web.gwt.app.client.ui.PropertiesTable;
import org.obiba.opal.web.gwt.plot.client.MonitoringChartFactory;
import org.obiba.opal.web.model.client.opal.EntryDto;
import org.obiba.opal.web.model.client.opal.OpalEnv;
import org.obiba.opal.web.model.client.opal.OpalStatus;

import com.github.gwtbootstrap.client.ui.Column;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class JVMView extends ViewImpl implements JVMPresenter.Display {

  // 2 minutes in milliseconds
  private static final int DURATION = 2 * 60000;

  // Bytes to megabytes factor
  private static final double B_TO_MB_FACTOR = 0.000000954;

  @UiTemplate("JVMView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, JVMView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget uiWidget;

  private MonitoringChartFactory memHeapChart;

  private MonitoringChartFactory memNonHeapChart;

  private MonitoringChartFactory threadsChart;

  private MonitoringChartFactory gcChart;

  private Integer gcCountMemento;

  // According to :GarbageCollectorMXBean, collection time is an accumulated collection elapsed time in milliseconds
  private Double gcTimeMemento;

  private Double initialTimestamp;

  @UiField
  Panel breadcrumbs;

  @UiField
  PropertiesTable javaProperties;

  @UiField
  ScrollPanel systemScroll;

  @UiField
  DefaultFlexTable systemProperties;

  @UiField
  Column memHeapChartColumn;

  @UiField
  Column memNonHeapChartColumn;

  @UiField
  Column threadsChartColumn;

  @UiField
  Column gcChartColumn;

  public JVMView() {
    uiWidget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return uiWidget;
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @Override
  public void renderProperties(OpalEnv env) {
    javaProperties.addProperty("Opal Version", env.getVersion());
    javaProperties.addProperty("Java Version", env.getJavaVersion());
    javaProperties.addProperty("VM Name", env.getVmName());
    javaProperties.addProperty("VM Vendor", env.getVmVendor());
    javaProperties.addProperty("VM Version", env.getVmVersion());

    int row = 0;
    JsArray<EntryDto> entries = JsArrays.toSafeArray(env.getSystemPropertiesArray());
    for(int i = 0; i < entries.length(); i++) {
      systemProperties.setWidget(row, 0, new Label(entries.get(i).getKey()));
      systemProperties.setWidget(row++, 1, new Label(entries.get(i).getValue()));
    }

    systemScroll.setHeight(javaProperties.getElement().getClientHeight() + "px");
  }

  @Override
  public void initCharts() {
    memHeapChart = new MonitoringChartFactory();
    memHeapChart.createAreaSplineChart("Memory Heap", "MegaBytes (Mb)", new String[] { "Committed", "Used" }, DURATION);
    memHeapChartColumn.clear();
    memHeapChart.getChart().setHeight(300);
    memHeapChartColumn.add(memHeapChart.getChart());

    memNonHeapChart = new MonitoringChartFactory();
    memNonHeapChart
        .createAreaSplineChart("Memory Non-Heap", "MegaBytes (Mb)", new String[] { "Commited", "Used" }, DURATION);
    memNonHeapChartColumn.clear();
    memNonHeapChart.getChart().setHeight(300);
    memNonHeapChartColumn.add(memNonHeapChart.getChart());

    threadsChart = new MonitoringChartFactory();
    threadsChart.createSplineChart("Threads", "Count", new String[] { "Peak", "Current" }, DURATION);
    threadsChartColumn.clear();
    threadsChart.getChart().setHeight(300);
    threadsChartColumn.add(threadsChart.getChart());

    gcChart = new MonitoringChartFactory();
    gcChart
        .createSplineChart("Garbage Collectors (delta)", "Delta", "Time (ms)", new String[] { "GC Count", "Time (ms)" },
            DURATION);
    gcChart.getChart().setHeight(300);
    gcChart.getChart().getYAxis(0).setMin(0);
    gcChartColumn.clear();
    gcChartColumn.add(gcChart.getChart());
  }

  @Override
  public void renderStatus(OpalStatus status) {
    if(initialTimestamp == null) {
      initialTimestamp = status.getTimestamp();
    }
    double timestamp = status.getTimestamp() - initialTimestamp;

    // Mem Heap
    memHeapChart.updateChart(0, timestamp, status.getHeapMemory().getCommitted() * B_TO_MB_FACTOR);
    memHeapChart.updateChart(1, timestamp, status.getHeapMemory().getUsed() * B_TO_MB_FACTOR);

    // Mem Non-Heap
    memNonHeapChart.updateChart(0, timestamp, status.getNonHeapMemory().getCommitted() * B_TO_MB_FACTOR);
    memNonHeapChart.updateChart(1, timestamp, status.getNonHeapMemory().getUsed() * B_TO_MB_FACTOR);

    // Threads
    threadsChart.updateChart(0, timestamp, status.getThreads().getPeak());
    threadsChart.updateChart(1, timestamp, status.getThreads().getCount());

    // Garbage collectors
    JsArray<OpalStatus.GarbageCollectorUsage> gcs = JsArrays.toSafeArray(status.getGcsArray());
    if(gcCountMemento == null) {
      gcCountMemento = 0;
      // get the initial gc count
      for(int i = 0; i < gcs.length(); i++) {
        gcCountMemento += Double.valueOf(gcs.get(i).getCollectionCount()).intValue();
      }
    }

    if(gcTimeMemento == null) {
      gcTimeMemento = 0d;
      // get the initial gc count
      for(int i = 0; i < gcs.length(); i++) {
        gcTimeMemento += gcs.get(i).getCollectionTime();
      }
    }

    // Count the number of GC
    int gcCount = 0;
    double gcTotalTime = 0d;
    for(int i = 0; i < gcs.length(); i++) {
      gcCount += gcs.get(i).getCollectionCount();
      gcTotalTime += gcs.get(i).getCollectionTime();
    }

    gcChart.updateChart(0, 0, timestamp, gcCount - gcCountMemento);
    gcChart.updateChart(1, 1, timestamp, gcCount - gcCountMemento == 0 ? 0 : gcTotalTime - gcTimeMemento);
    gcCountMemento = gcCount;
    gcTimeMemento = gcTotalTime;
  }

}
