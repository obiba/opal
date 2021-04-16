/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.google.common.base.Strings;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class DiffTable extends DefaultFlexTable {

  private int row = 0;

  private int aLineNumber = -1;

  private int bLineNumber = -1;

  private String style = "";

  public DiffTable() {
    setZebra(false);
  }

  @Override
  public void clear() {
    removeAllRows();
    row = 0;
    aLineNumber = -1;
    bLineNumber = -1;
    style = "";
  }

  public void setDiff(String diffEntries) {
    clear();
    fillDiffTable(diffEntries.split("\\n"));
  }

  private void fillDiffTable(String... diffEntries) {
    for(String line : diffEntries) {
      if(line.startsWith("@@")) {
        parseHunkRow(line);
        row++;
      } else if(!Strings.isNullOrEmpty(line) && !line.startsWith("\\ No newline") && (aLineNumber > 0 || bLineNumber > 0)) {
        parseDiffRows(line);
        row++;
      }

      if (!Strings.isNullOrEmpty(style)) {
        for (int i=0; i<3; i++) {
          getFlexCellFormatter().addStyleName(row - 1, i, style);
        }
        style = "";
      }
    }
  }

  private void parseDiffRows(String line) {Label lineWidget = new Label(line.substring(1));
    Widget lineStartWidget;

    if(line.charAt(0) == '+') {
      setWidget(row, 1, new Label(Integer.toString(bLineNumber++)));
      lineStartWidget = new Label("+");
      style = "diff-add";
    } else if(line.charAt(0) == '-') {
      setWidget(row, 0, new Label(Integer.toString(aLineNumber++)));
      lineStartWidget = new Label("-");
      style = "diff-rm";
    } else {
      setWidget(row, 0, new Label(Integer.toString(aLineNumber++)));
      setWidget(row, 1, new Label(Integer.toString(bLineNumber++)));
      lineStartWidget = new HTMLPanel(new SafeHtmlBuilder().appendHtmlConstant("&nbsp;").toSafeHtml());
    }

    for (int i=0; i<2; i++) {
      getFlexCellFormatter().addStyleName(row, i, "diff-line-num");
    }

    getFlexCellFormatter().addStyleName(row, 2, "diff-line-code");
    createDiffRowPanel(lineWidget, lineStartWidget);
  }

  private void createDiffRowPanel(Label lineWidget, Widget lineStartWidget) {FlowPanel panel = new FlowPanel();
    panel.add(lineStartWidget);
    panel.add(lineWidget);
    lineStartWidget.addStyleName("right-indent inline-block");
    lineWidget.addStyleName("inline");
    setWidget(row, 2, panel);
  }

  private void parseHunkRow(String line) {
    fillHunkRow(line);

    RegExp regExp = RegExp.compile("@@ -(\\d+)(,\\d+)? \\+(\\d+)(,\\d+)? @@");
    MatchResult matcher = regExp.exec(line);
    aLineNumber = Integer.parseInt(matcher.getGroup(1));
    bLineNumber = Integer.parseInt(matcher.getGroup(3));
    style = "diff-hunk";
  }

  private void fillHunkRow(String line) {
    for (int i=0; i<2; i++) {
      setWidget(row, i, new Label("..."));
      getFlexCellFormatter().addStyleName(row, i, "diff-line-num");
    }
    setWidget(row, 2, new Label(line));
  }
}
