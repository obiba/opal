/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.news;

import org.obiba.opal.core.support.yaml.AbstractYaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ObibaNewsYaml extends AbstractYaml<ObibaNews> {

  public ObibaNewsYaml() {
    super(ObibaNews.class);
  }

  public static ObibaNews loadNews() throws IOException {
    try (InputStream input = new URL("https://raw.githubusercontent.com/obiba/obiba.github.io/master/_data/news.yml").openStream()) {
      ObibaNewsYaml yaml = new ObibaNewsYaml();
      return yaml.load(input);
    }
  }
}
