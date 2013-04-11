package org.obiba.opal.web.gwt.app.client.administration.presenter;

import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.Tab;
import com.gwtplatform.mvp.client.TabContainerPresenter;
import com.gwtplatform.mvp.client.TabPanel;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.RequestTabs;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.TabContentProxy;

public class AdministrationPresenter
    extends TabContainerPresenter<AdministrationPresenter.Display, AdministrationPresenter.Proxy> {

  @RequestTabs
  public static final Type<RequestTabsHandler> RequestTabs = new Type<RequestTabsHandler>();

  @ContentSlot
  public static final Type<RevealContentHandler<?>> TabSlot = new Type<RevealContentHandler<?>>();

  private final PlaceManager placeManager;

  private TabContentProxy<?> defaultTab;

  @Inject
  public AdministrationPresenter(Display display, EventBus eventBus, Proxy proxy, PlaceManager placeManager) {
    super(eventBus, display, proxy, TabSlot, RequestTabs);
    this.placeManager = placeManager;
  }

  @Override
  public Tab addTab(TabContentProxy<?> tabProxy) {
    if(defaultTab == null || tabProxy.getTabData().getPriority() < defaultTab.getTabData().getPriority()) {
      defaultTab = tabProxy;
    }
    return super.addTab(tabProxy);
  }

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, ApplicationPresenter.WORKBENCH, this);
  }

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    super.prepareFromRequest(request);
    // We need to request a specific tab in order to display something
    placeManager.revealPlace(new PlaceRequest(defaultTab.getTargetHistoryToken()));
  }

  public interface Display extends View, TabPanel {}

  @ProxyStandard
  @NameToken(Places.administration)
  public interface Proxy extends ProxyPlace<AdministrationPresenter> {}

}
