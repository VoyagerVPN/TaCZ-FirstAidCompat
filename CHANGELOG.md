# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.1] - 2026-10-26

- Взрывы передают фактический центр из Forge ExplosionEvent.Detonate в алгоритм распределения First Aid.
- Исправлена потеря взрывного урона при переносе из разрушенной стопы: коэффициент взрыва сохраняется на всём пути.
- MixinExtras 0.5.0 включён в итоговый JAR через Forge Jar-in-Jar.
- Добавлены четыре Forge GameTest: центр взрыва, перенос из стопы, две составляющие пули, броня и бронепробитие.

Подготовка: `.\setup.ps1`. Сборка: `.\gradlew.bat build`. Проверки: `.\gradlew.bat runGameTestServer --offline` после заполнения `.cache/gradle`. JDK и моды закреплены с хешами в `dependencies.lock.json`; wrapper, build output и тестовый мир используют проектную `.cache`. Итоговый JAR: `.cache/build/libs/tacz-firstaid-compat-1.0.1.jar`.

GameTest подаёт события урона напрямую. Клиентская стрельба и визуальное совпадение хитбоксов этими проверками не подтверждаются.

## [1.0.0] - 2026-01-23

### Fixed
- Fixed "Missing metadata in pack" warning by adding `pack.mcmeta`.
- Fixed version checker error in logs by correcting/disabling invalid `update.json` URL.
- Added English and Russian localization for mod name and description.
- Removed non-existent logo file reference to clean up logs.
- Replaced `@Redirect` with `@WrapOperation` from MixinExtras for the primary damage distribution interceptor. This is much more robust and should resolve the recurring "Critical injection failure" crash.
- Added `MixinSquared` support to ensures compatibility if other mods also mixin into FirstAid's `EventHandler`.

## [0.0.4-beta] - 2026-01-23

### Fixed
- Further fix for Mixin injection failure in `FirstAidEventHandlerMixin`. Simplified the redirect target to use name-based matching, overcoming issues with exact method descriptor matching in production environments.

## [0.0.3-beta] - 2026-01-23

### Fixed
- Fixed critical Mixin injection failure in `FirstAidEventHandlerMixin` by setting `remap = false` on `@Inject` and `@Redirect`. This resolves the startup crash where the redirection failed to find the target method in the obfuscated environment.

## [0.0.2-beta] - 2026-01-23

### Fixed
- Added missing `@Mod` annotated main class (fixes "mods that were not found" error).

## [0.0.1-beta] - 2026-01-23

### Fixed
- Added missing `modLoader` and `loaderVersion` fields to `mods.toml` (fixes Forge loading error).

## [0.0.0-beta] - 2026-01-23

### Added
- Initial release of [TaCZ] First Aid Compat.
- Precise 3D hitbox detection for TacZ bullets (Head, Body, Arms, Legs, Feet).
- Custom damage distribution for explosions based on proximity.
- Damage spillover mechanics from limbs to torso.
- Support for crawling/prone state posture in TacZ.
- Proper armor protection calculations per bodypart.
- Converted core logic from Kotlin to pure Java to minimize mod size and dependencies.
- MIT License.
