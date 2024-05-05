// Code generated by protoc-gen-ts_proto. DO NOT EDIT.
// versions:
//   protoc-gen-ts_proto  v1.174.0
//   protoc               v3.12.4
// source: Math.proto

/* eslint-disable */

export const protobufPackage = "Math";

export interface SummaryStatisticsDto {
  resource: string;
  limit?: number | undefined;
}

export interface CategoricalSummaryDto {
  mode: string;
  n: number;
  frequencies: FrequencyDto[];
  otherFrequency?: number | undefined;
}

export interface ContinuousSummaryDto {
  summary: DescriptiveStatsDto | undefined;
  distributionPercentiles: number[];
  intervalFrequency: IntervalFrequencyDto[];
  frequencies: FrequencyDto[];
}

export interface DefaultSummaryDto {
  n: number;
  frequencies: FrequencyDto[];
}

export interface BinarySummaryDto {
  n: number;
  frequencies: FrequencyDto[];
}

export interface TextSummaryDto {
  n: number;
  frequencies: FrequencyDto[];
  otherFrequency?: number | undefined;
}

export interface GeoSummaryDto {
  n: number;
  frequencies: FrequencyDto[];
  points: PointDto[];
}

export interface PointDto {
  lat: number;
  lon: number;
}

export interface FrequencyDto {
  value: string;
  freq: number;
  pct: number;
  missing: boolean;
  cummFreq?: number | undefined;
  cummPct?: number | undefined;
}

export interface IntervalFrequencyDto {
  lower: number;
  upper: number;
  freq: number;
  density: number;
}

export interface DescriptiveStatsDto {
  min?: number | undefined;
  max?: number | undefined;
  mean?: number | undefined;
  geometricMean?: number | undefined;
  n?: number | undefined;
  sum?: number | undefined;
  sumsq?: number | undefined;
  stdDev?: number | undefined;
  variance?: number | undefined;
  percentiles: number[];
  skewness?: number | undefined;
  kurtosis?: number | undefined;
  median?: number | undefined;
  values: number[];
}
