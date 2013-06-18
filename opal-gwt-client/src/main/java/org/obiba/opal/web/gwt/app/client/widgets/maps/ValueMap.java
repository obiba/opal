package org.obiba.opal.web.gwt.app.client.widgets.maps;

import org.obiba.opal.web.model.client.magma.ValueSetsDto;

public abstract class ValueMap extends BaseMap {

  protected ValueSetsDto.ValueDto value;

  public ValueMap(ValueSetsDto.ValueDto value) {
    this.value = value;
    initialize();
  }

  public ValueMap(String width, String height, ValueSetsDto.ValueDto value) {
    super(width, height);
    this.value = value;
    initialize();
  }

  private void initialize() {
    if (value != null) {
      if (value.getValuesArray() == null || value.getValuesArray().length() == 0) {
        initializeValue();
      } else {
        initializeValueSequence();
      }
    }
    initWidget(contentPanel);
  }

  /**
   * Display the value on the map.
   */
  public abstract void initializeValue();

  /**
   * Display the value sequence on the map.
   */
  public abstract void initializeValueSequence();
}
