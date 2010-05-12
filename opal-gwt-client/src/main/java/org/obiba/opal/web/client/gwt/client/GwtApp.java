package org.obiba.opal.web.client.gwt.client;

import net.customware.gwt.presenter.client.DefaultEventBus;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.client.gwt.client.presenter.NavigatorPresenter;
import org.obiba.opal.web.client.gwt.client.presenter.VariablePresenter;
import org.obiba.opal.web.client.gwt.client.view.NavigatorView;
import org.obiba.opal.web.client.gwt.client.view.VariableView;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.ColumnChart;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GwtApp implements EntryPoint {

  private final EventBus eventBus = new DefaultEventBus();

  @Override
  public void onModuleLoad() {

    Runnable onLoadCallback = new Runnable() {
      @Override
      public void run() {
        NavigatorView navigator = new NavigatorView();
        VariableView variableView = new VariableView();
        navigator.getDetailsPanel().add(variableView);

        WidgetPresenter presenter = new VariablePresenter(variableView, eventBus);
        presenter.bind();

        presenter = new NavigatorPresenter(navigator, eventBus);
        presenter.bind();
        presenter.refreshDisplay();

        RootLayoutPanel.get().add(navigator);
      }
    };
    VisualizationUtils.loadVisualizationApi(onLoadCallback, ColumnChart.PACKAGE);
  }
}
