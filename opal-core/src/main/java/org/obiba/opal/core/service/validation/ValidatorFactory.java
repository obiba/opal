package org.obiba.opal.core.service.validation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
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
import java.net.URISyntaxException;
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
        try {
            Attribute attr = variable.getAttribute(ValidationService.VOCABULARY_URL_ATTRIBUTE);
            URL url = new URL(attr.getValue().toString());
            return Lists.<DataValidator>newArrayList(getVocabularyValidator(url));
        } catch (NoSuchAttributeException ex) {
            return Lists.newArrayList();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            String msg =
                    String.format("Error obtaining validators for variable %s", variable.getName());
            throw new RuntimeException(msg, ex);
        }
    }

    /**
     * Returns a vocabulary validator for a given url.
     *
     * @param url
     * @return
     * @throws IOException
     */
    public VocabularyValidator getVocabularyValidator(URL url) throws Exception {
        String extension = Files.getFileExtension(url.getFile());
        if (extension.isEmpty()) {
            throw new IllegalArgumentException("Could not obtain filename extension from " + url);
        }
        VocabularyImporter importer = importerMap.get(extension);

        if (importer == null) {
            throw new UnsupportedOperationException("File extension " + extension + " is not supported");
        }

        Set<String> codes = getVocabularyCodes(url, importer);
        return new VocabularyValidator(url.toString(), codes);
    }

    Set<String> getVocabularyCodes(URL url, VocabularyImporter importer) throws IOException, GeneralSecurityException {
        if ("https".equals(url.getProtocol())) {
            return getVocabularyCodesHttps(url, importer);
        } else {
            try (InputStream in = url.openStream()) {
                return importer.getCodes(in);
            }
        }
    }

    /**
     * To be used for unit tests purposes only
     * @param keyStore
     */
    @VisibleForTesting
    void setKeyStore(KeyStore keyStore) {
        if (this.systemKeyStoreService != null) {
            //not to be used when systemKeyStoreService is available
            throw new IllegalStateException("Should not be set manually when systemKeyStoreService is available");
        }
        this.keyStore = keyStore;
    }

    private CloseableHttpClient getHttpsClient() throws IOException, GeneralSecurityException {

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

    private Set<String> getVocabularyCodesHttps(URL url, VocabularyImporter importer) throws IOException,
            GeneralSecurityException {
        try (CloseableHttpClient httpClient = getHttpsClient();
             CloseableHttpResponse response = httpClient.execute(new HttpGet(url.toURI())) ) {
            StatusLine status = response.getStatusLine();
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException(String.format("Error getting contents of %s: status is %s", url, status.toString()));
            }
            return importer.getCodes(response.getEntity().getContent());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    public void postConstruct() {
        if (systemKeyStoreService != null) {
            keyStore = systemKeyStoreService.getKeyStore().getKeyStore();
        }

        Map<String, VocabularyImporter> map = new HashMap<>();
        VocabularyImporter csvImporter = new CsvVocabularyImporter();
        map.put("csv", csvImporter);
        map.put("txt", csvImporter);
        this.importerMap = Collections.unmodifiableMap(map);
    }

}
