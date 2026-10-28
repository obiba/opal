import { describe, expect, it } from 'vitest';
import { decodePath, encodePath, u } from './url';

describe('url helpers', () => {
  it('encodes each interpolated value as one path segment', () => {
    expect(u`/datasource/${'Amber'}/table/${'CA Postal Codes'}/variable/${'Q#1?'}`).toBe(
      '/datasource/Amber/table/CA%20Postal%20Codes/variable/Q%231%3F',
    );
    expect(u`/files/${'a+b 100%.csv'}`).toBe('/files/a%2Bb%20100%25.csv');
  });

  it('encodes a path segment by segment', () => {
    expect(encodePath('/home/user/my file.csv')).toBe('/home/user/my%20file.csv');
  });

  it('decodes a link and keeps a segment that is not percent-encoded', () => {
    expect(decodePath('/datasource/Amber/table/CA%20Postal%20Codes')).toBe('/datasource/Amber/table/CA Postal Codes');
    expect(decodePath('/datasource/Amber/table/50% sample')).toBe('/datasource/Amber/table/50% sample');
  });
});
