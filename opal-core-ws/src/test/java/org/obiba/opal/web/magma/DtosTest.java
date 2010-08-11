/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.util.Locale;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Variable;
import org.obiba.opal.web.model.Magma.AttributeDto;
import org.obiba.opal.web.model.Magma.CategoryDto;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.ExcelDatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.HibernateDatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.JdbcDatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.JdbcDatasourceSettingsDto;
import org.obiba.opal.web.model.Magma.VariableDto;

import com.google.protobuf.JsonFormat;

public class DtosTest {

  @BeforeClass
  public static void before() {
    new MagmaEngine();
  }

  @AfterClass
  public static void after() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void testFromDto_AttributeConverted() {
    AttributeDto attributesDto = buildAttributeDto();
    checkConversion(attributesDto, Dtos.fromDto(attributesDto));
  }

  @Test
  public void testFromDto_CategoryConverted() {
    CategoryDto categoryDto = buildCategoryDto();
    checkConversion(categoryDto, Dtos.fromDto(categoryDto));

  }

  @Test
  public void testFromDto_VariableConverted() {
    VariableDto variableDto = buildVariableDto();
    checkConversion(variableDto, Dtos.fromDto(variableDto));
  }

  private void checkConversion(VariableDto variableDto, Variable variable) {
    Assert.assertEquals(variable.getName(), variableDto.getName());
    Assert.assertEquals(variable.getEntityType(), variableDto.getEntityType());
    Assert.assertEquals(variable.getValueType().getName(), variableDto.getValueType());
    Assert.assertEquals(variable.getMimeType(), variableDto.getMimeType());
    Assert.assertEquals(variable.isRepeatable(), variableDto.getIsRepeatable());
    Assert.assertEquals(variable.getOccurrenceGroup(), variableDto.getOccurrenceGroup());
    Assert.assertEquals(variable.getUnit(), variableDto.getUnit());
    checkConversion(variableDto.getCategoriesList().get(0), (Category) variable.getCategories().toArray()[0]);
    checkConversion(variableDto.getAttributesList().get(0), variable.getAttributes().get(0));
  }

  private void checkConversion(AttributeDto attributesDto, Attribute attribute) {
    Assert.assertEquals(attribute.getName(), attributesDto.getName());
    Assert.assertEquals(attribute.getLocale().toString(), attributesDto.getLocale());
    Assert.assertEquals(attribute.getValue().getValue(), attributesDto.getValue());
  }

  private void checkConversion(CategoryDto categoryDto, Category category) {
    Assert.assertEquals(category.getName(), categoryDto.getName());
    Assert.assertEquals(category.isMissing(), categoryDto.getIsMissing());
    checkConversion(categoryDto.getAttributesList().get(0), category.getAttributes().get(0));
  }

  private VariableDto buildVariableDto() {
    VariableDto.Builder builder = VariableDto.newBuilder();
    builder.setName("name");
    builder.setEntityType("entityType");
    builder.setValueType("text");
    builder.setMimeType("mimeType");
    builder.setIsRepeatable(true);
    builder.setOccurrenceGroup("occurrenceGroup");
    builder.setUnit("unit");
    builder.addCategories(buildCategoryDto());
    builder.addAttributes(buildAttributeDto());
    VariableDto variableDto = builder.build();
    return variableDto;
  }

  private CategoryDto buildCategoryDto() {
    CategoryDto.Builder builder = CategoryDto.newBuilder();
    builder.setName("name");
    builder.setIsMissing(true);
    builder.addAttributes(buildAttributeDto());
    CategoryDto categoryDto = builder.build();
    return categoryDto;
  }

  private AttributeDto buildAttributeDto() {
    AttributeDto.Builder builder = AttributeDto.newBuilder();
    builder.setName("name");
    builder.setLocale(Locale.ENGLISH.toString());
    builder.setValue("value");
    AttributeDto attributesDto = builder.build();
    return attributesDto;
  }

  @Test
  public void testDatasourceFactoryDtoJsonFormat() {
    String json = JsonFormat.printToString(buildDatasourceFactoryDto());
    System.out.println(json);
  }

  private DatasourceFactoryDto buildDatasourceFactoryDto() {
    DatasourceFactoryDto.Builder builder = DatasourceFactoryDto.newBuilder();

    ExcelDatasourceFactoryDto.Builder excelBuilder = ExcelDatasourceFactoryDto.newBuilder();
    excelBuilder.setFile("/toto/tata.xlsx");
    builder.setExtension(ExcelDatasourceFactoryDto.params, excelBuilder.build());

    JdbcDatasourceFactoryDto.Builder jdbcBuilder = JdbcDatasourceFactoryDto.newBuilder();
    jdbcBuilder.setDriver("com.mysql.jdbc.Driver");
    jdbcBuilder.setUrl("jdbc:mysql://localhost:3306/mart");
    jdbcBuilder.setUsername("root");
    jdbcBuilder.setPassword("rootadmin");
    JdbcDatasourceSettingsDto.Builder settingsBuilder = JdbcDatasourceSettingsDto.newBuilder();
    settingsBuilder.setDefaultEntityType("Participant");
    settingsBuilder.setUseMetadataTables(true);
    jdbcBuilder.setSettings(settingsBuilder.build());
    builder.setExtension(JdbcDatasourceFactoryDto.params, jdbcBuilder.build());

    builder.setExtension(HibernateDatasourceFactoryDto.params, HibernateDatasourceFactoryDto.newBuilder().build());

    return builder.build();
  }

}
