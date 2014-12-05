package org.obiba.opal.pac4j.eid;

import org.pac4j.core.profile.AttributesDefinition;
import org.pac4j.core.profile.converter.Converters;

/**
 * Attribute types at http://openid.net/specs/openid-attribute-properties-list-1_0-01.html
 */
public class EIdAttributesDefinition extends AttributesDefinition {

    public static final AttributesDefinition instance = new EIdAttributesDefinition();

    public static String FULLNAME = "name";
    public static String FIRSTNAME = "firstname";
    public static String LASTNAME = "lastname";
    public static String CARDNUMBER = "cardnumber";

    public EIdAttributesDefinition() {
        addAttribute(FULLNAME, Converters.stringConverter);
        addAttribute(FIRSTNAME, Converters.stringConverter);
        addAttribute(LASTNAME, Converters.stringConverter);
        addAttribute(CARDNUMBER, Converters.stringConverter);
    }

}
