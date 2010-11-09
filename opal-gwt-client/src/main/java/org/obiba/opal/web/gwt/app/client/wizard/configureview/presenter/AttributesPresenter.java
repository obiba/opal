/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;

import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.inject.Inject;

/**
 *
 */
public class AttributesPresenter extends LocalizablesPresenter {
  //
  // Instance Variables
  //

  private VariableDto variableDto;

  //
  // Constructors
  //

  @Inject
  public AttributesPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // LocalizablesPresenter Methods
  //

  @Override
  protected List<Localizable> getLocalizables(String localeName) {
    List<Localizable> localizables = new ArrayList<Localizable>();

    for(int i = 0; i < variableDto.getAttributesArray().length(); i++) {
      final AttributeDto attributeDto = variableDto.getAttributesArray().get(i);

      if(attributeDto.getLocale().equals(localeName)) {
        localizables.add(new Localizable() {

          @Override
          public String getName() {
            return attributeDto.getName();
          }

          @Override
          public String getLabel() {
            return attributeDto.getValue();
          }
        });
      }
    }

    return localizables;
  }

  @Override
  protected void editLocalizable(Localizable localizable, String localeName) {
    // TODO: Show the "edit attribute" dialog.
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void deleteLocalizable(Localizable localizable, String localeName) {
    JsArray<AttributeDto> newAttributesArray = (JsArray<AttributeDto>) JsArray.createArray();

    for(int i = 0; i < variableDto.getAttributesArray().length(); i++) {
      AttributeDto attributeDto = variableDto.getAttributesArray().get(i);

      if(!attributeDto.getName().equals(localizable.getName()) || !attributeDto.getLocale().equals(localeName)) {
        newAttributesArray.push(attributeDto);
      }
    }

    variableDto.clearAttributesArray();
    variableDto.setAttributesArray(newAttributesArray);
  }

  //
  // Methods
  //

  public void setVariableDto(VariableDto variableDto) {
    this.variableDto = variableDto;
  }
}
