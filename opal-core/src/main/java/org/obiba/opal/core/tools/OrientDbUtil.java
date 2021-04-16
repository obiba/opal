/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.obiba.opal.core.service.OrientDbService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.base.Throwables;

public class OrientDbUtil {

  private OrientDbUtil() {
  }

  public static void main(String[] args) {
    if(args.length != 2) {
      throw new IllegalArgumentException("invalid args");
    }

    String command = args[0];
    String file = args[1];

    ApplicationContext context = new ClassPathXmlApplicationContext("spring/opal-core/migrator.xml");
    OrientDbService service = context.getBean(OrientDbService.class);

    switch(command) {
      case "export":
        if(Files.exists(Paths.get(file))) {
          throw new RuntimeException(String.format("File %s already exists", file));
        }

        System.out.println(String.format("Exporting to %s ...", file));

        try {
          service.exportDatabase(new File(file));
        } catch(IOException e) {
          throw Throwables.propagate(e);
        }
        break;
      case "import":
        System.out.println(String.format("Importing from %s ...", file));

        try {
          service.importDatabase(new File(file));
          System.out.println("Export finished successfully.");
        } catch(IOException e) {
          throw Throwables.propagate(e);
        }
        break;
      default:
        throw new IllegalArgumentException("invalid command");
    }

    System.out.println(String.format("%s finished successfully.", command));
  }
}
