/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui.resources;

import com.google.gwt.resources.client.DataResource;
import com.watopi.chosen.client.resources.ChozenCss;
import com.watopi.chosen.client.resources.Resources;

public interface ChooserResources extends Resources {

  @Source("chooser.css")
  ChozenCss css();

  @Source("chosen-sprite.png")
  DataResource chosenSprite();
}
