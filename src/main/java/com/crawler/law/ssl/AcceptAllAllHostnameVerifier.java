package com.crawler.law.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * This class implements a fake hostname verifier, trusting any host name.
 *
 * @author Q-GMA
 * @author Q-APE
 */
public class AcceptAllAllHostnameVerifier implements HostnameVerifier {

    /**
     * Always return true, indicating that the host name is
     * an acceptable match with the server's authentication scheme.
     *
     * @param hostname the host name.
     * @param session the SSL session used on the connection to
     * host.
     * @return the true boolean value
     * indicating the host name is trusted.
     */
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}
