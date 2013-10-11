package org.obiba.opal.web.gwt.validation.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.validation.client.AbstractValidationMessageResolver;
import com.google.gwt.validation.client.ProviderValidationMessageResolver;

public class ValidationMessageResolver extends AbstractValidationMessageResolver
    implements ProviderValidationMessageResolver {

  public ValidationMessageResolver() {
    super((ConstantsWithLookup) GWT.create(ValidationMessages.class));
  }
}
