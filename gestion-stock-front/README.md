# Interface Angular — Gestion de stock

Consulter le [README global](../README.md) pour la configuration SQL Server, l'authentification, les rôles et le lancement du backend.

```powershell
npm ci
npm start
npm run build
npm test -- --watch=false --browsers=ChromeHeadless
```

L'API de développement est configurée dans `src/environments/environment.ts`.
En production, `environment.prod.ts` utilise `/api` sur la même origine ; un reverse proxy doit rediriger ces requêtes vers Spring Boot.

Les pages sont chargées à la demande et rendues dans le navigateur, car leur contenu dépend du compte connecté. Le module PDF est chargé lorsque l'utilisateur demande un export.
