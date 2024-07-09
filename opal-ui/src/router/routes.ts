import { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  { path: '/index.html', redirect: '/' },
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: '', component: () => import('pages/IndexPage.vue') },
      { path: '/admin', component: () => import('pages/AdminPage.vue') },
      { path: '/admin/users', component: () => import('pages/AdminUsersGroupsPage.vue') },
      { path: '/admin/rservers', component: () => import('pages/AdminRPage.vue') },
      { path: '/admin/datashield', component: () => import('pages/AdminDatashieldPage.vue') },
      { path: '/admin/settings', component: () => import('pages/AdminConfigurationPage.vue') },
      { path: '/admin/databases', component: () => import('pages/AdminDatabasesPage.vue') },
      { path: '/admin/profiles', component: () => import('pages/AdminProfilesPage.vue') },
      { path: '/admin/profile', component: () => import('pages/AdminProfilePage.vue') },
      { path: '/admin/profile/:principal/permissions', component: () => import('pages/AdminProfileAclsPage.vue') },
      { path: '/admin/idproviders', component: () => import('pages/AdminIdentityProvidersPage.vue') },
      { path: '/projects', component: () => import('pages/ProjectsPage.vue') },
      {
        path: '/project/:id',
        component: () => import('pages/ProjectPage.vue'),
      },
      {
        path: '/project/:id/tables',
        component: () => import('pages/ProjectTablesPage.vue'),
      },
      {
        path: '/project/:id/table/:tid',
        component: () => import('pages/ProjectTablePage.vue'),
      },
      {
        path: '/project/:id/table/:tid/variable/:vid',
        component: () => import('pages/ProjectVariablePage.vue'),
      },
      {
        path: '/project/:id/resources',
        component: () => import('pages/ProjectResourcesPage.vue'),
      },
      {
        path: '/project/:id/files',
        component: () => import('pages/ProjectFilesPage.vue'),
      },
      {
        path: '/project/:id/reports',
        component: () => import('pages/ProjectReportsPage.vue'),
      },
      {
        path: '/project/:id/tasks',
        component: () => import('pages/ProjectTasksPage.vue'),
      },
      {
        path: '/project/:id/perms',
        component: () => import('pages/ProjectPermsPage.vue'),
      },
      {
        path: '/project/:id/admin',
        component: () => import('pages/ProjectAdminPage.vue'),
      },
      {
        path: '/files/:path*',
        component: () => import('pages/FilesPage.vue'),
      },
      {
        path: '/tasks/:path*',
        component: () => import('pages/TasksPage.vue'),
      },
    ],
  },
  {
    path: '/signin',
    component: () => import('pages/SigninPage.vue'),
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue'),
  },
];

export default routes;
