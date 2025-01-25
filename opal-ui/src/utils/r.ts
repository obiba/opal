import type { RPackageDto } from 'src/models/OpalR';

export function getPackageKey(pkg: RPackageDto) {
  return `${pkg.name}-${getDescriptionValue(pkg, 'LibPath')}`;
}

export function getDescriptionValue(pkg: RPackageDto, key: string) {
  return pkg.description.find((entry) => entry.key === key)?.value;
}
