package org.obiba.opal.pac4j.eid;

import org.obiba.opal.pac4j.eid.EIdAttributesDefinition.Attr;
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
        //return (String) getAttribute(Attr.FULLNAME.getKey());
        return getId();
    }

    /**
     * Overriden so the id is something readable and useful for Opal.
     * By doing this we are suppressing the standard id, but the alternative
     * is to extend many other classes just to override a couple of methods, and not always in a clean way.
     * @return username for Opal
     */
    @Override
    public String getId() {
        Object name = getAttribute(Attr.FULLNAME.getKey());
        Object dob = getAttribute(Attr.BIRTHDATE.getKey());
        String str = "" + name + getPathFriendlyDate(dob);
        return str.replace(' ','_');
    }

    /**
     * Returns a date String where '/' are replaced with '-' (path friendly).
     * If the date is not path friendly, Opal will not be able to create the user folder.
     * @param date
     * @return
     */
    private String getPathFriendlyDate(Object date) {
        if (date == null) {
            return "";
        }

        String str = date.toString();
        return str.replace('/','-');
    }

    /**
     * Overriden so type id picks getId(), and not the stored id
     * Also avoids returning the # separator, as it causes problems with REST urls later on.
     * @return full id
     */
    @Override
    public String getTypedId() {
        //return this.getClass().getSimpleName() + SEPARATOR + getId();
        return getId();
    }

}
