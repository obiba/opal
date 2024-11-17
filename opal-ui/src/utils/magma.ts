import { VariableDto } from 'src/models/Magma';

export const ValueTypes = [
  'text',
  'integer',
  'decimal',
  'date',
  'datetime',
  'point',
  'linestring',
  'polygon',
  'binary',
  'boolean',
  'locale',
];

export const ValueTypesMap = {
  textual: ['text', 'locale'],
  numerical: ['integer', 'decimal'],
  temporal: ['date', 'datetime'],
  geospatial: ['point', 'linestring', 'polygon'],
  other: ['binary', 'boolean'],
};

export const VariableNatures = {
  CATEGORICAL: 'CATEGORICAL',
  CONTINUOUS: 'CONTINUOUS',
  TEMPORAL: 'TEMPORAL',
  GEO: 'GEO',
  BINARY: 'BINARY',
  UNDETERMINED: 'UNDETERMINED',
};

export const getVariableNature = (variable: VariableDto) => {
  if (variable.categories && variable.categories.length > 0 && variable.categories.find((c) => !c.isMissing)) {
    return VariableNatures.CATEGORICAL;
  }

  if (variable.valueType === 'boolean') {
    return VariableNatures.CATEGORICAL;
  }

  if (variable.valueType === 'integer' || variable.valueType === 'decimal') {
    return VariableNatures.CONTINUOUS;
  }

  if (variable.valueType === 'date' || variable.valueType === 'datetime') {
    return VariableNatures.TEMPORAL;
  }

  if (variable.valueType === 'point' || variable.valueType === 'linestring' || variable.valueType === 'polygon') {
    return VariableNatures.GEO;
  }

  if (variable.valueType === 'binary') {
    return VariableNatures.BINARY;
  }

  return VariableNatures.UNDETERMINED;
}