import type { TaxonomyDto } from 'src/models/Opal';

export function getCreativeCommonsLicenseUrl() {
  return 'http://creativecommons.org/choose/';
}

export function getCreativeCommonsLicenseAnchor(taxonomy: TaxonomyDto, locale = 'en') {
  const theLicense = taxonomy.license || '';
  const licenseParts = theLicense.split(/\s+/);
  if (licenseParts.length === 3) {
    return `
    <a href="https://creativecommons.org/licenses/${licenseParts[1]}/${licenseParts[2]}/deed.${locale}" target="_blank">${theLicense}</a>`;
  } else {
    return theLicense;
  }
}
