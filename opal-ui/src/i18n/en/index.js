export default {
  '2fa': '2FA',
  app_configuration: {
    name_hint: 'A name for the application that represents your organization.',
    public_url_hint: 'Public base URL of the server that will be used when sending notification emails on report generation.',
    logout_url_hint: 'Optional redirection URL after user has logged out.',
    languages_hint: 'The languages used to describe the dictionaries.',
    default_charset_hint: 'The default char set used when prompting dictionnaries data.',
    enforced_2fa_hint: 'Enforce users to set up their two-factor authentication secret. Does not apply to users defined in external user registries.'
  },
  auth: {
    title: 'Sign in to start your session',
    password: 'Password',
    signin: 'Sign in',
    signout: 'Sign out',
    username: 'User name',
    totp_help: 'Open the authenticator mobile app and scan this QR code to initialise your PIN code generator.',
    code: 'Code',
    code_hint: 'Enter the 6-digits PIN code.',
    validate: 'Validate',
  },
  auth_types: {
    PASSWORD: 'Password',
    CERTIFICATE: 'Certificate',
    UNRECOGNIZED: 'Unrecognized',
  },
  db: {
    data_databases: 'Data databases',
    data_databases_info: 'Data databases are used to store data tables and variables.',
    id_database: 'Identifiers database',
    id_database_info: 'Identifiers database is used to store identifiers mappings.',
    name_hint: 'The name of the database must be unique.',
    usage_hint: 'Determine if this database will be proposed at import/export time or as a project\'s data storage.',
    driver: 'Driver',
    driver_hint: 'Specifies which driver should be used to connect to this database.',
    batch_size: 'Batch size',
    batch_size_hint: 'The number of rows to be processed in a batch.',
    register_sqldb: 'Register SQL DB',
    register_mongodb: 'Register MongoDB',
    unregister: 'Unregister',
    unregister_confirm: 'Are you sure you want to unregister this database \'{name}\'? Note that the data WILL NOT be deleted.',
    test_error: 'Connection test failed',
    test_success: 'Connection test succeeded',
    in_use: 'In use',
    default_entity_type: 'Default entity type',
    default_entity_type_hint: 'Entity type to be used if no variables description tables is available.',
    default_id_column: 'Entity Identifier Column',
    default_id_column_hint: 'The column name for identifying the entity. Make sure it will not conflict with a variable column name.',
    default_updated_column: 'Update Timestamp Column',
    default_updated_column_hint: 'The column name for entity values last update date time, required for performing incremental imports. Make sure it will not conflict with a variable column name.',
    use_metadata_tables: 'With variables description tables',
    use_metadata_tables_hint: 'Export data dictionnaries to metadata tables.',
    save_error: 'Error saving database',
  },
  datashield: {
    access_not_restricted: 'Any DataSHIELD user can access this profile.',
    access_restricted: 'Only permitted DataSHIELD users can access this profile.',
    aggregate_methods_info: 'The aggregation methods are used by DataSHIELD in order to compile individual data. The same aggregation methods must be defined in each DataSHIELD server that will be involved in a computation process. Each aggregation method is identified by a name that will be used from the DataSHIELD client.',
    assign_methods_info: 'The assign methods are used by DataSHIELD in order to transform individual data on server side. The same assign methods must be defined in each DataSHIELD server that will be involved in a computation process. Each assign method is identified by a name that will be used from the DataSHIELD client.',
    audit: 'Audit',
    audit_info: 'Datashield activity logs can be downloaded.',
    caption: 'Configure DataSHIELD, grant access to DataSHIELD service',
    delete_methods_confirm: '- | Are you sure you want to delete this DataSHIELD method? | Are you sure you want to delete these {count} DataSHIELD methods?',
    delete_options_confirm: '- | Are you sure you want to delete this R option? | Are you sure you want to delete these {count} R options?',
    download_all_logs: 'Download all logs',
    download_latest_logs: 'Download latest logs',
    method_func_hint: 'The R server side function that will be effectively called. Make sure there is no ambiguity by specifying the R package in which it is defined.',
    method_name_hint: 'The name of the DataSHIELD method name must be unique. This is the name of the allowed function that can be called by the DataSHIELD client.',
    method_script_hint: 'A custom R function implementation that will be effectively called.',
    option_name_hint: 'R option name.',
    option_value_hint: 'R option value. R syntax is expected: for instance use quotes for string values.',
    options_info: 'The R options are applied at DataSHIELD R session creation and affect the behavior of some of the DataSHIELD methods.',
    packages_info: 'DataSHIELD packages are a set of R packages that provide the DataSHIELD functionalities. They are installed in the R servers and are used by the DataSHIELD clients to perform the analysis.',
    profile_cluster_hint: 'The R servers cluster that will be used by this DataSHIELD profile.',
    profile_delete_confirm: 'Are you sure you want to delete this DataSHIELD profile?',
    profile_missing_cluster: 'The R servers cluster for this DataSHIELD profile does not exist.',
    profile_name_hint: 'The DataSHIELD profile name must be unique. This is the name that will be used by the DataSHIELD client to start a DataSHIELD session.',
    profile_settings_help: 'The DataSHIELD profile settings are used to define the aggregate/assign methods that are allowed to be called by a DataSHIELD and the R options that will be applied to the server-side DataSHIELD R session.',
    profile_access_toggle: 'Restrict access to this profile',
    profile_status_toggle: 'Activate or deactivate profile',
    profiles_info: 'A DataSHIELD profile is associated to a R servers cluster: the methods and options are extracted from the installed DataSHIELD packages settings and can be amended. ',
    settings_init_help: 'Select which DataSHIELD packages will get their methods and options applied to this profile.',
    settings_init: 'Initialize the DataSHIELD profile settings with selected DataSHIELD packages',
  },
  error: {
    Forbidden: 'This operation is forbidden',
    NoSuchValueTableInDatasource: 'The table does not exist or you do not have access to it',
    PasswordTooWeak: 'Password is too weak',
    IllegalArgument: 'Invalid data',
    Conflict: 'Conflicting entry detected',
    BannedUser: 'Too many sign in failures, user {0} is banned for {1} seconds',
    InvalidCredentials: 'Invalid credentials, please try again',
    TableAlreadyExists: 'The table already exists'
  },
  importer: {
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
    },
  },
  acls: {
    // global permissions
    PROJECT_ADD: {
      label: 'Add projects',
      description: 'Add new projects and therefore can import/export data in the context of the project.',
    },
    PROJECT_All: {
      label: 'Administrate projects',
      description: 'Full access to project.',
    },
    SYSTEM_ALL: {
      label: 'Administrate system',
      description: 'Access to all resources and actions in the system (trusted users only!).',
    },
    // r service
    R_USE: {
      label: 'Use R service',
      description: 'Can push accessible data to an R server session and can execute ANY R commands on them (trusted users only!).',
    },
    // datashield service
    DATASHIELD_USE: {
      label: 'Use DataSHIELD service',
      description: 'Can push accessible data to an R server session and can execute limited R commands on them.',
    },
    DATASHIELD_ALL: {
      label: 'Administrate DataSHIELD service',
      description: 'Administrate DataSHIELD settings.',
    },
    // datashield profile
    DATASHIELD_PROFILE_USE: {
      label: 'Use DataSHIELD profile',
      description: 'Can push accessible data to R and can execute limited R commands on them, in the context of this DataSHIELD profile.',
    },
    // datasource permissions
    DATASOURCE_VIEW: {
      label: 'View dictionary and values of all tables',
      description: 'Read access to project tables, including individual values.',
    },
    TABLE_ADD: {
      label: 'Add table',
      description: 'Add tables or views.',
    },
    DATASOURCE_ALL: {
      label: 'Administrate tables',
      description: 'Full access to project tables, including individual values.',
    },
    // table permissions
    TABLE_READ: {
      label: 'View table dictionary and summaries',
      description: 'View dictionary and summaries (no access to individual values).',
    },
    TABLE_VALUES: {
      label: 'View table dictionary and values',
      description: 'View dictionary with access to individual values.',
    },
    TABLE_EDIT: {
      label: 'Edit table dictionary and view summaries',
      description: 'Edit dictionary and view values summary (no access to individual values).',
    },
    TABLE_VALUES_EDIT: {
      label: 'Edit table dictionary and view values',
      description: 'Edit dictionary and view individual values.',
    },
    TABLE_ALL: {
      label: 'Administrate table',
      description: 'Full access to the table, including edition of the dictionary and individual values.',
    },
    // variable permissions
    VARIABLE_READ: {
      label: 'View variable with summary',
      description: 'View variable description and values summary (no access to individual values).',
    },
    // file permissions
    FILES_READ: {
      label: 'Access files',
      description: 'Access files without the permission to add or delete.',
    },
    FILES_SHARE: {
      label: 'Add and access files',
      description: 'Add and access files without the permission to delete.',
    },
    FILES_ALL: {
      label: 'Administrate files',
      description: 'Full access to files.',
    },
    // report permissions
    REPORT_TEMPLATE_ALL: {
      label: 'REPORT_TEMPLATE_ALL',
      description: 'REPORT_TEMPLATE_ALL',
    },
    REPORT_TEMPLATE_READ: {
      label: 'REPORT_TEMPLATE_ALL',
      description: 'REPORT_TEMPLATE_ALL',
    },
    // resource permissions
    RESOURCE_ALL: {
      label: 'RESOURCE_ALL',
      description: 'RESOURCE_ALL',
    },
    RESOURCES_ALL: {
      label: 'RESOURCES_ALL',
      description: 'RESOURCES_ALL',
    },
    RESOURCE_VIEW: {
      label: 'RESOURCE_VIEW',
      description: 'RESOURCE_VIEW',
    },
    RESOURCES_VIEW: {
      label: 'RESOURCES_VIEW',
      description: 'RESOURCES_VIEW',
    },
    // database permissions
    DATABASES_ALL: {
      label: 'DATABASES_ALL',
      description: 'DATABASES_ALL',
    },
    // vcf permissions
    DATABASES_ALLVCF_STORE_ALL: {
      label: 'VCF_STORE_ALL',
      description: 'VCF_STORE_ALL',
    },
    VCF_STORE_VALUES: {
      label: 'VCF_STORE_VALUES',
      description: 'VCF_STORE_VALUES',
    },
    VCF_STORE_VIEW: {
      label: 'VCF_STORE_VIEW',
      description: 'VCF_STORE_VIEW',
    },
  },
  validation: {
    user: {
      name_required: 'Name is required',
      password_required: 'Password is required and must be at least 8 characters long',
      certificate_required: 'Certificate is required',
      confirm_password_required: 'Confirm password is required',
      passwords_not_matching: 'Passwords do not match',
    },
  },
  main: {
    brand: 'Opal',
    powered_by: 'Powered by',
  },
  r: {
    cluster: 'Cluster',
    clusters_count: 'No cluster of R servers | 1 cluster of R servers | {count} clusters of R servers',
    packages_warn: 'This operation may seem convenient but it is not recommended in a production environment. It is best to deploy the R server using a validated Docker image to ensure reproducibility of the R analysis environment.',
    servers_info: 'R servers are grouped by clusters. In each cluster all the R servers are considered to be interchangeable, i.e. a load balancer can select one or the other on R session creation.',
    servers: 'R servers',
    sessions_counts: '{count} ({active} active)',
    sessions_info: 'R server activity monitoring: each R session is a computation unit started by R/DataSHIELD users or by internal processes. R sessions without activity for a while will be automatically terminated.',
    sessions: 'R sessions',
    system: '{cores} cores, {memory} free memory',
    version: 'R version',
    workspaces_info: 'R workspaces storage: each saved R/DataSHIELD workspace contains the session\'s image and files (if any). These can be restored any number of times on user demand.',
    workspaces: 'R workspaces',
  },
  actions: 'Actions',
  add_a_view: 'Add view',
  add_categories_range: 'Add category range',
  add_category: 'Add a category',
  add_folder: 'Add folder',
  add_method: 'Add method',
  add_option: 'Add option',
  add_permission: 'Add permission',
  add_group_permission: 'Add group permission',
  add_user_permission: 'Add user permission',
  add_profile: 'Add profile',
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
  auth_method: 'Authentication method',
  authentication: 'Authentication',
  back: 'Back',
  bookmark_add: 'Add to favorites',
  bookmark_remove: 'Remove from favorites',
  bookmarks: 'Favorites',
  built: 'Built',
  busy: 'Busy',
  cancel_task_confirm: 'Are you sure you want to cancel this task?',
  cancel: 'Cancel',
  categories_range_hint: 'Use hyphen \'-\' to specify a range of numerical values and comma \',\' to separate names or ranges of names, for example: \'1-4, 9\', or \'A, B, C\'. Existing categories will not be overridden.',
  categories: 'Categories',
  certificate_placeholder: 'Paste your certificate here',
  certificate: 'Certificate',
  char_set: 'Character set',
  clear_tasks_confirm: 'No tasks to clear | Are you sure you want to clear this task? (does not apply if task is running) | Are you sure you want to clear these {count} tasks? (running tasks will not be affected)',
  clear: 'Clear',
  code: 'Code',
  comment: 'Comment',
  configuration: 'Configuration',
  configure_import_source: 'Configure import source',
  confirm: 'Confirm',
  content: 'Content',
  context: 'Context',
  continue: 'Continue',
  copy_data: 'Copy data',
  copy_incremental_hint: 'Whether only new or updated data will be copied.',
  copy_incremental: 'Incremental',
  copy_nulls_hint: 'Whether a data can be overridden by a null value.',
  copy_nulls: 'Copy null values',
  copy_tables_data_task_created: 'Copy tables data task created with identifier [ {id} ].',
  copy_tables_data_text: 'No tables to be copied | One table will be copied | {count} tables will be copied.',
  copy_tables_data: 'Copy table data | Copy table data | Copy tables data',
  copy_view: 'Copy view',
  copy: 'Copy',
  created: 'Created on',
  credentials: 'Credentials',
  dashboard: 'Dashboard',
  data_access: 'Data access',
  data_analysis: 'Data analysis',
  data_file: 'Data file',
  data_format: 'Data format',
  data_management: 'Data management',
  data_server: 'Data server',
  databases_caption: 'Manage identifiers database and data databases for storage, import and export',
  databases: 'Databases',
  date: 'Date',
  default_value_type: 'Default value type',
  delete_categories_confirm: '- | Are you sure you want to delete this category? | Are you sure you want to delete these {count} categories?',
  delete_files_confirm: '- | Are you sure you want to delete this file? | Are you sure you want to delete these {count} files?',
  delete_group_confirm: 'Are you sure you want to delete group \'{group}\'?',
  delete_r_package_confirm: 'The deletion of an R package may not succeed if it is installed in a folder that is not writable. Are you sure you want to delete the R package "{name}"?',
  delete_r_workspaces_confirm: '- | Are you sure you want to delete this R workspace? | Are you sure you want to delete these {count} R workspaces?',
  delete_tables_confirm: 'No tables to delete | Are you sure you want to delete this table? | Are you sure you want to delete these {count} tables?',
  delete_permission_confirm: 'Are you sure you want to delete permission of {principal}?',
  delete_profile_acl_confirm: 'Are you sure you want to revoke \'{permission}\' from \'{resource}\' resource?',
  delete_profile_confirm: 'Are you sure you want to delete profile \'{profile}\'?',
  delete_profiles_confirm: 'Are you sure you want to delete  profile \'{profile}\'?| Are you sure you want to delete these {count} profiles?',
  delete_profiles_selected: 'Delete selected profiles',
  delete_user_confirm: 'Are you sure you want to delete user \'{user}\'?',
  delete_variables_confirm: 'No variables to delete | Are you sure you want to delete this variable? | Are you sure you want to delete these {count} variables?',
  delete: 'Delete',
  density: 'Density',
  derivation_script: 'Derivation script',
  descriptive_statistics: 'Descriptive statistics',
  destination_folder: 'Destination folder',
  dictionary: 'Dictionary',
  disable: 'Disable',
  docs: 'Docs',
  documentation_cookbook: 'Documentation and cookbook',
  download_dictionaries: 'Download dictionaries',
  download_dictionary: 'Download dictionary',
  download_logs: 'Download logs',
  download_view: 'Download view',
  download_views_backup: 'Backup views',
  download: 'Download',
  edit_category: 'Edit category',
  edit_permission: 'Edit permission',
  edit_script: 'Edit script',
  edit_table: 'Edit table',
  edit_view: 'Edit view',
  edit: 'Edit',
  enable: 'Enable',
  enabled: 'Enabled',
  encrypt_file_content: 'Encrypt file content',
  encrypt_password_hint: 'The password must have at least 8 characters.',
  encrypt_password: 'Password',
  end_time: 'End time',
  entities: 'Entities',
  entity_type: 'Entity type',
  export_data: 'Export data',
  export_file: 'Export to file',
  export_tables_task_created: 'Export tables task created with identifier [ {id} ].',
  export_tables_text: 'No tables to be exported | One table will be exported | {count} tables will be exported.',
  export: 'Export',
  extract: 'Extract',
  field_separator: 'Field separator',
  file_already_exists: 'File already exists',
  file_selection: 'File selection',
  file_system: 'System',
  files_caption: 'Manage files and folders, upload and download files',
  files: 'Files',
  frequencies: 'Frequencies',
  frequency: 'Frequency',
  from_row: 'From row',
  from_tables: 'From tables',
  full_name: 'Full name',
  full_summary: 'Full summary',
  function: 'Function',
  general_settings_caption: 'Manage general server configuration',
  general_settings: 'General Settings',
  gh_org_hint: 'Example: datashield',
  gh_org: 'Organization or user',
  gh_ref_hint: 'Branch, tag or commit, default is "master"',
  gh_ref: 'Reference',
  gh_repo_hint: 'Example: dsBase',
  gh_repo: 'Repository',
  group: 'Group',
  groups_hint: 'Select a group or type a new name and press \'ENTER\'.',
  groups_info: 'Groups can only be defined through users. Removing a group removes users from this group.',
  groups: 'Groups',
  help: 'Help',
  histogram: 'Histogram',
  id_column_hint: 'Name of the column that identifies the entity. If not provided, the first column will be selected as the entity IDs provider.',
  id_column_name_hint: 'Name of the column that identifies the entity. If not provided, the default name applies.',
  id_column_name: 'ID column name',
  id_column: 'ID column',
  id_mappings_caption: 'Manage identifiers mappings',
  id_mappings: 'Identifiers Mappings',
  id: 'ID',
  identity_providers_caption: 'Manage the OpenID Connect providers',
  identity_providers: 'Identity Providers',
  import_data_task_created: 'Import data task created with identifier [ {id} ].',
  import_data: 'Import data',
  import_file: 'Import from file',
  import_limit_hint: 'The maximum number of rows to import. If there is no limit or the limit is 0, all the records will be imported.',
  import_server: 'Import from server',
  import: 'Import',
  incremental_import_hint: 'Import only new or updated data.',
  incremental_import: 'Incremental import',
  index: 'Index',
  initialize: 'Initialize',
  install_action: 'Install',
  install_r_package_task_created: 'Install R package task created with identifier [ {id} ].',
  install_r_package: 'Install R package',
  install: 'Install',
  intervals: 'Intervals',
  is_missing_hint: 'Indicates that an observational value is missing.',
  is_missing: 'Missing',
  jvm_caption: 'Monitor Java virtual machine',
  jvm: 'Java Virtual Machine',
  kurtosis: 'Kurtosis',
  label: 'Label',
  labels: 'Labels',
  last_access: 'Last access',
  last_update: 'Last update',
  libpath: 'Libpath',
  limit: 'Limit',
  locale: 'Locale',
  max: 'Max',
  mean: 'Mean',
  median: 'Median',
  merge_dictionaries_hint: 'When a table already exists, the variable characteristics (properties, categories, attributes) will be merged with the imported ones instead of being simply overridden. This allows to preserve the variable annotations on subsequent imports.',
  merge_dictionaries: 'Merge dictionaries',
  merge_variables_hint: 'If selected, variable with same name will be merged (properties, categories and attributes will be added or updated, no deletion). Else the provided variables will replace the existing ones.',
  merge_variables: 'Merge variables',
  message: 'Message',
  messages: 'Messages',
  mime_type: 'Mime type',
  min: 'Min',
  missings: 'Missings',
  move_down: 'Move down',
  move_up: 'Move up',
  my_profile: 'My profile',
  name: 'Name',
  namespace: 'Namespace',
  new_name: 'New name',
  no_bookmarks: 'No favorites',
  no_options: 'No options',
  non_missings: 'Non missings',
  normal_distribution: 'Normal distribution',
  normal: 'Normal',
  not_empty: 'Not empty',
  occurrence_group: 'Occurrence group',
  opal_url: 'Opal URL',
  optional: 'Optional',
  other_links: 'Other links',
  other: 'Other',
  owner: 'Owner',
  package_manager: 'Package manager',
  package: 'Package',
  packages: 'Packages',
  password_confirm: 'Confirm password',
  password_hint: "{'The password must be at least 8 characters long, including one digit, one uppercase letter, one lowercase letter, one special character (e.g., @#$%^&+=!), and no white space.'}",
  password: 'Password',
  percentage: 'Percentage',
  permission: 'Permission',
  permissions: 'Permissions',
  personal_access_token: 'Personal access token',
  plugins_caption: 'Manage system plugins',
  plugins: 'Plugins',
  preview_import_source: 'Preview the data',
  profile: 'Profile',
  profile_acls_info: 'List of {principal}\'s permissions per resource.',
  profile_acls: 'User Permissions',
  profiles_caption: 'Manage user and application profiles',
  profiles_info: 'Each user who logged in has a profile. A realm is the user directory in which a user is defined. Logging in from different realm with the same user name is forbidden for security reasons. If removed, the user profile will be automatically recreated when the user logs in.',
  profiles: 'Profiles',
  progress: 'Progress',
  project_destination: 'Destination project',
  project: 'Project',
  projects_caption: 'Browse tables and variables, create views, import/export data and dictionaries.',
  projects: 'Projects',
  properties: 'Properties',
  quotation_mark: 'Quotation mark',
  range: 'Range',
  realm: 'Realm',
  referenced_entity_type: 'Referenced entity type',
  refresh: 'Refresh',
  repeatable: 'Repeatable',
  reports_caption: 'Configure and schedule reports',
  reports: 'Reports',
  resources: 'Resources',
  restore_views: 'Restore views',
  rservers_caption: 'Configure R server, grant access to R service',
  rservers: 'R',
  r_func: 'Function',
  r_script: 'Script',
  sample_quantiles: 'Sample quantiles',
  sample: 'Sample',
  save: 'Save',
  script: 'Script',
  search_caption: 'Configure search engine, schedule table indexing',
  search: 'Search',
  select_columns: 'Select columns',
  select_dictionary_file_template: 'Use the following Excel template to add new variables or update existing ones:',
  select_dictionary_file: 'Select a dictionary of variables in Excel file format or a View XML file for batch edition of tables and variables.',
  select_files_to_upload: 'Select files to upload',
  select_import_options: 'Select import options',
  select_import_source: 'Select the source of data',
  select: 'Select',
  server: 'Server',
  servers: 'Servers',
  settings: 'Settings',
  signin_with: 'Sign in with {provider}',
  size: 'Size',
  skewness: 'Skewness',
  source_code: 'Source code',
  sql: 'SQL',
  start_time: 'Start time',
  start: 'Start',
  started: 'Started',
  status: 'Status',
  std_dev: 'Standard deviation',
  stop: 'Stop',
  sub_total: 'Sub total',
  submit: 'Submit',
  sum: 'Sum',
  summary: 'Summary',
  sumsq: 'Sum of squares',
  system: 'System',
  table_name: 'Table name',
  table_references: 'Table references',
  table: 'Table',
  tables_views: 'Tables (views)',
  tables: 'Tables',
  tags: 'Tags',
  tasks_caption: 'Monitor and schedule tasks',
  tasks: 'Tasks',
  taxonomies_caption: 'Manage taxonomies for variable classification with controlled vocabularies',
  taxonomies: 'Taxonomies',
  terminate_r_sessions_confirm: '- | Are you sure you want to terminate this R session? | Are you sure you want to terminate these {count} R sessions?',
  terminate: 'Terminate',
  theoretical_quantiles: 'Theoretical quantiles',
  title: 'Title',
  token: 'Token',
  total: 'Total',
  type: 'Type',
  unique_name_hint: 'The name must be unique.',
  unit: 'Unit',
  unknown_error: 'An unknown error occurred',
  unrecognized: 'Unrecognized',
  update_action: 'Update',
  update_all_r_packages_note: 'This operation will update all R packages to the latest version available on CRAN. This is a long time running task. If any error happens, look for possible reasons (system dependency missing, network connection failure etc.) by downloading the R server log.',
  update_all_r_packages_task_created: 'Update all R packages task created with identifier [ {id} ].',
  update_all_r_packages: 'Update all R packages',
  update_method: 'Update method',
  update_option: 'Update option',
  update: 'Update',
  upload: 'Upload',
  user_add_with_crt: 'Add user with certificate',
  user_add_with_pwd: 'Add user with password',
  user_add: 'Add user',
  user_delete: 'Delete user',
  user_disable: 'Disable user',
  user_edit: 'Edit user',
  user_enable: 'Enable user',
  user: 'User',
  username: 'User name',
  user_profiles: 'User Profiles',
  users_and_groups_caption: 'Add, update, remove users and groups',
  users_and_groups: 'Users and groups',
  users_filter_placeholder: 'Filter users by name, group or authentication type...',
  users_info: 'Users can login using a password or programmatically by providing a certificate in a secured connection (HTTPS).',
  users: 'Users',
  value_type: 'Value type',
  value: 'Value',
  values: 'Values',
  variables: 'Variables',
  variance: 'Variance',
  version: 'Version',
  waiting: 'Waiting',
  public_url: 'Public URL',
  logout_url: 'URL after logout',
  laguages: 'Languages',
  default_charset: 'Default charset',
  enforced_2fa: 'Enforced 2FA',
  usage: 'Usage',
  storage: 'Storage',
  register: 'Register',
  default_storage: 'Default storage',
  options: 'Options',
  example: 'Example: {text}',
  test: 'Test',
};
