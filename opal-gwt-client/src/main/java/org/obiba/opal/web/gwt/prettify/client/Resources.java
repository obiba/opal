package org.obiba.opal.web.gwt.prettify.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.TextResource;

interface Resources extends ClientBundle {

  static final String LibPath = "lib/";

  static final String LangBase = LibPath + "lang-";

  public final Resources INSTANCE = new Instance();

  @Source(LibPath + "prettify.js")
  TextResource script();

  @CssResource.NotStrict
  @Source(LibPath + "prettify.css")
  CssResource style();

  Languages langs();

  interface Languages extends ClientBundle {

    @Source(LangBase + "apollo.js")
    TextResource apollo();

    @Source(LangBase + "css.js")
    TextResource css();

    @Source(LangBase + "hs.js")
    TextResource hs();

    @Source(LangBase + "lisp.js")
    TextResource lisp();

    @Source(LangBase + "lua.js")
    TextResource lua();

    @Source(LangBase + "ml.js")
    TextResource ml();

    @Source(LangBase + "proto.js")
    TextResource proto();

    @Source(LangBase + "scala.js")
    TextResource scala();

    @Source(LangBase + "sql.js")
    TextResource sql();

    @Source(LangBase + "vb.js")
    TextResource vb();

    @Source(LangBase + "vhdl.js")
    TextResource vhdl();

    @Source(LangBase + "wiki.js")
    TextResource wiki();

    @Source(LangBase + "yaml.js")
    TextResource yaml();

  }

  static class Instance implements Resources {

    private static final Resources RESOURCES = GWT.create(Resources.class);

    private static boolean initialized = false;

    Instance() {
      if(!initialized) {
        style().ensureInjected();

        HeadElement head = (HeadElement) Document.get().getElementsByTagName("head").getItem(0);
        ScriptElement element = Document.get().createScriptElement(script().getText());

        element.setId("prettify_" + script().getName());
        element.setType("text/javascript");

        head.appendChild(element);
        initialized = true;
      }
    }

    @Override
    public CssResource style() {
      return RESOURCES.style();
    }

    @Override
    public TextResource script() {
      return RESOURCES.script();
    }

    @Override
    public Languages langs() {
      return RESOURCES.langs();
    }

  }
}
