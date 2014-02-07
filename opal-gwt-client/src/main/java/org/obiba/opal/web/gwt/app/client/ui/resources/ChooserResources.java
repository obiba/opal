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
