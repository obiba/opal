import { PluginPackageDto, DatasourcePluginPackageDto } from 'src/models/Plugins';
import { ViewDto, VariableListViewDto, DatasourceFactoryDto, CsvDatasourceFactoryDto, FsDatasourceFactoryDto, RHavenDatasourceFactoryDto, PluginDatasourceFactoryDto, RestDatasourceFactoryDto, AttributeDto } from 'src/models/Magma';
import { TaxonomyDto, VocabularyDto, TermDto } from 'src/models/Opal';

export interface StringMap {
  [key: string]: string | string[] | undefined;
}

export interface Message {
  msg: string;
  timestamp: number;
}

export interface FileObject extends Blob {
  readonly size: number;
  readonly name: string;
  readonly path: string;
  readonly type: string;
}

//
// Plugins
//

export interface PluginPackage extends PluginPackageDto {
  'Plugins.DatasourcePluginPackageDto.datasource': DatasourcePluginPackageDto | undefined;
}

//
// Datasource
//

export interface View extends ViewDto {
  'Magma.VariableListViewDto.view': VariableListViewDto | undefined;
}

export interface DatasourceFactory extends DatasourceFactoryDto {
  'Magma.CsvDatasourceFactoryDto.params': CsvDatasourceFactoryDto | undefined;
  'Magma.FsDatasourceFactoryDto.params': FsDatasourceFactoryDto | undefined;
  'Magma.RHavenDatasourceFactoryDto.params': RHavenDatasourceFactoryDto | undefined;
  'Magma.PluginDatasourceFactoryDto.params': PluginDatasourceFactoryDto | undefined;
  'Magma.RestDatasourceFactoryDto.params': RestDatasourceFactoryDto | undefined;
}

export interface EnumOption {
  key: string;
  title: string;
}

export interface SchemaFormField {
  key: string;
  type: string;
  format?: string;
  title?: string;
  description?: string;
  default?: string;
  minimum?: number;
  maximum?: number;
  fileFormats?: string[];
  enum?: EnumOption[];
  items: SchemaFormField[];
}

export interface SchemaFormObject {
  '$schema': string;
  type: string;
  title?: string;
  description?: string;
  items: SchemaFormField[];
  required: string[];
}

export interface FormObject {
  [key: string]: boolean | number | string | FileObject | FormObject | Array<FormObject> | undefined;
}

export interface AttributesBundle {
  id: string;
  attributes: AttributeDto[];
}

export interface Annotation extends AttributesBundle {
  taxonomy: TaxonomyDto;
  vocabulary: VocabularyDto;
  term: TermDto;
}
