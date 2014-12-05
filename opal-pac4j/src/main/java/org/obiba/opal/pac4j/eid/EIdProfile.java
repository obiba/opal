package org.obiba.opal.pac4j.eid;

import org.pac4j.core.profile.AttributesDefinition;
import org.pac4j.openid.profile.OpenIdProfile;

import java.util.Locale;

/**
 *
 */
public class EIdProfile extends OpenIdProfile {

    private static final long serialVersionUID = 1L;

    @Override
    protected AttributesDefinition getAttributesDefinition() {
        return EIdAttributesDefinition.instance;
    }

    @Override
    public Locale getLocale() {
        //return (Locale) getAttribute(EIdAttributesDefinition.LANGUAGE);
        return Locale.ENGLISH; //@TODO confirm language
    }

    @Override
    public String getDisplayName() {
        return (String) getAttribute(EIdAttributesDefinition.FULLNAME);
    }

}
