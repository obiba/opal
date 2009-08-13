package org.obiba.opal.sesame.report;

import java.io.InputStream;

import org.obiba.opal.sesame.report.impl.DataEntryFormSelection;
import org.obiba.opal.sesame.report.impl.DataItemNameSelection;
import org.obiba.opal.sesame.report.impl.NameRegexDataItemFilter;

import com.thoughtworks.xstream.XStream;

public class XStreamReportLoader {

  private final XStream xstream;

  public XStreamReportLoader() {
    xstream = new XStream();
    xstream.alias("report", Report.class);
    xstream.alias("variables", DataItemNameSelection.class);
    xstream.alias("def", DataEntryFormSelection.class);
    xstream.alias("nameRegex", NameRegexDataItemFilter.class);
  }

  public Report loadReport(InputStream is) {
    return (Report) xstream.fromXML(is);
  }

}
