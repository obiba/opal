import { AttributeDto } from 'src/models/Magma';

export function getLabels(attributes: AttributeDto[]) {
  return attributes
    ? attributes.filter((a) => a.name === 'label' && a.namespace === undefined)
    : [];
}
