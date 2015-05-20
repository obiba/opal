package org.obiba.opal.pac4j.eid;

import org.pac4j.core.profile.AttributesDefinition;
import org.pac4j.core.profile.converter.AttributeConverter;
import org.pac4j.core.profile.converter.Converters;

/**
 * Attribute types at http://openid.net/specs/openid-attribute-properties-list-1_0-01.html
 */
public class EIdAttributesDefinition extends AttributesDefinition {

    public static final AttributesDefinition instance = new EIdAttributesDefinition();

    public EIdAttributesDefinition() {
        for (Attr attr: Attr.values()) {
            addAttribute(attr.key, attr.converter);
        }
    }

    public static enum Attr {
        FULLNAME("name", OpenIDAXConstants.AX_NAME_PERSON_TYPE),
        FIRSTNAME("firstname", OpenIDAXConstants.AX_FIRST_NAME_PERSON_TYPE),
        LASTNAME("lastname", OpenIDAXConstants.AX_LAST_NAME_PERSON_TYPE),
        BIRTHDATE("birthdate", OpenIDAXConstants.AX_BIRTHDATE_TYPE),
        //CARDNUMBER("cardnumber", OpenIDAXConstants.AX_CARD_NUMBER_TYPE),
        ;

        private final String key;
        private final String typeUri;
        private final AttributeConverter<? extends Object> converter;

        private Attr(String key, String typeUri) {
            this(key, typeUri, Converters.stringConverter);
        }

        private Attr(String key, String typeUri, AttributeConverter<? extends Object> converter) {
            this.key = key;
            this.typeUri = typeUri;
            this.converter = converter;
        }

        public String getKey() {
            return key;
        }

        public String getTypeUri() {
            return typeUri;
        }

        public boolean isRequired() {
            return true;
        }
    }

}
