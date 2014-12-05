package org.obiba.opal.pac4j.eid;

/**
 * OpenID Attribute Exchange 1.0 Attribute Types returned by eID.
 * <p/>
 *
 * @author Wim Vandenhaute
 * @author Frank Cornelis
 * @see <a href="http://openid.net/specs/openid-attribute-properties-list-1_0-01.html">OpenID AX Schema</a>
 */
public class OpenIDAXConstants {
    // default attributes
    public static final String AX_NAME_PERSON_TYPE = "http://axschema.org/namePerson";
    public static final String AX_FIRST_NAME_PERSON_TYPE = "http://axschema.org/namePerson/first";
    public static final String AX_LAST_NAME_PERSON_TYPE = "http://axschema.org/namePerson/last";

    // address attributes
    public static final String AX_POSTAL_ADDRESS_TYPE = "http://axschema.org/contact/postalAddress/home";
    public static final String AX_CITY_TYPE = "http://axschema.org/contact/city/home";
    public static final String AX_POSTAL_CODE_TYPE = "http://axschema.org/contact/postalCode/home";

    // identity attributes
    public static final String AX_BIRTHDATE_TYPE = "http://axschema.org/birthDate";
    public static final String AX_GENDER_TYPE = "http://axschema.org/person/gender";
    public static final String AX_NATIONALITY_TYPE = "http://axschema.org/eid/nationality";
    public static final String AX_PLACE_OF_BIRTH_TYPE = "http://axschema.org/eid/pob";
    public static final String AX_PHOTO_TYPE = "http://axschema.org/eid/photo";
    public static final String AX_RRN_TYPE = "http://axschema.org/eid/rrn";
    public static final String AX_CERT_AUTHN_TYPE = "http://axschema.org/eid/cert/auth";
    public static final String AX_AGE_TYPE = "http://axschema.org/eid/age";

    // card attributes
    public static final String AX_CARD_NUMBER_TYPE = "http://axschema.org/eid/card-number";
    public static final String AX_CARD_VALIDITY_BEGIN_TYPE = "http://axschema.org/eid/card-validity/begin";
    public static final String AX_CARD_VALIDITY_END_TYPE = "http://axschema.org/eid/card-validity/end";
}
