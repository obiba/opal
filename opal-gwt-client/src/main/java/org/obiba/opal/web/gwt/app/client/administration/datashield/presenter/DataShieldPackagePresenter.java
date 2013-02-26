/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.datashield.presenter;

import java.util.HashMap;
import java.util.Map;

import org.obiba.opal.web.model.client.opal.r.EntryDto;
import org.obiba.opal.web.model.client.opal.r.RPackageDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class DataShieldPackagePresenter extends PresenterWidget<DataShieldPackagePresenter.Display> {

  @Inject
  public DataShieldPackagePresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    registerHandler(getView().getCloseButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));
  }

  public void displayPackage(RPackageDto dto) {
    Map<String, EntryDto> entriesMap = descriptionArrayToMap(dto);

//    getView().setName(dto.getName());
    getView().setPackageName(entriesMap.get("package").getValue());
    getView().setVersion(entriesMap.get("version").getValue());
    getView().setTitle(entriesMap.get("title").getValue());
    getView().setAuthor(entriesMap.get("author").getValue());
    getView().setMaintainer(entriesMap.get("maintainer").getValue());
    getView().setDepends(entriesMap.get("depends").getValue());
    getView().setDescription(entriesMap.get("description").getValue());
    getView().setLicense(entriesMap.get("license").getValue());
    getView().setOpalVersion(entriesMap.get("opalVersion").getValue());
    getView().setUrl("http://www.obiba.org", "http://www.obiba.org");
    getView().setBugReports("http://jira.obiba.org", "http://jira.obiba.org");
//    getView().setUrl(entriesMap.get("url").getValue(), entriesMap.get("url").getValue());
//    getView().setBugReports(entriesMap.get("bugReports").getValue(), entriesMap.get("bugReports").getValue());

  }

  private Map<String, EntryDto> descriptionArrayToMap(RPackageDto dto) {
    Map<String, EntryDto> entriesMap = new HashMap<String, EntryDto>();
    JsArray<EntryDto> entries = dto.getDescriptionArray();

    for(int i = 0; i < entries.length(); i++) {
      entriesMap.put(entries.get(i).getKey().toLowerCase(), entries.get(i));
    }

    return entriesMap;
  }

  //
  // Inner classes and interfaces
  //

  public interface Display extends PopupView {

    void hideDialog();

    HasClickHandlers getCloseButton();

//    void setName(String name);

//    HasText getName();

    void setPackageName(String s);

    void setVersion(String s);

    void setTitle(String s);

    void setAuthor(String s);

    void setMaintainer(String s);

    void setDepends(String s);

    void setDescription(String s);

    void setLicense(String s);

    void setOpalVersion(String s);

    void setUrl(String s, String href);

    void setBugReports(String s, String href);
  }

}
