/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.jvm.view;

import org.obiba.opal.web.gwt.app.client.administration.jvm.presenter.JVMPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.PropertiesTable;
import org.obiba.opal.web.gwt.datetime.client.Duration;
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
  Label uptime;

  @UiField
  PropertiesTable javaProperties;

  @UiField
  PropertiesTable systemProperties;

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
    javaProperties.clearProperties();
    javaProperties.addProperty(translations.jvmMap().get("OPAL_VERSION"), env.getVersion());
    javaProperties.addProperty(translations.jvmMap().get("JAVA_VERSION"), env.getJavaVersion());
    javaProperties.addProperty(translations.jvmMap().get("VM_NAME"), env.getVmName());
    javaProperties.addProperty(translations.jvmMap().get("VM_VENDOR"), env.getVmVendor());
    javaProperties.addProperty(translations.jvmMap().get("VM_VERSION"), env.getVmVersion());

    systemProperties.clearProperties();
    JsArray<EntryDto> entries = JsArrays.toSafeArray(env.getSystemPropertiesArray());
    for(int i = 0; i < entries.length(); i++) {
      systemProperties.addProperty(entries.get(i).getKey(), entries.get(i).getValue());
    }
  }

  @Override
  public void initCharts() {
    memHeapChart = new MonitoringChartFactory();
    memHeapChart.createAreaSplineChart(translations.jvmMap().get("MEM_HEAP"), translations.jvmMap().get("MEGABYTES"),
        new String[] { translations.jvmMap().get("COMMITTED"), translations.jvmMap().get("USED") }, DURATION);
    memHeapChartColumn.clear();
    memHeapChart.getChart().setHeight(300);
    memHeapChartColumn.add(memHeapChart.getChart());

    memNonHeapChart = new MonitoringChartFactory();
    memNonHeapChart
        .createAreaSplineChart(translations.jvmMap().get("MEM_NON_HEAP"), translations.jvmMap().get("MEGABYTES"),
            new String[] { translations.jvmMap().get("COMMITTED"), translations.jvmMap().get("USED") }, DURATION);
    memNonHeapChartColumn.clear();
    memNonHeapChart.getChart().setHeight(300);
    memNonHeapChartColumn.add(memNonHeapChart.getChart());

    threadsChart = new MonitoringChartFactory();
    threadsChart.createSplineChart(translations.jvmMap().get("THREADS"), translations.jvmMap().get("COUNT"),
        new String[] { translations.jvmMap().get("PEAK"), translations.jvmMap().get("CURRENT") }, DURATION);
    threadsChartColumn.clear();
    threadsChart.getChart().setHeight(300);
    threadsChartColumn.add(threadsChart.getChart());

    gcChart = new MonitoringChartFactory();
    gcChart.createSplineChart(translations.jvmMap().get("GC_DELTA"), translations.jvmMap().get("DELTA"),
        translations.jvmMap().get("TIME_MS"),
        new String[] { translations.jvmMap().get("GC_COUNT"), translations.jvmMap().get("TIME_MS") }, DURATION);
    gcChart.getChart().setHeight(300);
    gcChart.getChart().getYAxis(0).setMin(0);
    gcChartColumn.clear();
    gcChartColumn.add(gcChart.getChart());
  }

  @Override
  public void renderStatus(OpalStatus status) {
    uptime.setText(TranslationsUtils
        .replaceArguments(translations.serverRunningFor(), Duration.create((int) status.getUptime()).humanize()));

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
