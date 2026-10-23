# Subscription Management API

API Spring Boot de gestion des abonnements d'entreprises à des plans.

## Prérequis

- Java 21 (toolchain Gradle)
- Aucun Gradle local nécessaire : utiliser le wrapper `./gradlew`

## Build

```bash
./gradlew build
```

Cette commande compile, exécute les tests et produit le JAR dans `build/libs/`.

Autres commandes utiles :

```bash
./gradlew test      # tests uniquement
./gradlew bootRun   # démarrer l'application
```

## Packages

Tous les packages se trouvent sous `hz.labs.subscriptionmanagementapi`.

| Package | Responsabilité |
|---|---|
| `company` | Gestion des entreprises clientes qui souscrivent aux plans d'abonnement. |
| `plan` | Définition et gestion du catalogue des plans d'abonnement. |
| `subscription.api` | Exposition REST des abonnements (contrôleurs, DTO, mapping HTTP). |
| `subscription.application` | Cas d'usage qui orchestrent le domaine et les ports. |
| `subscription.domain` | Modèle métier pur : entités, objets valeur, règles et invariants. |
| `subscription.infrastructure` | Adaptateurs techniques : persistance, intégrations, implémentation des ports. |
| `security` | Authentification, autorisation et filtres de sécurité. |
| `shared` | Éléments transverses : exceptions communes, utilitaires, types génériques. |

## Règle de dépendance

Les dépendances pointent toujours **vers le domaine**. Un package ne peut importer
que les packages listés dans sa colonne « Peut dépendre de ».

| Package | Peut dépendre de |
|---|---|
| `shared` | — (aucun package du projet) |
| `subscription.domain` | `shared` |
| `subscription.application` | `subscription.domain`, `company`, `plan`, `shared` |
| `subscription.api` | `subscription.application`, `shared` |
| `subscription.infrastructure` | `subscription.application`, `subscription.domain`, `shared` |
| `company`, `plan` | `shared` |
| `security` | `shared` |

```
subscription.api ──────────┐
                           ▼
subscription.infrastructure ──► subscription.application ──► subscription.domain ──► shared
```

En pratique :

- **`subscription.domain` ne dépend d'aucun framework** (ni Spring, ni JPA, ni HTTP).
- **`subscription.application` définit les ports** (interfaces) ; `subscription.infrastructure` les implémente.
- **`subscription.api` ne touche jamais l'infrastructure** et passe uniquement par les cas d'usage.
- **`company` et `plan` n'importent pas `subscription`** : la dépendance va de `subscription` vers eux, jamais l'inverse.
- **Aucun package métier n'importe `security`** : la sécurité s'applique de façon transverse, via la configuration et les filtres.
- **`shared` ne dépend de rien** dans le projet ; il ne doit pas devenir un fourre-tout métier.
