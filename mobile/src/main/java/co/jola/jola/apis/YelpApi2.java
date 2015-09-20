package co.jola.jola.apis;

import org.scribe.model.Token;
import org.scribe.builder.api.DefaultApi10a;

/**
 * Created by jasonlin on 9/19/15.
 */
public class YelpApi2 extends DefaultApi10a {

    @Override
    public String getAccessTokenEndpoint() {
        return null;
    }

    @Override
    public String getAuthorizationUrl(Token arg0) {
        return null;
    }

    @Override
    public String getRequestTokenEndpoint() {
        return null;
    }

}
