import type { TaxonomyDto } from 'src/models/Opal';
import DOMPurify from 'isomorphic-dompurify';

export function getCreativeCommonsLicenseUrl() {
  return 'http://creativecommons.org/choose/';
}

export function getCreativeCommonsLicenseAnchor(taxonomy: TaxonomyDto, locale = 'en') {
  const theLicense = taxonomy.license || '';
  const licenseParts = theLicense.split(/\s+/);
  if (licenseParts.length === 3) {
    return DOMPurify.sanitize(`
    <a href="https://creativecommons.org/licenses/${licenseParts[1]}/${licenseParts[2]}/deed.${locale}" target="_blank" rel="noopener noreferrer">${DOMPurify.sanitize(theLicense)}</a>`, {
      ALLOWED_TAGS: ['a'],
      ALLOWED_ATTR: ['href', 'target', 'rel']
    });
  } else {
    return DOMPurify.sanitize(theLicense);
  }
}
