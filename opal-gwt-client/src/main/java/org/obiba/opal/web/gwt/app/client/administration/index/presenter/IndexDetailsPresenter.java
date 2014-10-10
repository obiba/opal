package org.obiba.opal.web.gwt.app.client.administration.index.presenter;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;

/**
 *
 */
public class IndexDetailsPresenter extends ModalPresenterWidget<IndexDetailsPresenter.Display> {

    public interface Display extends PopupView, HasUiHandlers<ModalUiHandlers> {
        void setElement(TableIndexStatusDto dto);
    }

    @Inject
    public IndexDetailsPresenter(Display display, EventBus eventBus) {
        super(eventBus, display);
        getView().setUiHandlers(this);
    }

    public void setElement(TableIndexStatusDto dto) {
        getView().setElement(dto);
    }

}
