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
