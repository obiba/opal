package org.obiba.opal.core.service.validation;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.obiba.magma.Attribute;
import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.Variable;
import org.obiba.opal.core.service.ValidationService;
import org.obiba.opal.core.service.security.SystemKeyStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.*;

/**
 * Knows how to create DataValidators for a Variable.
 * Datasource and variable attributes will determine if validation is enabled, and which validators the variable has.
 */
@Component("validatorFactory")
public class ValidatorFactory {

    private static final Logger log = LoggerFactory.getLogger(ValidatorFactory.class);

    private Map<String, VocabularyImporter> importerMap;

    @Autowired
    private SystemKeyStoreService systemKeyStoreService;

    private KeyStore keyStore; //lazily initialized from systemKeyStoreService or injected from unit test

    /**
     * Returns the list of validators for a given Variable.
     *
     * @param variable
     * @return list of validators
     */
    public List<DataValidator> getValidators(Variable variable) {
        List<DataValidator> result = new ArrayList<>();

        Attribute attr = null;
        try {
            attr = variable.getAttribute(ValidationService.VOCABULARY_URL_ATTRIBUTE);
        } catch (NoSuchAttributeException ex) {
            //ignored
        }

        if (attr != null) {
            String url = attr.getValue().toString();
            try {
                result.add(getVocabularyValidator(url));
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                String msg =
                		String.format("Error obtaining validators for variable %s", variable.getName());
                throw new RuntimeException(msg, ex);
            }
        }

        return result;
    }

    /**
     * Returns a vocabulary validator for a given url.
     *
     * @param url
     * @return
     * @throws IOException
     */
    public VocabularyValidator getVocabularyValidator(String url) throws Exception {
        int idx = url.lastIndexOf('.');
        if (idx < 0) {
            throw new IllegalArgumentException("Could not obtain filename extension from " + url);
        }
        String extension = url.substring(idx + 1).toLowerCase();
        VocabularyImporter importer = importerMap.get(extension);

        if (importer == null) {
            throw new UnsupportedOperationException("File extension " + extension + " is not supported");
        }

        try {
            Set<String> codes = getVocabularyCodes(url, importer);
            return new VocabularyValidator(url, codes);
        } catch (RuntimeException ex) {
            throw ex;
        }
    }

    Set<String> getVocabularyCodes(String url, VocabularyImporter importer) throws IOException, GeneralSecurityException {
        if (url.startsWith("https")) {
            return getVocabularyCodesHttps(url, importer);
        } else {
            InputStream in = new URL(url).openStream();
            try {
                return importer.getCodes(in);
            } finally {
                in.close();
            }
        }
    }

    /**
     * To be used for unit tests purposes only
     * @param keyStore
     */
    synchronized void setKeyStore(KeyStore keyStore) {
        if (this.systemKeyStoreService == null) {
            this.keyStore = keyStore;
        } else {
            //no to be used when systemKeyStoreService is available
            throw new IllegalStateException("Should not be set manually when systemKeyStoreService is available");
        }
    }

    private synchronized KeyStore getKeyStore() {
        if (keyStore == null) {
            keyStore = systemKeyStoreService.getKeyStore().getKeyStore();
        }
        return keyStore;
    }

    private CloseableHttpClient getHttpsClient() throws IOException, GeneralSecurityException {

        KeyStore keyStore = getKeyStore();

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(keyStore, new TrustSelfSignedStrategy())
                .build();

        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[] { "TLSv1" },
                null,
                //SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
        );

        return HttpClients.custom()
                .useSystemProperties()
                .setSSLSocketFactory(sslsf).build();
    }

    private Set<String> getVocabularyCodesHttps(String url, VocabularyImporter importer) throws IOException, GeneralSecurityException {
        CloseableHttpClient httpClient = getHttpsClient();

        try {
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            try {
                StatusLine status = response.getStatusLine();
                if (status.getStatusCode() != 200) {
                    throw new RuntimeException(String.format("Error getting contents of %s: status is %s", url, status.toString()));
                }
                HttpEntity entity = response.getEntity();
                return importer.getCodes(entity.getContent());
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
    }

    @PostConstruct
    public void postConstruct() {
        Map<String, VocabularyImporter> map = new HashMap<>();
        VocabularyImporter csvImporter = new CsvVocabularyImporter();
        map.put("csv", csvImporter);
        map.put("txt", csvImporter);
        this.importerMap = Collections.unmodifiableMap(map);
    }

}
