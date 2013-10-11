package org.obiba.opal.web.gwt.validation.client;

/**
 * Interface to represent the constants contained in resource bundle:
 * 'validation/ValidationMessages.properties'.
 */
public interface ValidationMessages extends org.hibernate.validator.ValidationMessages {

  @DefaultStringValue("must be unique")
  @Key("org.obiba.opal.core.validator.Unique.message")
  String org_obiba_opal_core_validator_Unique_message();

}
