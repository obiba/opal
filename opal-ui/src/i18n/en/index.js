export default {
  auth: {
    password: 'Password',
    signin: 'Sign in',
    signout: 'Sign out',
    username: 'User name',
  },
  error: {
    Forbidden: 'This operation is forbidden',
    NoSuchValueTableInDatasource: 'The table does not exist or you do not have access to it',
  },
  importer : {
    file: {
      csv: 'This format expects the file to use a "delimiter separated values" format (default delimiter being comma). The first column should represent the participant identifiers and the subsequent column names identify variables.',
      opal: 'This format comes as a .zip file containing a folder for each table having: the full data dictionary in a XML file, a XML data file per entity.',
      haven_rds: 'This format expects the file to be a valid RDS file containing an R object of tibble class. This import procedure requires a functional R server with the tibble package installed.',
      haven_sas: 'This format expects the file to be a valid SAS file containing the variables and the data. This import procedure requires a functional R server with the tibble and haven packages installed.',
      haven_sast: 'This format expects the file to be a valid SAS Transport file containing the variables and the data. This import procedure requires a functional R server with the tibble and haven packages installed.',
      haven_spss: 'This format expects the file to be a valid SPSS file, optionally compressed, containing the variables and the data. This import procedure requires a functional R server with the tibble and haven packages installed.',
      haven_stata: 'This format expects the file to be a valid Stata file containing the variables and the data. This import procedure requires a functional R server with the tibble and haven packages installed.',
    },
    server: {
      opal: 'This format imports variable dictionaries and data from a remote Opal.',
    }
  },
  main: {
    brand: 'Opal',
    powered_by: 'Powered by',
  },
  add_a_view: 'Add view',
  add_folder: 'Add folder',
  add_table: 'Add table',
  add_tables: 'Add/update some tables',
  add: 'Add',
  administration: 'Administration',
  advanced_options: 'Advanced options',
  all_categories: 'All',
  all_projects: 'All',
  apps_caption: 'Manage external applications providing services',
  apps: 'Apps',
  attributes: 'Attributes',
  cancel: 'Cancel',
  categories: 'Categories',
  confirm: 'Confirm',
  content: 'Content',
  copy_incremental_hint: 'Whether only new or updated data will be copied.',
  copy_incremental: 'Incremental',
  copy_nulls_hint: 'Whether a data can be overridden by a null value.',
  copy_nulls: 'Copy null values',
  copy_tables_task_created: 'Copy tables task created with identifier [ {id} ].',
  import_data_task_created: 'Import data task created with identifier [ {id} ].',
  copy_tables_text: 'No tables to be copied | One table will be copied | {count} tables will be copied.',
  copy_tables: 'Copy tables',
  copy: 'Copy',
  created: 'Created on',
  dashboard: 'Dashboard',
  date: 'Date',
  data_access: 'Data access',
  data_analysis: 'Data analysis',
  data_file: 'Data file',
  data_management: 'Data management',
  databases_caption: 'Manage identifiers database and data databases for storage, import and export',
  databases: 'Databases',
  datashield_caption: 'Configure DataSHIELD, grant access to DataSHIELD service',
  datashield: 'DataSHIELD',
  delete_files_confirm: '- | Are you sure you want to delete this file? | Are you sure you want to delete these {count} files?',
  delete_tables_confirm: 'No tables to delete | Are you sure you want to delete this table? | Are you sure you want to delete these {count} tables?',
  delete_variables_confirm: 'No variables to delete | Are you sure you want to delete this variable? | Are you sure you want to delete these {count} variables?',
  delete: 'Delete',
  density: 'Density',
  derivation_script: 'Derivation script',
  descriptive_statistics: 'Descriptive statistics',
  dictionary: 'Dictionary',
  docs: 'Docs',
  documentation_cookbook: 'Documentation and cookbook',
  download_dictionaries: 'Download dictionaries',
  download_dictionary: 'Download dictionary',
  download_view: 'Download view',
  download_views_backup: 'Backup views',
  download: 'Download',
  encrypt_file_content: 'Encrypt file content',
  encrypt_password_hint: 'The password must have at least 8 characters.',
  encrypt_password: 'Password',
  end_time: 'End time',
  entities: 'Entities',
  entity_type: 'Entity type',
  extract: 'Extract',
  file_selection: 'File selection',
  file_system: 'System',
  files_caption: 'Manage files and folders, upload and download files',
  files: 'Files',
  frequencies: 'Frequencies',
  frequency: 'Frequency',
  full_name: 'Full name',
  full_summary: 'Full summary',
  general_settings_caption: 'Manage general server configuration',
  general_settings: 'General Settings',
  histogram: 'Histogram',
  id_mappings_caption: 'Manage identifiers mappings',
  id_mappings: 'Identifiers Mappings',
  id: 'ID',
  identity_providers_caption: 'Manage the OpenID Connect providers',
  identity_providers: 'Identity Providers',
  index: 'Index',
  intervals: 'Intervals',
  is_missing: 'Missing',
  jvm_caption: 'Monitor Java virtual machine',
  jvm: 'Java Virtual Machine',
  kurtosis: 'Kurtosis',
  label: 'Label',
  last_update: 'Last update',
  limit: 'Limit',
  locale: 'Locale',
  max: 'Max',
  mean: 'Mean',
  median: 'Median',
  merge_variables_hint: 'If selected, variable with same name will be merged (properties, categories and attributes will be added or updated, no deletion). Else the provided variables will replace the existing ones.',
  merge_variables: 'Merge variables',
  message: 'Message',
  messages: 'Messages',
  mime_type: 'Mime type',
  min: 'Min',
  missings: 'Missings',
  my_profile: 'My profile',
  name: 'Name',
  namespace: 'Namespace',
  new_name: 'New name',
  non_missings: 'Non missings',
  normal_distribution: 'Normal distribution',
  normal: 'Normal',
  not_empty: 'Not empty',
  occurrence_group: 'Occurrence group',
  other_links: 'Other links',
  other: 'Other',
  owner: 'Owner',
  percentage: 'Percentage',
  permissions: 'Permissions',
  plugins_caption: 'Manage system plugins',
  plugins: 'Plugins',
  profiles_caption: 'Manage user and application profiles',
  profiles: 'Profiles',
  progress: 'Progress',
  project_destination: 'Destination project',
  project: 'Project',
  projects_caption: 'Browse tables and variables, create views, import/export data and dictionaries.',
  projects: 'Projects',
  properties: 'Properties',
  referenced_entity_type: 'Referenced entity type',
  refresh: 'Refresh',
  repeatable: 'Repeatable',
  reports_caption: 'Configure and schedule reports',
  reports: 'Reports',
  resources: 'Resources',
  restore_views: 'Restore views',
  rservers_caption: 'Configure R server, grant access to R service',
  rservers: 'R',
  sample_quantiles: 'Sample quantiles',
  sample: 'Sample',
  search_caption: 'Configure search engine, schedule table indexing',
  search: 'Search',
  select_columns: 'Select columns',
  select_dictionary_file_template: 'Use the following Excel template to add new variables or update existing ones:',
  select_dictionary_file: 'Select a dictionary of variables in Excel file format or a View XML file for batch edition of tables and variables.',
  select_files_to_upload: 'Select files to upload',
  select: 'Select',
  size: 'Size',
  skewness: 'Skewness',
  source_code: 'Source code',
  sql: 'SQL',
  start_time: 'Start time',
  status: 'Status',
  std_dev: 'Standard deviation',
  sub_total: 'Sub total',
  sum: 'Sum',
  summary: 'Summary',
  sumsq: 'Sum of squares',
  system: 'System',
  table_references: 'Table references',
  tables_views: 'Tables (views)',
  tables: 'Tables',
  tags: 'Tags',
  tasks_caption: 'Monitor and schedule tasks',
  tasks: 'Tasks',
  taxonomies_caption: 'Manage taxonomies for variable classification with controlled vocabularies',
  taxonomies: 'Taxonomies',
  theoretical_quantiles: 'Theoretical quantiles',
  title: 'Title',
  total: 'Total',
  unit: 'Unit',
  upload: 'Upload',
  user: 'User',
  users_and_groups_caption: 'Add, update, remove users and groups',
  users_and_groups: 'Users and groups',
  users: 'Users',
  value_type: 'Value type',
  value: 'Value',
  values: 'Values',
  variables: 'Variables',
  variance: 'Variance',
  save: 'Save',
  edit: 'Edit',
  edit_script: 'Edit script',
  script: 'Script',
  comment: 'Comment',
  import: 'Import',
  export: 'Export',
  import_file: 'Import from file',
  import_server: 'Import from server',
  import_data: 'Import data',
  select_import_source: 'Select the source of data',
  configure_import_source: 'Configure import source',
  preview_import_source: 'Preview the data',
  select_import_options: 'Select import options',
  back: 'Back',
  continue: 'Continue',
  data_format: 'Data format',
  data_server: 'Data server',
  table_name: 'Table name',
  optional: 'Optional',
  default_value_type: 'Default value type',
  field_separator: 'Field separator',
  quotation_mark: 'Quotation mark',
  from_row: 'From row',
  char_set: 'Character set',
  clear_tasks_confirm: 'No tasks to clear | Are you sure you want to clear this task? (does not apply if task is running) | Are you sure you want to clear these {count} tasks? (running tasks will not be affected)',
  cancel_task_confirm: 'Are you sure you want to cancel this task?',
  clear: 'Clear',
  file_already_exists: 'File already exists',
  unknown_error: 'An unknown error occurred',
  import_limit_hint: 'The maximum number of rows to import. If there is no limit or the limit is 0, all the records will be imported.',
  incremental_import: 'Incremental import',
  incremental_import_hint: 'Import only new or updated data.',
  merge_dictionaries: 'Merge dictionaries',
  merge_dictionaries_hint: 'When a table already exists, the variable characteristics (properties, categories, attributes) will be merged with the imported ones instead of being simply overridden. This allows to preserve the variable annotations on subsequent imports.',
  no_options: 'No options',
  id_column: 'ID column',
  id_column_hint: 'Name of the column that identifies the entity. If not provided, the first column will be selected as the entity IDs provider.',
  opal_url: 'Opal URL',
  token: 'Token',
  personal_access_token: 'Personal access token',
  credentials: 'Credentials',
  username: 'User name',
  password: 'Password',
  auth_method: 'Authentication method',
  export_file: 'Export to file',
  export_data: 'Export data',
  export_tables_text: 'No tables to be exported | One table will be exported | {count} tables will be exported.',
  destination_folder: 'Destination folder',
  id_column_name: 'ID column name',
  id_column_name_hint: 'Name of the column that identifies the entity. If not provided, the default name applies.',
  export_tables_task_created: 'Export tables task created with identifier [ {id} ].',
  bookmarks: 'Favorites',
  bookmark_add: 'Add to favorites',
  bookmark_remove: 'Remove from favorites',
  no_bookmarks: 'No favorites',
  table: 'Table',
  delete_categories_confirm: '- | Are you sure you want to delete this category? | Are you sure you want to delete these {count} categories?',
  labels: 'Labels',
  unique_name_hint: 'The name must be unique.',
  is_missing_hint: 'Indicates that an observational value is missing.',
  edit_category: 'Edit category',
  add_category: 'Add a category',
  add_categories_range: 'Add category range',
  range: 'Range',
  categories_range_hint: 'Use hyphen \'-\' to specify a range of numerical values and comma \',\' to separate names or ranges of names, for example: \'1-4, 9\', or \'A, B, C\'. Existing categories will not be overridden.',
};
