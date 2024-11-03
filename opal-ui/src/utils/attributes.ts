import { AttributeDto } from 'src/models/Magma';

export function getLabels(attributes: AttributeDto[] | undefined) {
  return attributes ? attributes.filter((a) => a.name === 'label' && a.namespace === undefined) : [];
}

export function getLabelsString(attributes: AttributeDto[] | undefined) {
  const labels = getLabels(attributes);
  if (labels.length > 0) {
    return labels.map(attr => attr.locale ? `(${attr.locale}) ${attr.value}` : attr.value).join(' | ');
  }
  return '';
}

export function getDescriptions(attributes: AttributeDto[]) {
  return attributes ? attributes.filter((a) => a.name === 'description' && a.namespace === undefined) : [];
}
