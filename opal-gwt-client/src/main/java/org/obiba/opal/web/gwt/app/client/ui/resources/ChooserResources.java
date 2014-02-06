package org.obiba.opal.web.gwt.app.client.ui.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.watopi.chosen.client.resources.Resources;

public interface ChooserResources extends Resources {

  @ClientBundle.Source("chooser.css")
  @CssResource.NotStrict
  ChooserCss css();
}
