package org.obiba.opal.web.gwt.app.client;

import org.obiba.opal.web.gwt.app.client.presenter.NavigatorPresenter;
import org.obiba.opal.web.gwt.inject.client.OpalGinjector;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.ColumnChart;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GwtApp implements EntryPoint {

  @Override
  public void onModuleLoad() {
    final OpalGinjector opalGinjector = GWT.create(OpalGinjector.class);

    Runnable onLoadCallback = new Runnable() {
      @Override
      public void run() {

        NavigatorPresenter presenter = opalGinjector.getNavigatorPresenter();
        presenter.bind();
        presenter.revealDisplay();

        RootLayoutPanel.get().add(presenter.getDisplay().asWidget());
      }
    };
    VisualizationUtils.loadVisualizationApi(onLoadCallback, ColumnChart.PACKAGE);
  }
}
