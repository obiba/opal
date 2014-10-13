package org.obiba.opal.core.service.validation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Files;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.obiba.magma.Attribute;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
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
 * Knows how to create DataConstraints for a Variable.
 * Table/View and variable attributes will determine if validation is enabled, and which constraints the variable has.
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
    public List<DataConstraint> getValidators(ValueTable valueTable, Variable variable) {

        List<DataConstraint> result = new ArrayList<>();

        try {
            for (ConstraintType type: ConstraintType.values()) {
                DataConstraint dc = getConstraint(type, valueTable, variable);
                if (dc != null) {
                    result.add(dc);
                }
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            String msg =
                    String.format("Error obtaining validators for variable %s", variable.getName());
            throw new RuntimeException(msg, ex);
        }
        return result;
    }

    private DataConstraint getConstraint(ConstraintType type, ValueTable table, Variable variable) throws Exception {

        Attribute attr = type.getAttributeValue(variable);
        if (attr == null && type.isAtrributeBased()) {
            return null;
        }

        DataConstraint result = null;
        switch (type) {
            case EMBEDDED_VOCABULARY:
                if (table.isView() && variable.hasCategories()) {
                    result = new EmbeddedVocabularyConstraint(variable);
                }
                break;
            case EXTERNAL_VOCABULARY:
                result = getVocabularyValidator(new URL(attr.getValue().toString()));
                break;
            case MIN_VALUE:
                result = new MinValueConstraint(getDouble(attr));
                break;
            case MAX_VALUE:
                result = new MaxValueConstraint(getDouble(attr));
                break;
            case PAST_DATE:
                result = new PastDateConstraint(); //attribute value is irrelevant
                break;
            default:
                throw new UnsupportedOperationException("Implement for " + type);
        }

        return result;
    }

    private static double getDouble(Attribute attr) {
        String str = attr.getValue().toString();
        return Double.valueOf(str);
    }

    /**
     * Returns a vocabulary validator for a given url.
     *
     * @param url
     * @return
     * @throws IOException
     */
    VocabularyConstraint getVocabularyValidator(URL url) throws Exception {
        String extension = Files.getFileExtension(url.getFile());
        if (extension.isEmpty()) {
            throw new IllegalArgumentException("Could not obtain filename extension from " + url);
        }
        VocabularyImporter importer = importerMap.get(extension);

        if (importer == null) {
            throw new UnsupportedOperationException("File extension " + extension + " is not supported");
        }

        Set<String> codes = getVocabularyCodes(url, importer);
        String msgPattern = "Not found in vocabulary " + url.toString() + ", variable %s, value %s";
        return new VocabularyConstraint(msgPattern, codes);
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

        /*
         * @Todo: The current code doesn't verify the certificate at all. The plan is to use self-signed certificates
         * for the mica instances we are connecting to, but to only trust known certificates rather than any
         * self-signed cert.
         */

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
