import { TaxonomyDto } from 'src/models/Opal';

export function getCreativeCommonsLicenseUrl() {
  return 'http://creativecommons.org/choose/';
}

export function getCreativeCommonsLicenseAnchor(taxonomy: TaxonomyDto) {
  const theLicense = taxonomy.license || '';
  const licenseParts = theLicense.split(/\s+/);
  if (licenseParts.length === 3) {
    return `
    <a href="https://creativecommons.org/licenses/${licenseParts[1]}/${licenseParts[2]}" target="_blank">${theLicense}</a>`;
  } else {
    return theLicense;
  }
}
