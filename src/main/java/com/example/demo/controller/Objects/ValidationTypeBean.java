package com.example.demo.controller.Objects;

public class ValidationTypeBean {

    public enum ValidationType {
	ValidationFormulae,
        ValidationCNIILocal,
        ValidationCNIICorp,
        ValidationFormulaeImport,
        ValidationCNIICorpECargabal
        	
    }

    private ValidationType type;

    public ValidationTypeBean() {
    }

    public ValidationType getType() {
	return type;
    }

    public void setType(ValidationType type) {
	this.type = type;
    }

    public ValidationType setValidationType(String t) {
        
        if(t.equals(ValidationType.ValidationFormulae.toString()))
            setType(ValidationType.ValidationFormulae);
        
        else if(t.equals(ValidationType.ValidationCNIICorp.toString()))
            setType(ValidationType.ValidationCNIICorp);
        
        else if(t.equals(ValidationType.ValidationCNIILocal.toString()))
            setType(ValidationType.ValidationCNIILocal);
         else if(t.equals(ValidationType.ValidationFormulaeImport.toString()))
            setType(ValidationType.ValidationFormulaeImport);
        else if(t.equals(ValidationType.ValidationCNIICorpECargabal.toString()))
           setType(ValidationType.ValidationCNIICorpECargabal);

	return type;
    }

}