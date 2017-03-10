package org.obiba.opal.core.domain;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

public class GenotypesMapping {
    @NotNull
    @NotBlank
    private String projectName;

    @NotNull
    @NotBlank
    private String tableName;

    @NotNull
    @NotBlank
    private String participantIdVariable;

    @NotNull
    @NotBlank
    private String sampleIdVariable;

    @NotNull
    @NotBlank
    private String sampleRoleVariable;

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getParticipantIdVariable() {
        return participantIdVariable;
    }

    public void setParticipantIdVariable(String participantIdVariable) {
        this.participantIdVariable = participantIdVariable;
    }

    public String getSampleIdVariable() {
        return sampleIdVariable;
    }

    public void setSampleIdVariable(String sampleIdVariable) {
        this.sampleIdVariable = sampleIdVariable;
    }

    public String getSampleRoleVariable() {
        return sampleRoleVariable;
    }

    public void setSampleRoleVariable(String sampleRoleVariable) {
        this.sampleRoleVariable = sampleRoleVariable;
    }
    
    public static class Builder {
        private GenotypesMapping genotypesMapping;
        
        private Builder() {
            genotypesMapping = new GenotypesMapping();
        }
        
        public Builder projectName(String value) {
            genotypesMapping.projectName = value;
            return this;
        }
        
        public Builder tableName(String value) {
            genotypesMapping.tableName = value;
            return this;
        }

        public Builder participantIdVariable(String value) {
            genotypesMapping.participantIdVariable = value;
            return this;
        }

        public Builder sampleIdVariable(String value) {
            genotypesMapping.sampleIdVariable = value;
            return this;
        }

        public Builder sampleRoleVariable(String value) {
            genotypesMapping.sampleRoleVariable = value;
            return this;
        }

        public GenotypesMapping build() {
            return genotypesMapping;
        }
    }
}
