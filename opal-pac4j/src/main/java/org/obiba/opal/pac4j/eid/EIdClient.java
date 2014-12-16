package org.obiba.opal.pac4j.eid;

import org.obiba.opal.pac4j.Pac4jConfigurer;
import org.obiba.opal.pac4j.eid.EIdAttributesDefinition.Attr;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageException;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.openid.client.BaseOpenIdClient;
import org.pac4j.openid.credentials.OpenIdCredentials;

/**
 *
 */
public class EIdClient extends BaseOpenIdClient<CommonProfile> {

    public static final String NAME = "eidClient";

    private String endpoint;

    public EIdClient() {
        setName(NAME);
        setCallbackUrl(Pac4jConfigurer.getCallbackUrl());
    }

    @Override
    protected String getUser(WebContext context) {
        return endpoint;
    }

    @Override
    protected FetchRequest getFetchRequest() throws MessageException {
        final FetchRequest fetchRequest = FetchRequest.createFetchRequest();
        for (Attr attr: Attr.values()) {
            fetchRequest.addAttribute(attr.getKey(), attr.getTypeUri(), attr.isRequired());
        }
        logger.debug("fetchRequest: {}", fetchRequest);
        return fetchRequest;
    }

    @Override
    protected CommonProfile createProfile(AuthSuccess authSuccess) throws MessageException {
        final EIdProfile profile = new EIdProfile();

        if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
            final FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);

            for (final String name : EIdAttributesDefinition.instance.getAllAttributes()) {
                profile.addAttribute(name, fetchResp.getAttributeValue(name));
            }
        }
        return profile;
    }

    @Override
    protected BaseClient<OpenIdCredentials, CommonProfile> newClient() {
        return new EIdClient();
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return CommonHelper.toString(this.getClass(), "callbackUrl", this.callbackUrl, "name", getName());
    }

}
