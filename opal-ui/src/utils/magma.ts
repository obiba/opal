export const valueTypes = [
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

export const valueTypesMap = {
  textual: ['text', 'locale'],
  numerical: ['integer', 'decimal'],
  temporal: ['date', 'datetime'],
  geospatial: ['point', 'linestring', 'polygon'],
  other: ['binary', 'boolean'],
};
