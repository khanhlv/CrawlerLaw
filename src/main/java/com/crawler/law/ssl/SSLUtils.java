package com.crawler.law.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * Утилитные методы для настройки работы SSL соединения
 *
 * @author Q-GMA
 * @author Q-APE
 */
public final class SSLUtils {

    /**
     * Hostname verifier.
     */
    private static HostnameVerifier hostnameVerifier;

    /**
     * Thrust managers.
     */
    private static TrustManager[] trustManagers;

    /**
     * Set the default Hostname Verifier to an instance of a fake class that
     * trust all hostnames.
     */
    public static void trustAllHostnames() {
        // Create a trust manager that does not validate certificate chains
        if (hostnameVerifier == null) {
            hostnameVerifier = new AcceptAllAllHostnameVerifier();
        }
        // Install the all-trusting host name verifier:
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }

    /**
     * Set the default X509 Trust Manager to an instance of a fake class that
     * trust all certificates, even the self-signed ones.
     */
    public static void trustAllHttpsCertificates() {
        SSLContext context;

        // Create a trust manager that does not validate certificate chains
        if (trustManagers == null) {
            trustManagers = getTrustAllTrustManagers();
        }
        // Install the all-trusting trust manager:
        try {
            context = SSLContext.getInstance("SSL");
            context.init(null, trustManagers, new SecureRandom());
        }
        catch (GeneralSecurityException gse) {
            throw new IllegalStateException(gse.getMessage());
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
    }

    /**
     * Returns an array containing single TrustAllTrustManager.
     *
     * @return TrustManager[] an array containing TrustAllTrustManager
     */
    public static TrustManager[] getTrustAllTrustManagers() {
        return new TrustManager[] {new TrustAllTrustManager()};
    }

}
