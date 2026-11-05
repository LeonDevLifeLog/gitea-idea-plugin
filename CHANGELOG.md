<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Gitea Changelog

## [Unreleased]

### Changed

- Support IntelliJ IDEA 2026.3.
- Allow SSH ports from 1 to 65535 in the settings.

### Removed

- Remove inactive protected-branch and application-activation extension registrations.

### Fixed

- Send task API tokens in the authorization header instead of URL query parameters.
- Send the Basic and token authorization headers on every API request.
- Select Git HTTP credentials only from accounts matching the requested server.
- Prevent duplicate account creation during login.
- Make the login field editable only when no login is preset.
- Preserve Gitea installation path prefixes when parsing repository remote URLs.
- Correct clone repository list events and loading-state thread handling.
- Apply closed-issue filtering and the configured connection timeout, falling back to the default when it is 0.
- Handle Gitea API authentication, server and empty-response errors explicitly.
- Filter task results with the supported `assigned_by`, `state` and `type` parameters.
- Mask task access tokens and validate that replacement tokens belong to the expected account.
- Replace deprecated future-based repository and avatar loading with coroutine scopes.
- Treat empty and relative server URLs as validation errors instead of crashing the login dialog.
- Open the login dialog with an empty server when a Git remote URL cannot be parsed.

## [0.9.0] - 2026-08-02

- Supports new versions of JetBrains IDEs.

## [0.8.0] - 2026-07-27

- Supports new versions of JetBrains IDEs.

## [0.7.0] - 2026-05-05

- Support IntelliJ IDEA 2026.1

## [0.5.4] - 2025-10-14

- Fix: action runner error.
- Support IntelliJ idea 2025.x

## [0.5.2] - 2025-10-14

### Added

- Support IntelliJ idea 2025.x

## [0.5.1] - 2024-11-29

- Support IntelliJ idea 2024.3

## [0.5.0] - 2024-09-10

### Added

- Support IntelliJ IDEA 2024.2
- upgrade SDK version to 2.x

## [0.4.0] - 2024-07-25

### Added

- Support Gitea with self-signed cert.

## [0.3.4] - 2024-05-15

### Added

- Support IntelliJ IDEA 2024.1

## [0.3.3] - 2023-12-26

### Added

- Support pull code by ssh custom port
- Support reverse proxy Gitea instance with a sub-path

## [0.3.2] - 2023-12-15

### Added

- Support IntelliJ IDEA 2023.3

## [0.3.1] - 2023-12-04

### Fixed

- unable to pull code via ssh

## [0.3.0] - 2023-10-24

### Added

- support issue/task state update by idea
- support issue/task time track

### Fixed

- incorrect use of global issue number

## [0.2.2] - 2023-08-16

### Fixed

- issues about plugin verification

## [0.2.1] - 2023-08-16

### Fixed

- issues about plugin verification

## [0.2.0] - 2023-08-14

### Added

- Support Clone Gitea Repository

## [0.2.0-beta] - 2023-08-13

### Added

- Support Clone Gitea Repository

## [0.1.0] - 2023-08-11

### Added

- Support Gitea Task(Issue) manager

[Unreleased]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/compare/0.9.0...HEAD
[0.9.0]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/compare/0.8.0...0.9.0
[0.8.0]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/compare/0.7.0...0.8.0
[0.7.0]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/compare/0.5.4...0.7.0
[0.5.4]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/compare/0.5.2...0.5.4
[0.5.2]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/compare/0.5.1...0.5.2
[0.5.1]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/compare/0.5.0...0.5.1
[0.5.0]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/compare/0.4.0...0.5.0
[0.4.0]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/compare/0.3.4...0.4.0
[0.3.4]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/compare/0.3.3...0.3.4
[0.3.3]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/compare/0.3.2...0.3.3
[0.3.2]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/compare/0.3.1...0.3.2
[0.3.1]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/compare/0.3.0...0.3.1
[0.3.0]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/compare/0.2.2...0.3.0
[0.2.2]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/compare/0.2.1...0.2.2
[0.2.1]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/compare/0.2.0...0.2.1
[0.2.0]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/compare/0.2.0-beta...0.2.0
[0.2.0-beta]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/compare/0.1.0...0.2.0-beta
[0.1.0]: https://github.com/LeonDevLifeLog/gitea-idea-plugin/commits/0.1.0
