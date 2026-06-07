# fp-felles

Shared building blocks and integration clients for Team Foreldrepenger backend applications.

## Shared context

- Source of truth for shared domain, architecture, and conventions: `navikt/fp-context`
- Copilot Space: `navikt/TeamForeldrepenger`
- Consumer guide: `fp-context/architecture/team-libraries.md`

## Repo-specific context

| Topic | Details                                                                         |
 |---|---------------------------------------------------------------------------------|
| Role | Common building blocks (`felles/`) and shared external clients (`integrasjon/`) |
| Tech stack | Java SemVer library  |
| Consumers | All fp backend apps, `ft-beregning`, `ft-kalkulus`                              |
| Release model | SemVer; not in `fp-bom`; imported directly                                      |

## Key constraints

- `felles/` modules are mostly independent blocks — avoid adding cross-dependencies
- `integrasjon/` clients belong here only if used by >1 app
- `oidc` and `abac` have known opt-outs (see `fp-context/team-libraries.md`)
- `server` is evolving — check consumers before changing defaults

## Verification

- Changes may affect all backend apps
- Verify through a representative consumer (e.g. `fp-sak`, `fp-soknad`)
- For integration impact: `navikt/fp-autotest` suites `fpsak`, `verdikjede`
