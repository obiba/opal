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

  public DiffTable() {
    setZebra(false);
  }

  @Override
  public void clear() {
    removeAllRows();
  }

  public void setDiff(String diffEntries) {
    clear();
    fillDiffTable(diffEntries.split("\\n"));
  }


  private void fillDiffTable(String[] diffEntries) {
    int row = 0;
    int aLineNumber = -1;
    int bLineNumber = -1;
    String style = "";
    for(String line : diffEntries) {
      if(line.startsWith("@@")) {
        fillHunkRow(row, line);

        RegExp regExp = RegExp.compile("@@ -(\\d+)(,\\d+)? \\+(\\d+)(,\\d+)? @@");
        MatchResult matcher = regExp.exec(line);
        aLineNumber = Integer.parseInt(matcher.getGroup(1));
        bLineNumber = Integer.parseInt(matcher.getGroup(3));

        style = "diff-hunk";

        row++;
      } else if(!Strings.isNullOrEmpty(line) && !line.startsWith("\\ No newline") && (aLineNumber > 0 || bLineNumber > 0)) {
        Label lineWidget = new Label(line.substring(1));
        Widget lineStartWidget;
        if(line.charAt(0) == '+') {
          setWidget(row, 1, new Label(bLineNumber++ + ""));
          lineStartWidget = new Label("+");
          style = "diff-add";
        } else if(line.charAt(0) == '-') {
          setWidget(row, 0, new Label(aLineNumber++ + ""));
          lineStartWidget = new Label("-");
          style = "diff-rm";
        } else {
          setWidget(row, 0, new Label(aLineNumber++ + ""));
          setWidget(row, 1, new Label(bLineNumber++ + ""));
          lineStartWidget = new HTMLPanel(new SafeHtmlBuilder().appendHtmlConstant("&nbsp;").toSafeHtml());
        }
        for (int i=0; i<2; i++) {
          getFlexCellFormatter().addStyleName(row, i, "diff-line-num");
        }
        getFlexCellFormatter().addStyleName(row, 2, "diff-line-code");

        FlowPanel panel = new FlowPanel();
        panel.add(lineStartWidget);
        panel.add(lineWidget);
        lineStartWidget.addStyleName("right-indent inline-block");
        lineWidget.addStyleName("inline");
        setWidget(row, 2, panel);

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

  private void fillHunkRow(int row, String line) {
    for (int i=0; i<2; i++) {
      setWidget(row, i, new Label("..."));
      getFlexCellFormatter().addStyleName(row, i, "diff-line-num");
    }
    setWidget(row, 2, new Label(line));
  }
}
