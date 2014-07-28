package org.obiba.opal.core.service.validation;

import org.obiba.magma.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created by carlos on 7/28/14.
 */
public class ValidatorFactory {

    private static final Map<String, VocabularyImporter> importerMap = new HashMap<>();
    private static final String VALIDATION_FAILURE_MSG = "";

    static {
        VocabularyImporter csvImporter = new CsvVocabularyImporter();
        importerMap.put("csv", csvImporter);
        importerMap.put("txt", csvImporter);
    }

    public List<DataValidator> getValidators(Variable variable) {
        List<DataValidator> result = new ArrayList<>();
        //TODO add type validators

        Attribute attr = variable.getAttribute("vocabulary_url");
        if (attr != null) {
            try {
                String url = attr.getValue().toString();
                result.add(getVocabularyValidator(url));
            } catch (Exception ex) {
                //TODO log error in console
            }
        }

        return result;
    }

    public VocabularyValidator getVocabularyValidator(String url) throws IOException {
        int idx = url.lastIndexOf('.');
        if (idx < 0) {
            throw new IllegalArgumentException("Could not obtain filename extension from " + url);
        }
        String extension = url.substring(idx + 1).toLowerCase();
        VocabularyImporter importer = importerMap.get(extension);

        if (importer == null) {
            throw new UnsupportedOperationException("File extension " + extension + " is not supported");
        }

        URL url2 = new URL(url);
        String name = url2.getPath();
        Set<String> codes = importer.getCodes(url2);
        return new VocabularyValidator(name, codes);
    }

    public void validate(ValueTable valueTable) {
        System.out.println("----------");

        Map<Variable, List<DataValidator>> validatorMap = new HashMap<>();
        ValidatorFactory validatorFactory = new ValidatorFactory();

        for (Variable var: valueTable.getVariables()) {
            List<DataValidator> validators = validatorFactory.getValidators(var);
            if (validators != null && validators.size() > 0) {
                validatorMap.put(var, validators);
            }
        }

        if (validatorMap.isEmpty()) {
            return; //noting to validate
        }

        Iterator<ValueSet> valueSets = valueTable.getValueSets().iterator();
        while (valueSets.hasNext()) {
            ValueSet vset = valueSets.next();
            Set<String> failures = getFailures(validatorMap, valueTable, vset);
            if (failures.size() > 0) {
                String msg = "Entity " + vset.getVariableEntity().getIdentifier() + " failed validation of rules " + failures;
                //TODO log failures
                //vset.getVariableEntity().
            }
        }

    }

    public Set<String> getFailures(Map<Variable, List<DataValidator>> validatorMap, ValueTable valueTable,
                                   ValueSet valueSet) {

        Set<String> result = new HashSet<>();
        for (Map.Entry<Variable, List<DataValidator>> entry: validatorMap.entrySet()) {
            Value value = valueTable.getValue(entry.getKey(), valueSet);
            List<DataValidator> validators = entry.getValue();
            for (DataValidator validator: validators) {
                if (!validator.isValid(value)) {
                    result.add(validator.getName());
                }
            }
        }
        return result;
    }

}
