export default {
  auth: {
    password: 'Mot de passe',
    signin: 'Se connecter',
    signout: 'Se déconnecter',
    username: "Nom d'utilisateur",
  },
  error: {
    Forbidden: 'L\'operation est interdite',
    NoSuchValueTableInDatasource: 'La table n\'existe pas ou vous n\'y avez pas accès',
  },
  importer : {
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
    }
  },
  main: {
    brand: 'Opal',
    powered_by: 'Propulsé par',
  },
  add_a_view: 'Ajouter une vue',
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
  cancel: 'Annuler',
  categories: 'Catégories',
  confirm: 'Confirmer',
  content: 'Contenu',
  copy_incremental_hint: 'Seuls les enregistrements nouveaux ou mises à jour seront copiés.',
  copy_incremental: 'Incrémental',
  copy_nulls_hint: 'Permettre de remplacer une donnée par une valeur nulle.',
  copy_nulls: 'Copier les valeurs nulles',
  copy_tables_task_created: 'La tâche de copie des tables a été créée avec l\'identifiant [ {id} ].',
  import_data_task_created: 'La tâche d\'importation des données a été créée avec l\'identifiant [ {id} ].',
  copy_tables_text: 'Aucune table à copier | Une table sera copiée | {count} tables seront copiées.',
  copy_tables: 'Copier les tables',
  copy: 'Copier',
  created: 'Date de création',
  dashboard: 'Tableau de bord',
  date: 'Date',
  data_access: 'Accès aux données',
  data_analysis: 'Analyse de données',
  data_file: 'Fichier de données',
  data_management: 'Gestion des données',
  databases_caption: 'Gérer les bases de données pour le stockage, l\'import et l\'export des données',
  databases: 'Base de données',
  datashield_caption: 'Configurer DataSHIELD, accorder l\'accès au service DataSHIELD',
  datashield: 'DataSHIELD',
  delete_files_confirm: '- | Êtes-vous sûr de vouloir supprimer ce fichier ? | Êtes-vous sûr de vouloir supprimer ces {count} fichiers ?',
  delete_tables_confirm: 'Aucune table à supprimer | Êtes-vous sûr de vouloir supprimer cette table ? | Êtes-vous sûr de vouloir supprimer ces {count} tables ?',
  delete_variables_confirm: 'Aucune variable à supprimer | Êtes-vous sûr de vouloir supprimer cette variable ? | Êtes-vous sûr de vouloir supprimer ces {count} variables ?',
  delete: 'Supprimer',
  density: 'Densité',
  derivation_script: 'Script de dérivation',
  descriptive_statistics: 'Statistiques descriptives',
  dictionary: 'Dictionnaire',
  docs: 'Docs',
  documentation_cookbook: 'Documentation et recettes',
  download_dictionaries: 'Télécharger les dictionnaires',
  download_dictionary: 'Télécharger le dictionnaire',
  download_view: 'Télécharger la vue',
  download_views_backup: 'Sauvegarder les vues',
  download: 'Télécharger',
  encrypt_file_content: 'Encrypter le contenu du fichier',
  encrypt_password_hint: 'Le mot de passe doit avoir au moins 8 caractères.',
  encrypt_password: 'Mot de passe',
  end_time: 'Fin',
  entities: 'Entités',
  entity_type: "Type d'entité",
  extract: 'Extraire',
  file_selection: 'Sélection de fichier',
  file_system: 'Système',
  files_caption: 'Gérer le système de fichiers, téléverser et télécharger des fichiers',
  files: 'Fichiers',
  frequencies: 'Fréquences',
  frequency: 'Fréquence',
  full_name: 'Nom complet',
  full_summary: 'Sommaire complet',
  general_settings_caption: 'Gérer la configuration générale du serveur',
  general_settings: 'Paramètres généraux',
  histogram: 'Histogramme',
  id_mappings_caption: 'Gérer les identifiants',
  id_mappings: 'Identifiants',
  id: 'ID',
  identity_providers_caption: 'Gérer les fournisseurs d\'identité OpenID Connect',
  identity_providers: 'Fournisseurs d\'identité',
  index: 'Index',
  intervals: 'Intervalles',
  is_missing: 'Manquante',
  jvm_caption: 'Monitorer la machine virtuelle Java',
  jvm: 'Machine virtuelle Java',
  kurtosis: 'Kurtosis',
  label: 'Libellé',
  last_update: 'Dernière mise à jour',
  limit: 'Limite',
  locale: 'Locale',
  max: 'Max',
  mean: 'Moyenne',
  median: 'Médiane',
  merge_variables_hint: 'Si sélectionné, les variables avec le même nom seront fusionnées (les propriétés, les catégories et les attributs seront ajoutés ou mis à jour, pas de suppression). Sinon, les variables fournies remplaceront les existantes.',
  merge_variables: 'Fusionner les variables',
  message: 'Message',
  messages: 'Messages',
  mime_type: 'Type mime',
  min: 'Min',
  missings: 'Manquantes',
  my_profile: 'Mon profil',
  name: 'Nom',
  namespace: 'Espace de noms',
  new_name: 'Nouveau nom',
  non_missings: 'Non manquantes',
  normal_distribution: 'Distribution normale',
  normal: 'Normal',
  not_empty: 'Non vide',
  occurrence_group: "Groupe d'occurrence",
  other_links: 'Autres liens',
  other: 'Autre',
  owner: 'Propriétaire',
  percentage: 'Pourcentage',
  permissions: 'Permissions',
  plugins_caption: 'Gérer les greffons du système',
  plugins: 'Greffons',
  profiles_caption: 'Gérer les profils',
  profiles: 'Profils',
  progress: 'Progression',
  project_destination: 'Projet de destination',
  project: 'Projet',
  projects_caption: 'Parcourir les tables et les variables, créer des vues, importer/exporter des données et des dictionnaires',
  projects: 'Projets',
  properties: 'Propriétés',
  referenced_entity_type: "Type d'entité référencée",
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
  search_caption: 'Configurer les paramètres de recherche',
  search: 'Recherche',
  select_columns: 'Sélectionner des colonnes',
  select_dictionary_file_template: 'Utilisez le modèle Excel suivant pour ajouter de nouvelles variables ou mettre à jour des variables existantes:',
  select_dictionary_file: 'Sélectionner un fichier de dictionnaire de variables au format Excel ou un fichier XML de vue pour l\'édition en lot des tables et des variables.',
  select_files_to_upload: 'Sélectionner les fichiers à téléverser',
  select: 'Sélectionner',
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
  table_references: 'Tables référencées',
  tables_views: 'Tables (vues)',
  tables: 'Tables',
  tags: 'Étiquettes',
  tasks_caption: 'Monitorer et planifier des tâches',
  tasks: 'Tâches',
  taxonomies_caption: 'Gérer les taxonomies et les vocabulaires contrôlés',
  taxonomies: 'Taxonomies',
  theoretical_quantiles: 'Quantiles théoriques',
  title: 'Titre',
  total: 'Total',
  unit: 'Unité',
  upload: 'Téléverser',
  user: 'Utilisateur',
  users_and_groups_caption: 'Ajouter, mettre à jour, supprimer des utilisateurs et des groupes',
  users_and_groups: 'Utilisateurs et groupes',
  users: 'Utilisateurs',
  value_type: 'Type de valeur',
  value: 'Valeur',
  values: 'Valeurs',
  variables: 'Variables',
  variance: 'Variance',
  save: 'Sauvegarder',
  edit: 'Éditer',
  edit_script: 'Éditer le script',
  script: 'Script',
  comment: 'Commentaire',
  import: 'Importer',
  export: 'Exporter',
  import_file: 'Importer depuis un fichier',
  import_server: 'Importer depuis un serveur',
  import_data: 'Importer des données',
  select_import_source: 'Sélectionner la source de données',
  configure_import_source: 'Configurer la source de données',
  preview_import_source: 'Prévisualiser les données',
  select_import_options: 'Sélectionner les options d\'import',
  back: 'Retour',
  continue: 'Continuer',
  data_format: 'Format des données',
  data_server: 'Serveur de données',
  table_name: 'Nom de la table',
  optional: 'Optionnel',
  default_value_type: 'Type de valeur par défaut',
  field_separator: 'Séparateur de champ',
  quotation_mark: 'Marque de citation',
  from_row: 'A partir de la ligne',
  char_set: 'Jeu de caractères',
  clear_tasks_confirm: 'Aucune tâche à nettoyer | Êtes-vous sûr de vouloir nettoyer cette tâche ? | Êtes-vous sûr de vouloir nettoyer ces {count} tâches ?',
  cancel_task_confirm: 'Êtes-vous sûr de vouloir annuler cette tâche ?',
  clear: 'Nettoyer',
  file_already_exists: 'Ce fichier existe déjà',
  unknown_error: 'Une erreur inconnue est survenue',
  import_limit_hint: 'Le nombre maximum de lignes à importer. Si aucune limite n\'est définie ou si la limite est 0, tous les enregistrements seront importés.',
  incremental_import: 'Importation incrémentale',
  incremental_import_hint: 'Importer uniquement les données nouvelles ou mises à jour',
  merge_dictionaries: 'Fusionner les dictionnaires',
  merge_dictionaries_hint: 'Lorsqu\'une table existe déjà, les caractéristiques des variables (propriétés, catégories, attributs) seront fusionnées avec celles importées au lieu d\'être simplement remplacées. Cela permet de conserver les annotations des variables lors des importations ultérieures.',
  no_options: 'Aucune option',
  id_column: 'Colonne ID',
  id_column_hint: 'Nom de la colonne des identifiants uniques. Si non fournie, la première colonne sera utilisée pour déterminer l\'identifiant de l\'entité.',
  opal_url: 'URL Opal',
  token: 'Jeton',
  personal_access_token: 'Jeton d\'accès personnel',
  credentials: 'Informations d\'identification',
  username: 'Nom d\'utilisateur',
  password: 'Mot de passe',
  auth_method: 'Méthode d\'authentification',
  bookmarks: 'Favoris',
  bookmark_add: 'Ajouter aux favoris',
  bookmark_remove: 'Retirer des favoris',
  no_bookmarks: 'Aucun favori',
  table: 'Table',
  delete_categories_confirm: '- | Êtes-vous sûr de vouloir supprimer cette catégorie ? | Êtes-vous sûr de vouloir supprimer ces {count} catégories ?',
  labels: 'Libellés',
};
