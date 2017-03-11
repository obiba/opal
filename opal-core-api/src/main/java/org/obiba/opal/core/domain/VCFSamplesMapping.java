package org.obiba.opal.core.domain;

import javax.validation.constraints.NotNull;

public class VCFSamplesMapping {
    @NotNull
    private String projectName;

    @NotNull
    private String tableName;

    @NotNull
    private String participantIdVariable;

    @NotNull
    private String sampleIdVariable;

    @NotNull
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
        private VCFSamplesMapping vcfSamplesMapping;
        
        private Builder() {
            vcfSamplesMapping = new VCFSamplesMapping();
        }
        
        public Builder projectName(String value) {
            vcfSamplesMapping.projectName = value;
            return this;
        }
        
        public Builder tableName(String value) {
            vcfSamplesMapping.tableName = value;
            return this;
        }

        public Builder participantIdVariable(String value) {
            vcfSamplesMapping.participantIdVariable = value;
            return this;
        }

        public Builder sampleIdVariable(String value) {
            vcfSamplesMapping.sampleIdVariable = value;
            return this;
        }

        public Builder sampleRoleVariable(String value) {
            vcfSamplesMapping.sampleRoleVariable = value;
            return this;
        }

        public VCFSamplesMapping build() {
            return vcfSamplesMapping;
        }
    }
}
