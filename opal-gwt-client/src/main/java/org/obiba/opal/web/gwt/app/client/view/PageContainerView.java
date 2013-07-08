package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.app.client.presenter.PageContainerPresenter;

import com.github.gwtbootstrap.client.ui.PageHeader;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class PageContainerView extends ViewImpl implements PageContainerPresenter.Display {
  @UiTemplate("PageContainerView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, PageContainerView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget uiWidget;

  @UiField
  Panel header;

  @UiField
  PageHeader pageTitle;

  @UiField
  Panel body;



  public PageContainerView() {
    uiWidget = uiBinder.createAndBindUi(this);
  }


  @Override
  public Widget asWidget() {
    return uiWidget;
  }

  @Override
  public void setPageTitle(String title) {
    pageTitle.setText(title);
  }

  @Override
  public void addToSlot(Object slot, Widget content) {
  }

  @Override
  public void removeFromSlot(Object slot, Widget content) {
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    if (PageContainerPresenter.HEADER == slot) {
      header.clear();
      header.add(content);
    }
    else if (PageContainerPresenter.CONTENT == slot) {
      body.clear();
      body.add(content);
    }
  }

}