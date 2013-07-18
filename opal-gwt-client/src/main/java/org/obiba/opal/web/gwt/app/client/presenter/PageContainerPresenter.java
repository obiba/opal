package org.obiba.opal.web.gwt.app.client.presenter;

import org.obiba.opal.web.gwt.app.client.place.Places;

import com.google.gwt.event.shared.GwtEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class PageContainerPresenter extends Presenter<PageContainerPresenter.Display,PageContainerPresenter.Proxy> {

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> HEADER = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> CONTENT = new GwtEvent.Type<RevealContentHandler<?>>();

  @Inject
  public PageContainerPresenter(Display display, EventBus eventBus, Proxy proxy) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
  }

  @ProxyStandard
  @NameToken(Places.admin)
  public interface Proxy extends ProxyPlace<PageContainerPresenter> {}

  public interface Display extends View {
    void setPageTitle(String title);
  }

  @Override
  public void setInSlot(Object slot, PresenterWidget<?> content) {

    if (CONTENT == slot) {
      if (content instanceof HasPageTitle) {
        HasPageTitle pageHeader = (HasPageTitle)content;

        if (pageHeader != null) {
          getView().setPageTitle(pageHeader.getTitle());
        }
      }

    }

    super.setInSlot(slot, content);
  }

}
