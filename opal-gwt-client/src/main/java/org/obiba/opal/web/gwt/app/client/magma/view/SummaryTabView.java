/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.view;

import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.presenter.SummaryTabPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.SummaryTabUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.NumericTextBox;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.math.BinarySummaryDto;
import org.obiba.opal.web.model.client.math.CategoricalSummaryDto;
import org.obiba.opal.web.model.client.math.ContinuousSummaryDto;
import org.obiba.opal.web.model.client.math.DefaultSummaryDto;
import org.obiba.opal.web.model.client.math.FrequencyDto;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;
import org.obiba.opal.web.model.client.math.TextSummaryDto;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 *
 */
public class SummaryTabView extends ViewWithUiHandlers<SummaryTabUiHandlers> implements SummaryTabPresenter.Display {

  private static final int DEFAULT_MAX_TEXT_RESULTS = 20;

  private static final String EMPTY_VALUE = "N/A";

  private static final String NOT_NULL_VALUE = "NOT_NULL";

  @UiTemplate("SummaryTabView.ui.xml")
  interface Binder extends UiBinder<Widget, SummaryTabView> {}

  @UiField
  Alert previewSummary;

  @UiField
  Label previewSummaryText;

  @UiField
  Label previewSummaryTextSuffix;

  @UiField
  NumericTextBox limitTextBox;

  @UiField
  IconAnchor refreshSummaryLink;

  @UiField
  IconAnchor fullSummaryLink;

  @UiField
  IconAnchor cancelSummaryLink;

  @UiField
  Panel summary;

  private final Translations translations;

  private final TranslationMessages translationMessages;

  @Inject
  public SummaryTabView(Binder uiBinder, Translations translations, TranslationMessages translationMessages) {
    this.translations = translations;
    this.translationMessages = translationMessages;
    initWidget(uiBinder.createAndBindUi(this));

    limitTextBox.setMaxConstrained(true);
    limitTextBox.setMaxConstrained(false);
    limitTextBox.setMin(1);
    limitTextBox.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          getUiHandlers().onRefreshSummary();
        }
      }
    });
  }

  @Override
  @SuppressWarnings("IfStatementWithTooManyBranches")
  public void renderSummary(SummaryStatisticsDto dto) {
    summary.clear();

    if(dto.getExtension(ContinuousSummaryDto.SummaryStatisticsDtoExtensions.continuous) != null) {
      renderContinuousSummary(dto);
    } else if(dto.getExtension(DefaultSummaryDto.SummaryStatisticsDtoExtensions.defaultSummary) != null) {
      renderDefaultSummary(dto);
    } else if(dto.getExtension(BinarySummaryDto.SummaryStatisticsDtoExtensions.binarySummary) != null) {
      renderBinarySummary(dto);
    } else if(dto.getExtension(TextSummaryDto.SummaryStatisticsDtoExtensions.textSummary) != null) {
      renderTextSummary(dto);
    } else {
      renderNoSummary();
    }
  }

  @Override
  public void renderSummary(SummaryStatisticsDto dto, VariableDto variableDto) {
    summary.clear();
    if(dto.getExtension(CategoricalSummaryDto.SummaryStatisticsDtoExtensions.categorical) != null) {
      renderCategoricalSummary(dto, variableDto);
    }
  }

  private void renderContinuousSummary(SummaryStatisticsDto dto) {
    ContinuousSummaryDto continuous = dto.getExtension(ContinuousSummaryDto.SummaryStatisticsDtoExtensions.continuous)
        .cast();

    final double[] totals = { 0d, 0d };
    ImmutableListMultimap<Boolean, FrequencyDto> frequenciesByMissing = Multimaps
        .index(JsArrays.toIterable(continuous.getFrequenciesArray()), new Function<FrequencyDto, Boolean>() {
          @Nullable
          @Override
          public Boolean apply(@Nullable FrequencyDto input) {
            // when boolean, is missing is not set
            if(input != null && EMPTY_VALUE.equals(input.getValue())) {
              totals[1] += input.getFreq();
              return true;
            }
            totals[0] += input == null ? 0 : input.getFreq();
            return false;
          }
        });

    summary.add(new ContinuousSummaryView(continuous, frequenciesByMissing.get(false), frequenciesByMissing.get(true),
        totals[0], totals[1]));
  }

  private void renderCategoricalSummary(SummaryStatisticsDto dto, final VariableDto variableDto) {
    CategoricalSummaryDto categorical = dto
        .getExtension(CategoricalSummaryDto.SummaryStatisticsDtoExtensions.categorical).cast();

    final Map<String, CategoryDto> categoriesByName = Maps
        .uniqueIndex(JsArrays.toIterable(variableDto.getCategoriesArray()), new Function<CategoryDto, String>() {
          @Override
          public String apply(CategoryDto input) {
            return input.getName();
          }
        });

    final double[] totals = { 0d, 0d };
    ImmutableListMultimap<Boolean, FrequencyDto> categoriesByMissing = Multimaps
        .index(JsArrays.toIterable(categorical.getFrequenciesArray()), new Function<FrequencyDto, Boolean>() {
          @Nullable
          @Override
          public Boolean apply(@Nullable FrequencyDto input) {
            // when boolean, is missing is not set
            if("boolean".equals(variableDto.getValueType())) {
              if(input != null && EMPTY_VALUE.equals(input.getValue())) {
                totals[1] += input.getFreq();
                return true;
              }
              totals[0] += input == null ? 0 : input.getFreq();
              return false;
            }

            if(input != null && (!categoriesByName.containsKey(input.getValue()) ||
                categoriesByName.get(input.getValue()).getIsMissing())) {
              totals[1] += input.getFreq();
              return true;
            }
            totals[0] += input == null ? 0 : input.getFreq();
            return false;
          }
        });

    summary.add(new CategoricalSummaryView(dto.getResource(), categorical, categoriesByMissing.get(false),
        categoriesByMissing.get(true), totals[0], totals[1]));
  }

  private void renderDefaultSummary(SummaryStatisticsDto dto) {
    DefaultSummaryDto defaultSummaryDto = dto
        .getExtension(DefaultSummaryDto.SummaryStatisticsDtoExtensions.defaultSummary).cast();

    final double[] totals = { 0d, 0d };
    ImmutableListMultimap<Boolean, FrequencyDto> valuesByMissing = Multimaps
        .index(JsArrays.toIterable(defaultSummaryDto.getFrequenciesArray()), new Function<FrequencyDto, Boolean>() {
          @Nullable
          @Override
          public Boolean apply(@Nullable FrequencyDto input) {
            if(input != null && input.getValue().equals(NOT_NULL_VALUE)) {
              input.setValue(translations.notEmpty());
              totals[0] += input.getFreq();
              return false;
            }
            totals[1] += input == null ? 0 : input.getFreq();
            return true;
          }
        });

    summary.add(
        new DefaultSummaryView(defaultSummaryDto, valuesByMissing.get(false), valuesByMissing.get(true), totals[0],
            totals[1]));
  }

  private void renderTextSummary(SummaryStatisticsDto dto) {
    TextSummaryDto textSummaryDto = dto.getExtension(TextSummaryDto.SummaryStatisticsDtoExtensions.textSummary).cast();

    final double[] totals = { 0d, 0d };
    ImmutableListMultimap<Boolean, FrequencyDto> valuesByMissing = Multimaps
        .index(JsArrays.toIterable(textSummaryDto.getFrequenciesArray()), new Function<FrequencyDto, Boolean>() {
          @Nullable
          @Override
          public Boolean apply(@Nullable FrequencyDto input) {
            if(input != null && EMPTY_VALUE.equals(input.getValue())) {
              totals[1] += input.getFreq();
              return true;
            }
            totals[0] += input == null ? 0 : input.getFreq();
            return false;
          }
        });

    summary.add(
        new TextSummaryView(textSummaryDto, valuesByMissing.get(false), valuesByMissing.get(true), totals[0], totals[1],
            DEFAULT_MAX_TEXT_RESULTS));
  }

  private void renderBinarySummary(SummaryStatisticsDto dto) {
    BinarySummaryDto binarySummaryDto = dto.getExtension(BinarySummaryDto.SummaryStatisticsDtoExtensions.binarySummary)
        .cast();
    summary.add(new BinarySummaryView(binarySummaryDto));
  }

  @Override
  public void renderNoSummary() {
    summary.clear();
    summary.add(new Label(translations.noSummaryDataAvailableLabel()));
  }

  @Override
  public void requestingSummary(int limit, int entitiesCount) {
    summary.clear();
    summary.add(new Image("image/loading.gif"));

    limitTextBox.setValue(String.valueOf(limit));
    if(limit < entitiesCount) {
      showLimitBox(entitiesCount);
    } else {
      hideLimitBox();
    }
    previewSummary.setVisible(true);
    cancelSummaryLink.setVisible(true);
  }

  private void showLimitBox(int entitiesCount) {
    limitTextBox.setVisible(true);
    previewSummaryTextSuffix.setVisible(true);
    fullSummaryLink.setVisible(false);
    refreshSummaryLink.setVisible(true);
    previewSummaryText.setText(translations.summaryPreviewPendingLabel());
    previewSummaryTextSuffix.setText(translationMessages.summaryTotalEntitiesLabel(entitiesCount));
  }

  private void hideLimitBox() {
    limitTextBox.setVisible(false);
    previewSummaryTextSuffix.setVisible(false);
    fullSummaryLink.setVisible(false);
    refreshSummaryLink.setVisible(false);
    previewSummaryText.setText(translations.summaryFullPendingLabel());
  }

  @Override
  public void renderSummaryLimit(int limit, int entitiesCount) {
    limitTextBox.setVisible(true);
    limitTextBox.setValue(String.valueOf(limit));
    previewSummaryTextSuffix.setVisible(true);
    previewSummary.setVisible(true);
    cancelSummaryLink.setVisible(false);
    refreshSummaryLink.setVisible(true);
    fullSummaryLink.setVisible(limit < entitiesCount);
    previewSummaryText.setText(translations.summaryOnLabel());
    previewSummaryTextSuffix.setText(translationMessages.summaryTotalEntitiesLabel(entitiesCount));
  }

  @Override
  public void renderCancelSummaryLimit(int limit, int entitiesCount) {
    summary.clear();
    limitTextBox.setValue(String.valueOf(limit));
    limitTextBox.setVisible(true);

    previewSummary.setVisible(true);
    cancelSummaryLink.setVisible(false);
    previewSummaryTextSuffix.setVisible(true);
    fullSummaryLink.setVisible(true);
    refreshSummaryLink.setVisible(true);
    previewSummaryText.setText(translations.summaryFetchSummaryLabel());
    previewSummaryTextSuffix.setText(translationMessages.summaryTotalEntitiesLabel(entitiesCount));
  }

  @UiHandler("fullSummaryLink")
  public void onFullSummary(ClickEvent event) {
    getUiHandlers().onFullSummary();
  }

  @UiHandler("cancelSummaryLink")
  public void onCancelSummary(ClickEvent event) {
    getUiHandlers().onCancelSummary();
  }

  @UiHandler("refreshSummaryLink")
  public void onRefreshSummary(ClickEvent event) {
    getUiHandlers().onRefreshSummary();
  }

  @Override
  public Number getLimit() {
    return limitTextBox.getNumberValue();
  }

  @Override
  public void setLimit(int limit) {
    limitTextBox.setText(String.valueOf(limit));
  }

  @Override
  public void hideSummaryPreview() {
    previewSummary.setVisible(false);
  }
}
