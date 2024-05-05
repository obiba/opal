import { PluginPackageDto, DatasourcePluginPackageDto } from 'src/models/Plugins';
import { ViewDto, VariableListViewDto, DatasourceFactoryDto, CsvDatasourceFactoryDto } from 'src/models/Magma';

export interface Message {
  msg: string;
  timestamp: number;
}

export interface FileObject extends Blob {
  readonly size: number;
  readonly name: string;
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
}
