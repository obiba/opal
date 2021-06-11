/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.dashboard;

import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.PageHeader;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.NewsDto;

public class DashboardView extends Composite implements DashboardPresenter.Display {

  interface Binder extends UiBinder<Widget, DashboardView> {
  }

  @UiField
  PageHeader pageTitle;

  @UiField
  IconAnchor exploreProjectsLink;

  @UiField
  IconAnchor searchLink;

  @UiField
  IconAnchor exploreFilesLink;

  @UiField
  IconAnchor datashieldLink;

  @UiField
  IconAnchor tasksLink;

  @UiField
  IconAnchor reportsLink;

  @UiField
  IconAnchor identifiersLink;

  @UiField
  IconAnchor myprofileLink;

  @UiField
  Panel identifiers;

  @UiField
  Panel datashield;

  @UiField
  Panel tasks;

  @UiField
  Panel reports;

  @UiField
  Panel bookmarks;

  @UiField
  Panel newsGroup;

  @UiField
  Panel news;

  private final Translations translations;

  @Inject
  public DashboardView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;

    exploreProjectsLink.setHref("#" + Places.PROJECTS);
    searchLink.setHref("#" + Places.SEARCH);
    identifiersLink.setHref("#" + Places.ADMINISTRATION + "/" + Places.IDENTIFIERS);
    reportsLink.setHref("#" + Places.ADMINISTRATION + "/" + Places.REPORT_TEMPLATES);
    exploreFilesLink.setHref("#" + Places.ADMINISTRATION + "/" + Places.FILES);
    datashieldLink.setHref("#" + Places.ADMINISTRATION + "/" + Places.DATASHIELD);
    tasksLink.setHref("#" + Places.ADMINISTRATION + "/" + Places.TASKS);
    myprofileLink.setHref("#" + Places.PROFILE);
    pageTitle.setText(translations.pageDashboardTitle());

    exploreProjectsLink.setText(translations.exploreVariables());
    searchLink.setText(translations.pageSearchTitle());
    exploreFilesLink.setText(translations.manageFiles());
    datashieldLink.setText("DataSHIELD");
    tasksLink.setText(translations.tasksLabel());
    reportsLink.setText(translations.runReports());
    identifiersLink.setText(translations.manageParticipantIdentifiers());
    myprofileLink.setText(translations.myProfile());
  }

  @Override
  public void showNews(NewsDto notes) {
    newsGroup.setVisible(notes != null && notes.getNotesCount() > 0);
    if (!newsGroup.isVisible()) return;

    news.clear();
    int i = 0;
    for (NewsDto.NoteDto note : JsArrays.toIterable(notes.getNotesArray())) {
      if (i<6) {
        FlowPanel panel = new FlowPanel();
        Heading heading = new Heading(6, note.getTitle());
        heading.addStyleName("no-bottom-margin");
        panel.add(heading);
        String summary = note.getDate() + (note.hasSummary() ? " - " + note.getSummary() : "");
        Label label = new Label(summary);
        label.addStyleName("help-block no-bottom-margin xsmall-right-indent inline");
        panel.add(label);
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendHtmlConstant("<a href='" + note.getLink() + "' target='_blank'>" + translations.moreLabel() + "</a>");
        panel.add(new Anchor(builder.toSafeHtml()));
        news.add(panel);
        i++;
      }
    }
  }

  @Override
  public void addToSlot(Object slot, IsWidget content) {
  }

  @Override
  public void removeFromSlot(Object slot, IsWidget content) {
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if (slot == DashboardPresenter.BOOKMARKS) {
      bookmarks.clear();
      bookmarks.add(content);
    }
  }

  @Override
  public HasAuthorization getDataShieldAuthorizer() {
    return new WidgetAuthorizer(datashield);
  }

  @Override
  public HasAuthorization getIdentifiersAuthorizer() {
    return new WidgetAuthorizer(identifiers);
  }

  @Override
  public HasAuthorization getReportsAuthorizer() {
    return new WidgetAuthorizer(reports);
  }

  @Override
  public HasAuthorization getTasksAuthorizer() {
    return new WidgetAuthorizer(tasks);
  }

}
