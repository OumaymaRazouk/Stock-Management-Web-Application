import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: '**',
    // Authenticated, user-specific screens and their IDs are resolved in the browser.
    renderMode: RenderMode.Client
  }
];
