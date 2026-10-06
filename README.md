# Gestion de stock

Application de gestion de stock composée d'une API Spring Boot et d'une interface Angular. Elle couvre le suivi des articles, fournisseurs, besoins et commandes, ainsi que les alertes de stock et l'export PDF des bons de commande.

## Fonctionnalités

- Connexion et déconnexion ; aucune inscription publique.
- Consultation et gestion des articles et des fournisseurs.
- Gestion des comptes acheteurs, commerciaux et administrateurs, réservée à ADMIN.
- Expression des besoins, filtres et préparation des commandes par fournisseur.
- Consultation des commandes et de leurs lignes (« bandes de commande »).
- Alertes de stock faible ou en rupture et tableau de bord.
- Export PDF d'un bon de commande.

Les règles métier de quantités, prix et mouvements de stock sont conservées. Les limites identifiées sont détaillées dans [VERIFICATION.md](VERIFICATION.md).

## Technologies

| Partie | Technologies |
| --- | --- |
| Interface | Angular 19.2, TypeScript 5.7, RxJS, Bootstrap 5, jsPDF |
| API | Java 17+, Spring Boot 3.5.3, Spring Security, Spring Data JPA, Bean Validation |
| Base de données | Microsoft SQL Server |
| Tests | JUnit, Spring Boot Test, MockMvc, H2 (tests uniquement), Jasmine/Karma |

## Structure

```text
projetStage/
├── GestionStock_back/
│   ├── src/main/java/org/springdemo/gestionstock/
│   │   ├── controllers/   # API REST
│   │   ├── dto/           # Objets de requête/réponse
│   │   ├── entity/        # Entités JPA
│   │   ├── repository/    # Accès aux données
│   │   ├── security/      # Authentification et autorisations
│   │   └── services/      # Services métier
│   ├── src/main/resources/application.properties
│   ├── src/test/
│   └── pom.xml
├── gestion-stock-front/
│   ├── src/app/
│   ├── src/environments/
│   ├── angular.json
│   └── package.json
├── .env.example
├── .gitignore
├── README.md
└── VERIFICATION.md
```

## Prérequis

- JDK 17 ou supérieur, avec `JAVA_HOME` configuré.
- Node.js 22 et npm pour Angular 19 ; dépendances verrouillées dans `package-lock.json`.
- SQL Server accessible et un outil d'administration SQL, par exemple SSMS.
- Accès Internet lors de la première installation Maven/npm.
- Chrome pour les tests Angular avec `ChromeHeadless`.

Le wrapper Maven est fourni : aucune installation Maven globale n'est nécessaire.

## SQL Server et variables d'environnement

1. Activer TCP/IP sur l'instance SQL Server et vérifier le port utilisé.
2. Créer une base dédiée, par exemple `gestion_stock`.
3. Créer un compte SQL applicatif dédié avec les permissions nécessaires sur cette base. Éviter le compte administrateur SQL.
4. Exporter les variables suivantes dans le processus qui lancera Spring Boot.

| Variable | Usage |
| --- | --- |
| `DB_URL` | URL JDBC SQL Server, obligatoire |
| `DB_USERNAME` | Compte SQL applicatif, obligatoire |
| `DB_PASSWORD` | Mot de passe SQL local, obligatoire |
| `CORS_ALLOWED_ORIGINS` | Origines autorisées, séparées par des virgules ; défaut : `http://localhost:4200` |
| `AUTH_TOKEN_TTL` | Durée ISO-8601 des sessions ; défaut : `PT8H`, maximum 24 heures |

[.env.example](.env.example) ne contient que des valeurs d'exemple. **Spring Boot ne charge pas automatiquement un fichier .env** : utiliser les variables du terminal ou de la configuration d'exécution de l'IDE. Les fichiers `.env` sont exclus de Git.

Exemple PowerShell, à adapter localement :

```powershell
$env:DB_URL = 'jdbc:sqlserver://localhost:1433;databaseName=gestion_stock;encrypt=true;trustServerCertificate=true'
$env:DB_USERNAME = 'stock_app_example'
$credential = Get-Credential -UserName $env:DB_USERNAME -Message 'Compte SQL Server local'
$env:DB_PASSWORD = $credential.GetNetworkCredential().Password
$env:CORS_ALLOWED_ORIGINS = 'http://localhost:4200'
```

`trustServerCertificate=true` est destiné au développement local avec certificat autosigné. Pour un serveur exposé, configurer un certificat valide et utiliser `trustServerCertificate=false`.

La base doit exister avant le démarrage. Le réglage historique `spring.jpa.hibernate.ddl-auto=update` est conservé ; Hibernate met à jour les tables. Prévoir des migrations versionnées et des sauvegardes avant un déploiement sur des données réelles.

## Installation et lancement

Dans un premier terminal, avec les variables SQL configurées :

```powershell
cd GestionStock_back
.\mvnw.cmd spring-boot:run
```

L'API écoute par défaut sur `http://localhost:8080`.

Dans un second terminal :

```powershell
cd gestion-stock-front
npm ci
npm start
```

Ouvrir `http://localhost:4200`. L'adresse de l'API locale est centralisée dans `src/environments/environment.ts`.

Sous Linux/macOS, utiliser `./mvnw` à la place de `.\mvnw.cmd` et exporter les variables avec le shell utilisé.

## Authentification et rôles

Les mots de passe sont hachés avec BCrypt. La connexion vérifie les comptes existants dans `Utilisateur` puis `Acheteur`, sans modifier les données. Elle émet un jeton opaque aléatoire de 256 bits, transmis dans `Authorization: Bearer …`. Seule son empreinte est conservée côté serveur. Les jetons expirent et sont révoqués à la déconnexion ; un changement de mot de passe ou une suppression du compte invalide l'accès à la requête suivante.

Le stockage des sessions est **en mémoire sur une seule instance**, limité à 10 000 sessions actives. Un redémarrage déconnecte les utilisateurs. Pour plusieurs instances, prévoir un stockage de sessions partagé. Le frontend conserve le jeton dans le stockage local et efface la session lorsqu'une API répond 401.

Aucun compte ni mot de passe par défaut n'est fourni. Les seuls rôles autorisés sont `ADMIN`, `ACHETEUR` et `COMMERCIAL`. L'inscription publique est supprimée, y compris côté API ; aucune variable d'environnement ne la réactive.

| Accès aux modules présents | ADMIN | ACHETEUR | COMMERCIAL |
| --- | --- | --- | --- |
| Consulter articles, fournisseurs, besoins et alertes | Oui | Oui | Oui |
| Modifier articles et fournisseurs | Oui | Non | Non |
| Créer un besoin | Tous les types | Non | DEVIS ou NEGOCE |
| Modifier ou supprimer un besoin existant | Oui | Non | Non |
| Préparer, consulter et passer les commandes d'achat | Oui | Oui | Non |
| Créer, modifier et supprimer des comptes | Oui | Non | Non |

Le périmètre métier COMMERCIAL inclut les clients, devis et commandes de vente. **Ces modules ne sont pas présents dans cette version.** Seule la création des besoins DEVIS/NEGOCE est disponible ; il n'existe pas encore de relation persistée vers un document de vente ou un devis. Les commandes actuellement présentes sont des commandes d'achat et ne sont pas accessibles au COMMERCIAL.

### Gestion des comptes et données existantes

L'écran **Gestion des comptes** (`/comptes`) est accessible uniquement à ADMIN, tout comme l'API `/api/admin/accounts`. Il regroupe les comptes des deux tables existantes. Les nouveaux acheteurs sont créés dans Acheteur ; les nouveaux commerciaux et administrateurs dans Utilisateur. Une modification ne déplace pas un compte entre les tables et conserve son identifiant. Un mot de passe vide en modification conserve le hash existant.

Le rôle historique `USER` n'accorde plus d'accès et n'est plus attribuable. **Les comptes et leurs mots de passe sont conservés**, sans conversion automatique : un ADMIN doit choisir explicitement leur nouveau rôle depuis cet écran. La suppression d'un compte exige une action explicite de l'ADMIN avec confirmation dans l'interface. La suppression de son propre compte, le retrait de son propre rôle ADMIN et la disparition du dernier ADMIN sont refusés.

### Initialiser le premier ADMIN

Cette procédure est facultative, manuelle et uniquement destinée à une base ne contenant aucun ADMIN. Si un ADMIN existe déjà, utiliser son compte. Aucune initialisation ne s'exécute lors d'un lancement normal.

Dans le terminal backend, après configuration des variables SQL, saisir un **nouvel email** et un mot de passe local :

```powershell
$bootstrapCredential = Get-Credential -Message 'Nouveau compte ADMIN : email et mot de passe'
$env:BOOTSTRAP_ADMIN_EMAIL = $bootstrapCredential.UserName
$env:BOOTSTRAP_ADMIN_PASSWORD = $bootstrapCredential.GetNetworkCredential().Password
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--app.bootstrap-admin.enabled=true --server.address=127.0.0.1'
```

Le démarrage crée exactement un nouveau compte ADMIN. Il échoue sans créer de compte si un ADMIN existe déjà ou si l'email est déjà utilisé. Aucun compte historique n'est promu, supprimé ou remplacé. Les mutations de comptes sont sérialisées dans le déploiement mono-instance pris en charge.

Après ce démarrage, arrêter l'application (Ctrl+C), retirer les variables d'initialisation, puis relancer normalement :

```powershell
Remove-Item Env:BOOTSTRAP_ADMIN_EMAIL, Env:BOOTSTRAP_ADMIN_PASSWORD
Remove-Variable bootstrapCredential
.\mvnw.cmd spring-boot:run
```

Se connecter avec le nouvel ADMIN, ouvrir **Gestion des comptes** et attribuer les rôles des anciens comptes, ou créer les comptes acheteurs/commerciaux nécessaires. Ne jamais mettre ces identifiants dans Git.

## Builds et tests

Frontend :

```powershell
cd gestion-stock-front
npm run build
npm test -- --watch=false --browsers=ChromeHeadless
```

Backend :

```powershell
cd GestionStock_back
.\mvnw.cmd verify
```

Les tests backend utilisent une base H2 en mémoire, indépendante de SQL Server. Ils ne remplacent pas une validation des parcours métier avec SQL Server.

## Configuration de production

- `environment.prod.ts` utilise une URL API vide : les requêtes ciblent `/api` sur la même origine. Configurer le reverse proxy pour acheminer `/api/**` vers Spring Boot.
- Pour des domaines séparés, définir l'origine réelle de l'API dans ce fichier, puis adapter `CORS_ALLOWED_ORIGINS` et reconstruire.
- Les pages protégées sont rendues dans le navigateur : leurs données et identifiants ne peuvent pas être pré-rendus lors du build.
- Le serveur Angular reste disponible via `npm run serve:ssr:gestion-stock-front` après compilation. Il ne remplace pas le proxy API.
- Les composants de pages et l'export PDF sont chargés à la demande. Les budgets du build restent contrôlés.
- Utiliser HTTPS ; l'inscription publique est déjà fermée.

## Publication du code

Les fichiers générés, dépendances, réglages IDE, journaux, fichiers .env, clés et sauvegardes SQL sont exclus par les règles d'ignorance. Conserver `package-lock.json` et le wrapper Maven.

Le frontend possède déjà son propre dépôt Git. Aucun dépôt n'a été supprimé ou initialisé et aucune publication n'a été faite. L'organisation du futur dépôt global reste à effectuer séparément, en préservant l'historique existant. Un `.gitignore` ne retire pas un secret déjà suivi dans l'index ou l'historique : vérifier aussi ces éléments avant publication.

Aucune capture d'écran métier n'est incluse. Aucun fichier de licence n'a été ajouté : le choix de licence appartient au propriétaire du projet.
