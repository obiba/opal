package org.obiba.opal.web.gwt.app.client.administration.index.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexDetailsPresenter.Display;
import org.obiba.opal.web.gwt.app.client.common.BaseMessagesView;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;

public class IndexDetailsView extends BaseMessagesView implements Display {

  interface ViewUiBinder extends UiBinder<Widget, IndexDetailsView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @Inject
  public IndexDetailsView(EventBus eventBus) {
    super(eventBus);
  }

  @Override
  public void setElement(TableIndexStatusDto dto) {
    dialogBox.setTitle(translations.tableLabel() + " " + dto.getDatasource() + "." + dto.getTable());
    setData(dto.getMessagesArray());
  }

    @Override
    protected Widget createWidget() {
        return uiBinder.createAndBindUi(this);
    }

}
