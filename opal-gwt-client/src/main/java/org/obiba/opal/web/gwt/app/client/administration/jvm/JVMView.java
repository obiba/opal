/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.jvm;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.CollapsiblePanel;
import org.obiba.opal.web.gwt.app.client.ui.PropertiesTable;
import org.obiba.opal.web.gwt.datetime.client.Duration;
import org.obiba.opal.web.gwt.plot.client.MonitoringChartFactory;
import org.obiba.opal.web.model.client.opal.EntryDto;
import org.obiba.opal.web.model.client.opal.OpalEnv;
import org.obiba.opal.web.model.client.opal.OpalStatus;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Column;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class JVMView extends ViewWithUiHandlers<JVMUiHandlers> implements JVMPresenter.Display {

  // 2 minutes in milliseconds
  private static final int DURATION = 2 * 60000;

  // Bytes to megabytes factor
  private static final double B_TO_MB_FACTOR = 0.000000954;

  public static final int HEIGHT = 300;

  private static final int MAX_LENGTH = 100;

  interface Binder extends UiBinder<Widget, JVMView> {}

  private MonitoringChartFactory memHeapChart;

  private MonitoringChartFactory memNonHeapChart;

  private MonitoringChartFactory threadsChart;

  private MonitoringChartFactory gcChart;

  private Integer gcCountMemento;

  // According to :GarbageCollectorMXBean, collection time is an accumulated collection elapsed time in milliseconds
  private Double gcTimeMemento;

  private Double initialTimestamp;

  @UiField
  HasWidgets breadcrumbs;

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

  @UiField
  Button gc;

  private final Translations translations;

  @Inject
  public JVMView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    gc.setTitle(translations.launchGarbageCollectorTitle());
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
      if("java.class.path".equals(entries.get(i).getKey())) {
        addCollapsibleProperty(entries.get(i));
      } else {
        systemProperties.addProperty(entries.get(i).getKey(), entries.get(i).getValue());
      }
    }
  }

  private void addCollapsibleProperty(EntryDto entry) {
    String header = entry.getValue().length() > MAX_LENGTH
        ? entry.getValue().substring(0, MAX_LENGTH) + "..."
        : entry.getValue();
    CollapsiblePanel value = new CollapsiblePanel(header);
    value.addStyleName("collapsible-property");
    value.add(new Label(entry.getValue()));
    value.setOpen(false);
    systemProperties.addProperty(new Label(entry.getKey()), value);
  }

  @Override
  public void initCharts() {
    initMemHeapChart();
    initMemeNonHeapChart();
    initThreadsCountChart();
    initGcChart();
  }

  private void initGcChart() {
    gcChart = new MonitoringChartFactory();
    gcChart.createSplineChart(translations.jvmMap().get("GC_DELTA"), translations.jvmMap().get("DELTA"),
        translations.jvmMap().get("TIME_MS"),
        new String[] { translations.jvmMap().get("GC_COUNT"), translations.jvmMap().get("TIME_MS") }, DURATION);
    gcChart.getChart().setHeight(HEIGHT);
    gcChart.getChart().getYAxis(0).setMin(0);
    gcChartColumn.clear();
    gcChartColumn.add(gcChart.getChart());
  }

  private void initThreadsCountChart() {
    threadsChart = new MonitoringChartFactory();
    threadsChart.createSplineChart(translations.jvmMap().get("THREADS"), translations.jvmMap().get("COUNT"),
        new String[] { translations.jvmMap().get("PEAK"), translations.jvmMap().get("CURRENT") }, DURATION);
    threadsChartColumn.clear();
    threadsChart.getChart().setHeight(HEIGHT);
    threadsChartColumn.add(threadsChart.getChart());
  }

  private void initMemeNonHeapChart() {
    memNonHeapChart = new MonitoringChartFactory();
    memNonHeapChart
        .createAreaSplineChart(translations.jvmMap().get("MEM_NON_HEAP"), translations.jvmMap().get("MEGABYTES"),
            new String[] { translations.jvmMap().get("COMMITTED"), translations.jvmMap().get("USED") }, DURATION);
    memNonHeapChartColumn.clear();
    memNonHeapChart.getChart().setHeight(HEIGHT);
    memNonHeapChartColumn.add(memNonHeapChart.getChart());
  }

  private void initMemHeapChart() {
    memHeapChart = new MonitoringChartFactory();
    memHeapChart.createAreaSplineChart(translations.jvmMap().get("MEM_HEAP"), translations.jvmMap().get("MEGABYTES"),
        new String[] { translations.jvmMap().get("COMMITTED"), translations.jvmMap().get("USED") }, DURATION);
    memHeapChartColumn.clear();
    memHeapChart.getChart().setHeight(HEIGHT);
    memHeapChartColumn.add(memHeapChart.getChart());
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

    updateGcChart(status, timestamp);

  }

  @UiHandler("gc")
  void onGc(ClickEvent event) {
    getUiHandlers().onGc();
  }

  private void updateGcChart(OpalStatus status, double timestamp) {// Garbage collectors
    JsArray<OpalStatus.GarbageCollectorUsage> gcs = JsArrays.toSafeArray(status.getGcsArray());

    initGcCountMemento(gcs);
    initGcTimeMemento(gcs);

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

  private void initGcTimeMemento(JsArray<OpalStatus.GarbageCollectorUsage> gcs) {
    if(gcTimeMemento == null) {
      gcTimeMemento = 0d;
      // get the initial gc count
      for(int i = 0; i < gcs.length(); i++) {
        gcTimeMemento += gcs.get(i).getCollectionTime();
      }
    }
  }

  private void initGcCountMemento(JsArray<OpalStatus.GarbageCollectorUsage> gcs) {
    if(gcCountMemento == null) {
      gcCountMemento = 0;
      // get the initial gc count
      for(int i = 0; i < gcs.length(); i++) {
        gcCountMemento += Double.valueOf(gcs.get(i).getCollectionCount()).intValue();
      }
    }
  }
}
