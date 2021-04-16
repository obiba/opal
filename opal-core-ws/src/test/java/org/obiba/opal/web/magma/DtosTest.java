/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import java.util.Locale;

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
import org.obiba.opal.web.model.Magma.VariableDto;

import com.googlecode.protobuf.format.JsonFormat;

import static org.fest.assertions.api.Assertions.assertThat;

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
    assertThat(variable.getName()).isEqualTo(variableDto.getName());
    assertThat(variable.getEntityType()).isEqualTo(variableDto.getEntityType());
    assertThat(variable.getValueType().getName()).isEqualTo(variableDto.getValueType());
    assertThat(variable.getMimeType()).isEqualTo(variableDto.getMimeType());
    assertThat(variable.isRepeatable()).isEqualTo(variableDto.getIsRepeatable());
    assertThat(variable.getOccurrenceGroup()).isEqualTo(variableDto.getOccurrenceGroup());
    assertThat(variable.getUnit()).isEqualTo(variableDto.getUnit());
    checkConversion(variableDto.getCategoriesList().get(0), (Category) variable.getCategories().toArray()[0]);
    checkConversion(variableDto.getAttributesList().get(0), variable.getAttributes().get(0));
  }

  private void checkConversion(AttributeDto attributesDto, Attribute attribute) {
    assertThat(attribute.getName()).isEqualTo(attributesDto.getName());
    assertThat(attribute.getLocale().toString()).isEqualTo(attributesDto.getLocale());
    assertThat(attribute.getValue().getValue()).isEqualTo(attributesDto.getValue());
  }

  private void checkConversion(CategoryDto categoryDto, Category category) {
    assertThat(category.getName()).isEqualTo(categoryDto.getName());
    assertThat(category.isMissing()).isEqualTo(categoryDto.getIsMissing());
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
    return builder.build();
  }

  private CategoryDto buildCategoryDto() {
    CategoryDto.Builder builder = CategoryDto.newBuilder();
    builder.setName("name");
    builder.setIsMissing(true);
    builder.addAttributes(buildAttributeDto());
    return builder.build();
  }

  private AttributeDto buildAttributeDto() {
    AttributeDto.Builder builder = AttributeDto.newBuilder();
    builder.setName("name");
    builder.setLocale(Locale.ENGLISH.toString());
    builder.setValue("value");
    return builder.build();
  }

  @Test
  public void testDatasourceFactoryDtoJsonFormat() {
    String json = JsonFormat.printToString(buildDatasourceFactoryDto());
    // non regression check for protobuf json format patch about nested extensions
    assertThat(
        "{\"Magma.ExcelDatasourceFactoryDto.params\": {\"file\": \"/toto/tata.xlsx\"},\"Magma.JdbcDatasourceFactoryDto.params\": {\"database\": \"MyDatabase\"},\"Magma.HibernateDatasourceFactoryDto.params\": {}}")
        .isEqualTo(json);
  }

  private DatasourceFactoryDto buildDatasourceFactoryDto() {
    DatasourceFactoryDto.Builder builder = DatasourceFactoryDto.newBuilder();

    ExcelDatasourceFactoryDto.Builder excelBuilder = ExcelDatasourceFactoryDto.newBuilder();
    excelBuilder.setFile("/toto/tata.xlsx");
    builder.setExtension(ExcelDatasourceFactoryDto.params, excelBuilder.build());

    JdbcDatasourceFactoryDto.Builder jdbcBuilder = JdbcDatasourceFactoryDto.newBuilder();
    jdbcBuilder.setDatabase("MyDatabase");
    builder.setExtension(JdbcDatasourceFactoryDto.params, jdbcBuilder.build());

    builder.setExtension(HibernateDatasourceFactoryDto.params, HibernateDatasourceFactoryDto.newBuilder().build());

    return builder.build();
  }

}
