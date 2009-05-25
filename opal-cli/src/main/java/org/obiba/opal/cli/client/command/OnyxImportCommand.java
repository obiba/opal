/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client.command;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.obiba.opal.cli.client.command.options.OnyxImportCommandOptions;
import org.obiba.opal.core.service.OnyxImportService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * DCC onyx-import command.
 */
public class OnyxImportCommand extends AbstractCommand<OnyxImportCommandOptions> {
  //
  // Instance Variables
  //

  private OnyxImportService onyxImportService;

  //
  // AbstractCommand Methods
  //

  public void execute() {
    // Ensure that options have been set.
    if(options == null) {
      throw new IllegalStateException("Options not set (setOptions must be called before calling execute)");
    }

    // If user name and password have now been provided, go ahead and import the data.
    if(options.isUsername() && options.isPassword()) {
      // Tell Carol not to initialize its CMI component. This helps us minimize dependencies brought in by JOTM.
      // See: http://wiki.obiba.org/confluence/display/CAG/Technical+Requirements for details.
      System.setProperty("cmi.disabled", "true");

      ApplicationContext context = null;
      try {
        // First, lazily initialize the onyxImportService variable (fetch it from the Spring ApplicationContext).
        context = loadContext();
        setOnyxImportService((OnyxImportService) context.getBean("onyxImportService"));

        // Safely resolve optional option/arg values.
        String date = options.isDate() ? options.getDate() : null;
        String site = options.isSite() ? options.getSite() : null;
        String tags = options.isTags() ? options.getTags() : null;
        List<File> files = options.isFiles() ? options.getFiles() : null;

        // Call the appropriate service method.
        if(files != null) {
          onyxImportService.importData(options.getUsername(), options.getPassword(), csvToList(tags), files.get(0));
        } else if(options.isDate() || options.isSite()) {
          onyxImportService.importData(options.getUsername(), options.getPassword(), stringToDate(date), site, csvToList(tags));
        } else {
          onyxImportService.importData(options.getUsername(), options.getPassword());
        }
      } finally {
        // JOTM will shutdown when the Spring Application shutdown event occurs. Since we're running as a
        // Stand alone command line application we will now send that shutdown event by calling close()
        // manually on the application context.
        ((ConfigurableApplicationContext) context).close();
      }
    }
  }

  //
  // Methods
  //

  public void setOnyxImportService(OnyxImportService onyxImportService) {
    this.onyxImportService = onyxImportService;
  }

  private ApplicationContext loadContext() {
    return new ClassPathXmlApplicationContext("spring/opal-cli/context.xml");
  }

  /**
   * Converts a <code>String</code> containing comma-separated values to a <code>List</code> of those values.
   * 
   * @param csv <code>String</code> of comma-separated values
   * @return <code>List</code> of values (or <code>null</code> if <code>csv</code> is <code>null</code> or
   * empty)
   */
  private List<String> csvToList(String csv) {
    List<String> valueList = null;

    if(csv != null) {
      valueList = Arrays.asList(csv.split(","));
      if(valueList.isEmpty()) {
        valueList = null;
      }
    }

    return valueList;
  }

  private Date stringToDate(String dateString) {
    Date date = null;

    if(dateString != null) {
      try {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        date = dateFormat.parse(dateString);
      } catch(ParseException ex) {
        throw new IllegalArgumentException("Invalid date format (expected yyyy-MM-dd)");
      }
    }

    return date;
  }
}
