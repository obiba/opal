export default {
  auth: {
    title: 'Identifiez-vous pour commencer une session',
    password: 'Mot de passe',
    signin: 'Se connecter',
    signout: 'Se déconnecter',
    username: 'Nom d\'utilisateur',
    totp_help: 'Ouvrir l\'application mobile d\'authentification et scanner ce code QR pour initialiser votre générateur de code NIP.',
    code: 'Code',
    code_hint: 'Entrer le code à 6 chiffres.',
    validate: 'Valider',
  },
  auth_types: {
    PASSWORD: 'Mot de passe',
    CERTIFICATE: 'Certificat',
    UNRECOGNIZED: 'Non reconnu',
  },
  error: {
    Forbidden: 'L\'operation est interdite',
    NoSuchValueTableInDatasource: 'La table n\'existe pas ou vous n\'y avez pas accès',
    PasswordTooWeak: 'Mot de passe trop faible',
    IllegalArgument: 'Données non valides',
    Conflict: 'Conflit détecté',
    BannedUser: 'Trop d\'erreurs d\'identification, l\'utilisateur {0} est banni pour une durée de {1} secondes',
    InvalidCredentials: 'Nom d\'utilisateur ou mot de passe incorrect, veuillez réessayer',
    TableAlreadyExists: 'La table existe déjà'
  },
  importer: {
    file: {
      csv: 'Ce format s\'attend à ce que le fichier utilise un format "valeurs séparées par des délimiteurs" (le délimiteur par défaut étant la virgule). La première colonne doit représenter les identifiants des participants et les noms de colonnes suivantes identifient les variables.',
      opal: 'Ce format se présente sous la forme d\'un fichier .zip contenant un dossier pour chaque table contenant : le dictionnaire de données complet dans un fichier XML, un fichier de données XML par entité.',
      haven_rds: 'Ce format s\'attend à ce que le fichier soit un fichier RDS valide contenant un objet R de classe tibble. Cette procédure d\'importation nécessite un serveur R fonctionnel avec le package tibble installé.',
      haven_sas: 'Ce format s\'attend à ce que le fichier soit un fichier SAS valide contenant les variables et les données. Cette procédure d\'importation nécessite un serveur R fonctionnel avec les paquets tibble et haven installés.',
      haven_sast: 'Ce format s\'attend à ce que le fichier soit un fichier de transport SAS valide contenant les variables et les données. Cette procédure d\'importation nécessite un serveur R fonctionnel avec les paquets tibble et haven installés.',
      haven_spss: 'Ce format s\'attend à ce que le fichier soit un fichier SPSS valide, éventuellement compressé, contenant les variables et les données. Cette procédure d\'importation nécessite un serveur R fonctionnel avec les paquets tibble et haven installés.',
      haven_stata: 'Ce format s\'attend à ce que le fichier soit un fichier Stata valide contenant les variables et les données. Cette procédure d\'importation nécessite un serveur R fonctionnel avec les paquets tibble et haven installés.',
    },
    server: {
      opal: 'Ce format importe des dictionnaires de variables et des données depuis un Opal distant.',
    },
  },
  validation: {
    user: {
      name_required: 'Le nom est obligatoire',
      password_required: 'Le mot de passe est obligatoire et doit comporter au moins 8 caractères',
      confirm_password_required: 'La confirmation du mot de passe est obligatoire',
      passwords_not_matching: 'Les mots de passe ne correspondent pas',
    },
  },
  main: {
    brand: 'Opal',
    powered_by: 'Propulsé par',
  },
  actions: 'Actions',
  add_a_view: 'Ajouter une vue',
  add_categories_range: 'Ajouter une plage de catégories',
  add_category: 'Ajouter une catégorie',
  add_folder: 'Ajouter un dossier',
  add_table: 'Ajouter une table',
  add_tables: 'Ajouter/mettre à jour des tables',
  add: 'Ajouter',
  administration: 'Administration',
  advanced_options: 'Options avancées',
  all_categories: 'Toutes',
  all_projects: 'Tous',
  apps_caption: 'Gérer les applications externes fournissants des services',
  apps: 'Apps',
  attributes: 'Attributs',
  auth_method: 'Méthode d\'authentification',
  authentication: 'Authentification',
  back: 'Retour',
  bookmark_add: 'Ajouter aux favoris',
  bookmark_remove: 'Retirer des favoris',
  bookmarks: 'Favoris',
  cancel_task_confirm: 'Êtes-vous sûr de vouloir annuler cette tâche ?',
  cancel: 'Annuler',
  categories_range_hint: 'Utilisez le trait d\'union \'-\' pour spécifier une plage de valeurs numériques et la virgule \',\' pour séparer les noms ou les plages de noms, par exemple : \'1-4, 9\' ou \'A, B, C \'. Les catégories existantes ne seront pas modifiées.',
  categories: 'Catégories',
  char_set: 'Jeu de caractères',
  clear_tasks_confirm: 'Aucune tâche à nettoyer | Êtes-vous sûr de vouloir nettoyer cette tâche ? | Êtes-vous sûr de vouloir nettoyer ces {count} tâches ?',
  clear: 'Nettoyer',
  comment: 'Commentaire',
  configure_import_source: 'Configurer la source de données',
  confirm: 'Confirmer',
  content: 'Contenu',
  continue: 'Continuer',
  copy_data: 'Copier les données',
  copy_view: 'Copier la vue',
  copy_incremental_hint: 'Seuls les enregistrements nouveaux ou mises à jour seront copiés.',
  copy_incremental: 'Incrémental',
  copy_nulls_hint: 'Permettre de remplacer une donnée par une valeur nulle.',
  copy_nulls: 'Copier les valeurs nulles',
  copy_tables_data_task_created: 'La tâche de copie des tables a été créée avec l\'identifiant [ {id} ].',
  copy_tables_data_text: 'Aucune table à copier | Une table sera copiée | {count} tables seront copiées.',
  copy_tables_data: 'Copier la table | Copier la table | Copier les tables',
  copy: 'Copier',
  created: 'Date de création',
  credentials: 'Informations d\'identification',
  dashboard: 'Tableau de bord',
  data_access: 'Accès aux données',
  data_analysis: 'Analyse de données',
  data_file: 'Fichier de données',
  data_format: 'Format des données',
  data_management: 'Gestion des données',
  data_server: 'Serveur de données',
  databases_caption: 'Gérer les bases de données pour le stockage, l\'import et l\'export des données',
  databases: 'Base de données',
  datashield_caption: 'Configurer DataSHIELD, accorder l\'accès au service DataSHIELD',
  datashield: 'DataSHIELD',
  date: 'Date',
  default_value_type: 'Type de valeur par défaut',
  delete_categories_confirm: '- | Êtes-vous sûr de vouloir supprimer cette catégorie ? | Êtes-vous sûr de vouloir supprimer ces {count} catégories ?',
  delete_files_confirm: '- | Êtes-vous sûr de vouloir supprimer ce fichier ? | Êtes-vous sûr de vouloir supprimer ces {count} fichiers ?',
  delete_tables_confirm: 'Aucune table à supprimer | Êtes-vous sûr de vouloir supprimer cette table ? | Êtes-vous sûr de vouloir supprimer ces {count} tables ?',
  delete_user_confirm: 'Êtes-vous sûr de vouloir supprimer l\'utilisateur \'{user}\'?',
  delete_variables_confirm: 'Aucune variable à supprimer | Êtes-vous sûr de vouloir supprimer cette variable ? | Êtes-vous sûr de vouloir supprimer ces {count} variables ?',
  delete: 'Supprimer',
  density: 'Densité',
  derivation_script: 'Script de dérivation',
  descriptive_statistics: 'Statistiques descriptives',
  destination_folder: 'Dossier de destination',
  dictionary: 'Dictionnaire',
  disable: 'Désactiver',
  docs: 'Docs',
  documentation_cookbook: 'Documentation et recettes',
  download_dictionaries: 'Télécharger les dictionnaires',
  download_dictionary: 'Télécharger le dictionnaire',
  download_view: 'Télécharger la vue',
  download_views_backup: 'Sauvegarder les vues',
  download: 'Télécharger',
  edit_category: 'Éditer la catégorie',
  edit_script: 'Éditer le script',
  edit_table: 'Éditer la table',
  edit_view: 'Éditer la vue',
  edit: 'Éditer',
  enable: 'Activer',
  enabled: 'Activé',
  encrypt_file_content: 'Encrypter le contenu du fichier',
  encrypt_password_hint: 'Le mot de passe doit avoir au moins 8 caractères.',
  encrypt_password: 'Mot de passe',
  end_time: 'Fin',
  entities: 'Entités',
  entity_type: 'Type d\'entité',
  export_data: 'Exporter les données',
  export_file: 'Exporter vers un fichier',
  export_tables_task_created: 'Tâche d\'exportation de tables créée avec l\'identifiant [ {id} ]',
  export_tables_text: 'Aucune table à exporter | Une table sera exportée | {count} tables seront exportées',
  export: 'Exporter',
  extract: 'Extraire',
  field_separator: 'Séparateur de champ',
  file_already_exists: 'Ce fichier existe déjà',
  file_selection: 'Sélection de fichier',
  file_system: 'Système',
  files_caption: 'Gérer le système de fichiers, téléverser et télécharger des fichiers',
  files: 'Fichiers',
  frequencies: 'Fréquences',
  frequency: 'Fréquence',
  from_row: 'A partir de la ligne',
  from_tables: 'Depuis les tables',
  full_name: 'Nom complet',
  full_summary: 'Sommaire complet',
  general_settings_caption: 'Gérer la configuration générale du serveur',
  general_settings: 'Paramètres généraux',
  groups_hint: 'Sélectionnez un groupe ou saisissez un nouveau nom et appuyez sur \'Entrée\'.',
  groups_info: 'Les groupes ne peuvent être définis que par l\'intermédiaire des utilisateurs. La suppression d\'un groupe supprime les utilisateurs de ce groupe.',
  groups: 'Groupes',
  histogram: 'Histogramme',
  id_column_hint: 'Nom de la colonne des identifiants uniques. Si non fournie, la première colonne sera utilisée pour déterminer l\'identifiant de l\'entité.',
  id_column_name_hint: 'Nom de la colonne qui identifie l\'entité. S\'il n\'est pas fourni, le nom par défaut s\'applique',
  id_column_name: 'Nom de la colonne ID',
  id_column: 'Colonne ID',
  id_mappings_caption: 'Gérer les identifiants',
  id_mappings: 'Identifiants',
  id: 'ID',
  identity_providers_caption: 'Gérer les fournisseurs d\'identité OpenID Connect',
  identity_providers: 'Fournisseurs d\'identité',
  import_data_task_created: 'La tâche d\'importation des données a été créée avec l\'identifiant [ {id} ].',
  import_data: 'Importer des données',
  import_file: 'Importer depuis un fichier',
  import_limit_hint: 'Le nombre maximum de lignes à importer. Si aucune limite n\'est définie ou si la limite est 0, tous les enregistrements seront importés.',
  import_server: 'Importer depuis un serveur',
  import: 'Importer',
  incremental_import_hint: 'Importer uniquement les données nouvelles ou mises à jour',
  incremental_import: 'Importation incrémentale',
  index: 'Index',
  intervals: 'Intervalles',
  is_missing_hint: 'Indique qu\'une valeur d\'observation est manquante.',
  is_missing: 'Manquante',
  jvm_caption: 'Monitorer la machine virtuelle Java',
  jvm: 'Machine virtuelle Java',
  kurtosis: 'Kurtosis',
  label: 'Libellé',
  labels: 'Libellés',
  last_update: 'Dernière mise à jour',
  limit: 'Limite',
  locale: 'Locale',
  max: 'Max',
  mean: 'Moyenne',
  median: 'Médiane',
  merge_dictionaries_hint: 'Lorsqu\'une table existe déjà, les caractéristiques des variables (propriétés, catégories, attributs) seront fusionnées avec celles importées au lieu d\'être simplement remplacées. Cela permet de conserver les annotations des variables lors des importations ultérieures.',
  merge_dictionaries: 'Fusionner les dictionnaires',
  merge_variables_hint: 'Si sélectionné, les variables avec le même nom seront fusionnées (les propriétés, les catégories et les attributs seront ajoutés ou mis à jour, pas de suppression). Sinon, les variables fournies remplaceront les existantes.',
  merge_variables: 'Fusionner les variables',
  message: 'Message',
  messages: 'Messages',
  mime_type: 'Type mime',
  min: 'Min',
  missings: 'Manquantes',
  move_down: 'Descendre',
  move_up: 'Monter',
  my_profile: 'Mon profil',
  name: 'Nom',
  namespace: 'Espace de noms',
  new_name: 'Nouveau nom',
  no_bookmarks: 'Aucun favori',
  no_options: 'Aucune option',
  non_missings: 'Non manquantes',
  normal_distribution: 'Distribution normale',
  normal: 'Normal',
  not_empty: 'Non vide',
  occurrence_group: 'Groupe d\'occurrence',
  opal_url: 'URL Opal',
  optional: 'Optionnel',
  other_links: 'Autres liens',
  other: 'Autre',
  owner: 'Propriétaire',
  password_confirm: 'Confirmer le mot de passe',
  password_hint: '{\'Le mot de passe doit comporter au moins 8 caractères, dont un chiffre, une lettre majuscule, une lettre minuscule, un caractère spécial (par exemple, @#$%^&+= !) et aucun espace blanc.\'}',
  password: 'Mot de passe',
  percentage: 'Pourcentage',
  permissions: 'Permissions',
  personal_access_token: 'Jeton d\'accès personnel',
  plugins_caption: 'Gérer les greffons du système',
  plugins: 'Greffons',
  preview_import_source: 'Prévisualiser les données',
  profiles_caption: 'Gérer les profils',
  profiles: 'Profils',
  progress: 'Progression',
  project_destination: 'Projet de destination',
  project: 'Projet',
  projects_caption: 'Parcourir les tables et les variables, créer des vues, importer/exporter des données et des dictionnaires',
  projects: 'Projets',
  properties: 'Propriétés',
  quotation_mark: 'Marque de citation',
  range: 'Plage',
  referenced_entity_type: 'Type d\'entité référencée',
  refresh: 'Rafraîchir',
  repeatable: 'Répétable',
  reports_caption: 'Configurer et planifier des rapports',
  reports: 'Rapports',
  resources: 'Ressources',
  restore_views: 'Restaurer des vues',
  rservers_caption: 'Configurer les serveurs R, accorder l\'accès au service R',
  rservers: 'R',
  sample_quantiles: 'Quantiles d\'échantillon',
  sample: 'Échantillon',
  save: 'Sauvegarder',
  script: 'Script',
  search_caption: 'Configurer les paramètres de recherche',
  search: 'Recherche',
  select_columns: 'Sélectionner des colonnes',
  select_dictionary_file_template: 'Utilisez le modèle Excel suivant pour ajouter de nouvelles variables ou mettre à jour des variables existantes:',
  select_dictionary_file: 'Sélectionner un fichier de dictionnaire de variables au format Excel ou un fichier XML de vue pour l\'édition en lot des tables et des variables.',
  select_files_to_upload: 'Sélectionner les fichiers à téléverser',
  select_import_options: 'Sélectionner les options d\'import',
  select_import_source: 'Sélectionner la source de données',
  select: 'Sélectionner',
  signin_with: 'S\'identifier avec {provider}',
  size: 'Taille',
  skewness: 'Skewness',
  source_code: 'Code source',
  sql: 'SQL',
  start_time: 'Début',
  status: 'Statut',
  std_dev: 'Écart type',
  sub_total: 'Sous-total',
  sum: 'Somme',
  summary: 'Sommaire',
  sumsq: 'Somme des carrés',
  system: 'Système',
  table_name: 'Nom de la table',
  table_references: 'Tables référencées',
  table: 'Table',
  tables_views: 'Tables (vues)',
  tables: 'Tables',
  tags: 'Étiquettes',
  tasks_caption: 'Monitorer et planifier des tâches',
  tasks: 'Tâches',
  taxonomies_caption: 'Gérer les taxonomies et les vocabulaires contrôlés',
  taxonomies: 'Taxonomies',
  theoretical_quantiles: 'Quantiles théoriques',
  title: 'Titre',
  token: 'Jeton',
  total: 'Total',
  unique_name_hint: 'Le nom doit être unique.',
  unit: 'Unité',
  unknown_error: 'Une erreur inconnue est survenue',
  update: 'Mise à jour',
  upload: 'Téléverser',
  user_add_with_crt: 'Ajouter un utilisateur avec un certificat',
  user_add_with_pwd: 'Ajouter un utilisateur avec un mot de passe',
  user_add: 'Ajouter un utilisateur',
  user_delete: 'Supprimer l\'utilisateur',
  user_disable: 'Désactiver l\'utilisateur',
  user_edit: 'Modifier l\'utilisateur',
  user_enable: 'Activer l\'utilisateur',
  user: 'Utilisateur',
  username: 'Nom d\'utilisateur',
  users_add: 'Ajouter un utilisateur',
  users_and_groups_caption: 'Ajouter, mettre à jour, supprimer des utilisateurs et des groupes',
  users_and_groups: 'Utilisateurs et groupes',
  users_filter_placeholder: 'Filtrer les utilisateurs par nom, groupe ou type d\'authentification...',
  users_info: 'Les utilisateurs peuvent se connecter à l\'aide d\'un mot de passe ou de manière programmatique en fournissant un certificat dans une connexion sécurisée (HTTPS).',
  users: 'Utilisateurs',
  value_type: 'Type de valeur',
  value: 'Valeur',
  values: 'Valeurs',
  variables: 'Variables',
  variance: 'Variance',
  r: 'R',
  r_servers: 'Serveurs R',
  r_servers_info: 'Les serveurs R sont regroupés par clusters. Dans chaque cluster, tous les serveurs R sont considérés comme interchangeables, c\'est-à-dire que la charge d\'activité sera répartie sur l\'un ou l\'autre serveur lors de la création d\'une session R.',
  r_cluster: 'Cluster',
  r_clusters_count: 'Aucun cluster de serveurs R | 1 cluster de serveurs R | {count} clusters de serveurs R',
  r_version: 'Version R',
  r_system: 'coeurs : {cores}, mémoire libre : {memory}',
  r_sessions: 'Sessions R',
  r_sessions_info: 'Surveillance de l\'activité du serveur R : chaque session R est une unité de calcul démarrée par les utilisateurs R/DataSHIELD ou par des processus internes. Les sessions R sans activité pendant un certain temps seront automatiquement terminées.',
  r_workspaces: 'Espaces de travail R',
  r_workspaces_info: 'Stockage des espaces de travail R : chaque espace de travail R/DataSHIELD enregistré contient l\'image et les fichiers de la session (le cas échéant). Ceux-ci peuvent être restaurés autant de fois que nécessaire à la demande de l\'utilisateur.',
  servers: 'Serveurs',
  server: 'Serveur',
  packages: 'Paquets',
  r_sessions_counts: '{count} ({active} active)',
  version: 'Version',
  built: 'Built',
  libpath: 'Libpath',
  download_logs: 'Télécharger les logs',
  start: 'Démarrer',
  stop: 'Arrêter',
  profile: 'Profile',
  context: 'Context',
  started: 'Début',
  last_access: 'Dernier accès',
  waiting: 'En attente',
  busy: 'Occupé',
  unrecognized: 'Non reconnu',
  terminate: 'Terminer',
  terminate_r_sessions_confirm: '- | Êtes-vous sûr de vouloir terminer cette session R ? | Êtes-vous sûr de vouloir terminer ces {count} sessions R ?',
};
