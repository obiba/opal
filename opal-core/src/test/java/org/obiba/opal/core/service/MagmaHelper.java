package org.obiba.opal.core.service;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMock;
import org.obiba.magma.*;
import org.obiba.magma.support.StaticDatasource;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.magma.type.*;
import org.obiba.opal.core.service.validation.ConstraintType;

public class MagmaHelper {

	public static final String VOCAB_VARIABLE = "code";
	public static final String ENTITY = "Participant";
	
    public static URL getVocabularyFileUrl() throws Exception {
        File file = new File("src/test/resources/vocabulary.csv");
        URI uri = file.toURI();
        return uri.toURL();
    }
    
    public static Variable createVocabularyVariable() throws Exception {
    	return createVocabularyVariable(getVocabularyFileUrl().toExternalForm());
    }

    public static Variable createVariable() {
        Variable.Builder builder = new Variable.Builder(VOCAB_VARIABLE, TextType.get(), ENTITY);
        return builder.build();
    }

    public static Variable createVocabularyVariable(String vocabUrl) {
        System.out.println(vocabUrl);
    	Variable.Builder builder = new Variable.Builder(VOCAB_VARIABLE, TextType.get(), ENTITY);
    	builder.addAttribute(ConstraintType.EXTERNAL_VOCABULARY.getAttribute(), vocabUrl);
    	return builder.build();
    }
    
    public static final Value valueOf(Object obj) {
    	if (obj instanceof Value) {
    		return (Value)obj;
    	} else if (obj instanceof String) {
    		return TextType.get().valueOf(obj);
    	} else {
    		throw new UnsupportedOperationException("Not implemented for " + obj);
    	}
    }
    
    public static Datasource createDatasource(boolean validate) {
    	StaticDatasource result = new StaticDatasource("test");
    	if (validate) {
        	result.setAttributeValue(ValidationService.VALIDATE_ATTRIBUTE, TextType.get().valueOf("true"));
    	}
    	return result;
    }
    
    public static StaticValueTable createValueTable(Datasource datasource, Set<String> entities, Variable ... variables) throws Exception {
		StaticValueTable result = new StaticValueTable(datasource, "table", entities);
		for (Variable var: variables) {
			result.addVariable(var);
		}

		return result;
    }
    
    public static void addRow(StaticValueTable table, String entityId, Object ... varnameAndValues ) {
    	List<Object> list = new ArrayList<>();
    	for (int i=0; i<varnameAndValues.length; i+=2) {
    		Variable var = table.getVariable(varnameAndValues[i].toString());
    		list.add(var);
    		list.add(valueOf(varnameAndValues[i+1]));
    	}
    	table.addValues(entityId, list.toArray());
    }
    
    public static ValueSet createValueSet() {
    	ValueSet result = EasyMock.createMock(ValueSet.class);
    	
    	
    	return result;
    }

    public static Value valueOf(int value) {
        return IntegerType.get().valueOf(value);
    }

    public static Value valueOf(double value) {
        return DecimalType.get().valueOf(value);
    }

    public static Value nowAsDate(long delta) {
        return DateTimeType.get().valueOf(nowAsJavaDate(delta));
    }

    public static Value nowAsMagmaDate(long delta) {
        return DateTimeType.get().valueOf(new MagmaDate(nowAsJavaDate(delta)));
    }

    private static Date nowAsJavaDate(long delta) {
        return new Date(System.currentTimeMillis() + delta);
    }

}
