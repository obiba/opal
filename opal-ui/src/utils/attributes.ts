import { Attribute } from 'src/components/models';

export function getLabels(attributes: Attribute[]) {
  return attributes
    ? attributes.filter((a) => a.name === 'label' && a.namespace === undefined)
    : [];
}
