package org.obiba.opal.web.gwt.app.client.administration.taxonomies.view;

import java.util.HashMap;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.VocabularyEditUiHandlers;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.LocalizedEditableText;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import com.allen_sauer.gwt.dnd.client.DragController;
import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandlerAdapter;
import com.allen_sauer.gwt.dnd.client.DragStartEvent;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.allen_sauer.gwt.dnd.client.drop.FlowPanelDropController;
import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import static org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.VocabularyEditPresenter.Display;
import static org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.VocabularyEditPresenter.TermArrayUtils;

public class VocabularyEditView extends ViewWithUiHandlers<VocabularyEditUiHandlers> implements Display {

  interface ViewUiBinder extends UiBinder<Widget, VocabularyEditView> {}

  private final Map<String, LocalizedEditableText> vocabularyTitleTexts = new HashMap<String, LocalizedEditableText>();

  private final Map<String, LocalizedEditableText> vocabularyDescriptionTexts
      = new HashMap<String, LocalizedEditableText>();

  // Map of term names with their map of localized texts
  private final Map<String, Map<String, LocalizedEditableText>> termsDescriptionTexts
      = new HashMap<String, Map<String, LocalizedEditableText>>();

  private final Map<String, Map<String, LocalizedEditableText>> termsTitleTexts
      = new HashMap<String, Map<String, LocalizedEditableText>>();

  @UiField
  Breadcrumbs breadcrumbs;

  @UiField
  Button saveVocabulary;

  @UiField
  Button cancelVocabulary;

  @UiField
  Heading vocabularyName;

  @UiField
  TextBox name;

  @UiField
  Chooser taxonomies;

  @UiField
  FlowPanel vocabularyTitles;

  @UiField
  FlowPanel vocabularyDescriptions;

  @UiField
  CheckBox repeatable;

  @UiField
  FlowPanel termPanel;

  @UiField
  TextBox newTermName;

  @UiField
  Button addChild;

  @UiField
  Button addSibling;

  @UiField
  FlowPanel termsEditionPanel;

  @UiField
  Heading termTitle;

  @UiField
  FlowPanel termsPanel;

  @UiField
  TextBox termName;

  @UiField
  FlowPanel termTitles;

  @UiField
  FlowPanel termDescriptions;

  @Inject
  public VocabularyEditView(ViewUiBinder viewUiBinder) {
    initWidget(viewUiBinder.createAndBindUi(this));
    newTermName.setPlaceholder("New term name...");
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @Override
  public TakesValue<JsArray<LocaleTextDto>> getTitles(JsArrayString localeTitles) {
    return new LocaleTextDtoTakesValue(vocabularyTitles, localeTitles) {
      @Override
      public Map<String, LocalizedEditableText> getLocalizedEditableTextMap() {
        return vocabularyTitleTexts;
      }
    };
  }

  @Override
  public HasText getVocabularyName() {
    return name;
  }

  @Override
  public TakesValue<JsArray<LocaleTextDto>> getDescriptions(JsArrayString localeDescriptions) {
    return new LocaleTextDtoTakesValue(vocabularyDescriptions, localeDescriptions) {
      @Override
      public Map<String, LocalizedEditableText> getLocalizedEditableTextMap() {
        return vocabularyDescriptionTexts;
      }
    };
  }

  @Override
  public HasValue getRepeatable() {
    return repeatable;
  }

  @Override
  public void setTaxonomies(JsArray<TaxonomyDto> taxonomies) {
    this.taxonomies.clear();
    for(int i = 0; i < taxonomies.length(); i++) {
      String t = taxonomies.get(i).getName();
      this.taxonomies.addItem(t);
    }
  }

  @Override
  public void setSelectedTaxonomy(String taxonomyName) {
    taxonomies.setSelectedValue(taxonomyName);
  }

  @Override
  public String getSelectedTaxonomy() {
    return taxonomies.getSelectedValue();
  }

  @Override
  public HasText getTermName() {
    return termName;
  }

  @Override
  public TakesValue<JsArray<LocaleTextDto>> getTermTitles(final String title, JsArrayString localeTitles) {
    return new LocaleTextDtoTakesValue(termTitles, localeTitles) {
      @Override
      public Map<String, LocalizedEditableText> getLocalizedEditableTextMap() {
        if(!termsTitleTexts.containsKey(title)) {
          termsTitleTexts.put(title, new HashMap<String, LocalizedEditableText>());
        }

        return termsTitleTexts.get(title);
      }
    };
  }

  @Override
  public TakesValue<JsArray<LocaleTextDto>> getTermDescriptions(final String title, JsArrayString localeDescriptions) {
    return new LocaleTextDtoTakesValue(termDescriptions, localeDescriptions) {
      @Override
      public Map<String, LocalizedEditableText> getLocalizedEditableTextMap() {
        if(!termsDescriptionTexts.containsKey(title)) {
          termsDescriptionTexts.put(title, new HashMap<String, LocalizedEditableText>());
        }

        return termsDescriptionTexts.get(title);
      }
    };
  }

  @Override
  public Panel getTermPanel() {
    return termPanel;
  }

  @UiHandler("saveVocabulary")
  void onSave(ClickEvent event) {
    getUiHandlers().onSave();
  }

  @UiHandler("cancelVocabulary")
  void onCancel(ClickEvent event) {
    getUiHandlers().onCancel();
  }

  @UiHandler("addChild")
  void onAddChild(ClickEvent event) {
    getUiHandlers().onAddChild(newTermName.getText());
  }

  @UiHandler("addSibling")
  void onAddSibling(ClickEvent event) {
    getUiHandlers().onAddSibling(newTermName.getText());
  }

  @Override
  public void displayVocabulary(VocabularyDto vocabularyDto) {
    vocabularyName.setText(name.getText());
    displayTerms(vocabularyDto);
  }

  private void displayTerms(VocabularyDto vocabulary) {
    termsPanel.clear();
    termsPanel.add(addTermsLinks(vocabulary, vocabulary.getTermsArray(), 0));
  }

  private Widget addTermsLinks(VocabularyDto vocabulary, JsArray<TermDto> terms, int level) {

    FlowPanel target = new FlowPanel();
    PickupDragController dragController = new PickupDragController(RootPanel.get(), false);

    int nb = terms.length();
    if(nb > 0) {

      DropController flowPanelDropController = new FlowPanelDropController(target);
      dragController.addDragHandler(new TermDragHandlerAdapter(target, vocabulary));
      dragController.registerDropController(flowPanelDropController);

      for(int i = 0; i < nb; i++) {
        FocusPanel focusPanel = getTermFocusPanel(vocabulary, terms.get(i), dragController, level);
        target.add(focusPanel);
        if(terms.get(i).getTermsCount() > 0) {
          target.add(addTermsLinks(vocabulary, terms.get(i).getTermsArray(), level + 1));
        }
      }
    }

    return target;
  }

  @Override
  public void clearTermName() {
    newTermName.setText("");
  }

  private FocusPanel getTermFocusPanel(final VocabularyDto vocabulary, final TermDto term,
      DragController dragController, int level) {
    FocusPanel focusPanel = new FocusPanel();
    focusPanel.setTitle(term.getName());
    FlowPanel p = new FlowPanel();

    NavLink link = new NavLink(term.getName());
    link.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        getUiHandlers().onTermSelection(term);
      }
    });
    link.addStyleName("inline");

    SafeHtml indent = SafeHtmlUtils.fromString("");
    for(int i = 0; i < level; i++) {
      indent = SafeHtmlUtils.fromTrustedString(indent.asString() + "&nbsp;&nbsp;&nbsp;&nbsp;");
    }
    InlineHTML spacer = new InlineHTML(indent);
    spacer.addStyleName("inline-block");

    FocusPanel pMove = new FocusPanel();

    Icon move = new Icon();
    move.setIcon(IconType.MOVE);
    pMove.add(move);
    pMove.addStyleName("inline-block");

    p.setTitle(term.getName());
    p.add(spacer);
    p.add(pMove);
    p.add(link);

    IconAnchor delete = new IconAnchor();
    delete.setIcon(IconType.REMOVE);
    delete.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {

        // Modify vocabularyDto with the new structure
        TermDto parent = TermArrayUtils.findParent(null, vocabulary.getTermsArray(), term);
        if(parent != null) {
          JsArray<TermDto> termsArray = JsArrays.create().cast();
          for(int i = 0; i < parent.getTermsCount(); i++) {
            if(!parent.getTerms(i).getName().equals(term.getName())) {
              termsArray.push(parent.getTerms(i));
            }
          }
          parent.setTermsArray(termsArray);
        } else {
          JsArray<TermDto> termsArray = JsArrays.create().cast();
          for(int i = 0; i < vocabulary.getTermsCount(); i++) {
            if(!vocabulary.getTerms(i).getName().equals(term.getName())) {
              termsArray.push(vocabulary.getTerms(i));
            }
          }

          vocabulary.setTermsArray(termsArray);
        }
        displayTerms(vocabulary);
      }
    });

    p.add(delete);
    dragController.makeDraggable(focusPanel, pMove);

    focusPanel.add(p);

    return focusPanel;
  }

  private abstract class LocaleTextDtoTakesValue implements TakesValue<JsArray<LocaleTextDto>> {

    FlowPanel target;

    JsArrayString locales;

    LocaleTextDtoTakesValue(FlowPanel target, JsArrayString locales) {
      this.target = target;
      this.locales = locales;
    }

    public abstract Map<String, LocalizedEditableText> getLocalizedEditableTextMap();

    @Override
    public void setValue(JsArray<LocaleTextDto> value) {
      // Add all TexDto to vocabularyTitles
      target.clear();
      int size = value.length();
      int nbLocales = locales.length();
      for(int i = 0; i < nbLocales; i++) {
        // Find the right textDto corresponding with the locale
        boolean found = false;
        for(int j = 0; j < size; j++) {
          if(locales.get(i).equals(value.get(j).getLocale())) {
            LocalizedEditableText textValueInput = getTextValueInput(value.get(j).getLocale(), value.get(j).getText());
            getLocalizedEditableTextMap().put(value.get(j).getLocale(), textValueInput);

            target.add(textValueInput);
            found = true;
            break;
          }
        }

        if(!found) {
          LocalizedEditableText textValueInput = getTextValueInput(locales.get(i), "");
          getLocalizedEditableTextMap().put(locales.get(i), textValueInput);

          target.add(textValueInput);
        }
      }
    }

    @Override
    public JsArray<LocaleTextDto> getValue() {
      JsArray<LocaleTextDto> texts = JsArrays.create();
      for(String locale : getLocalizedEditableTextMap().keySet()) {
        LocaleTextDto localeText = LocaleTextDto.create();
        localeText.setText(getLocalizedEditableTextMap().get(locale).getTextBox().getText());
        localeText.setLocale(locale);

        texts.push(localeText);
      }

      return texts;
    }

    private LocalizedEditableText getTextValueInput(String locale, String text) {
      LocalizedEditableText localizedText = new LocalizedEditableText();
      localizedText.setValue(new LocalizedEditableText.LocalizedText(locale, text));

      return localizedText;
    }
  }

  private class TermDragHandlerAdapter extends DragHandlerAdapter {
    private final FlowPanel target;

    private final VocabularyDto vocabulary;

    int original;

    boolean insertAfter = true;

    private TermDragHandlerAdapter(FlowPanel target, VocabularyDto vocabulary) {
      this.target = target;
      this.vocabulary = vocabulary;
      original = 0;
    }

    @Override
    public void onDragStart(DragStartEvent event) {
      super.onDragStart(event);
      original = target.getWidgetIndex((Widget) event.getSource());
    }

    @Override
    public void onDragEnd(DragEndEvent event) {
      if(event.getContext().finalDropController != null) {
        int pos = target.getWidgetIndex((Widget) event.getSource());
        String title = target.getWidget(pos).getTitle();

        if(pos <= original) {
          insertAfter = false;
        }

        getUiHandlers().onReorderTerms(title, pos, insertAfter);

      }
    }
  }
}
