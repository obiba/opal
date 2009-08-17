package org.obiba.opal.jdbcmart.batch;

import java.util.List;

import org.obiba.opal.sesame.report.Report;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;

public class DataItemSetFactoryBean implements FactoryBean {

  private Resource reportDescriptor;

  private XStream xstream;

  private List<Report> reports;

  public void setReportDescriptor(Resource reportDescriptor) {
    this.reportDescriptor = reportDescriptor;
  }

  public void setXstream(XStream xstream) {
    this.xstream = xstream;
  }

  public Object getObject() throws Exception {
    if(reports == null) {
      reports = (List<Report>) xstream.fromXML(reportDescriptor.getInputStream());
    }
    return reports;
  }

  public Class getObjectType() {
    return List.class;
  }

  public boolean isSingleton() {
    return true;
  }

}
