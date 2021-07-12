# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- No new features!
### Changed
- No changed features!
### Deprecated
- No deprecated features!
### Removed
- No removed features!
### Fixed
- No fixed issues!
### Security
- No security issues fixed!

## [3.0.0] - 2021-06-10
### Changed
- BREAKING CHANGE: Remove RX packages, moved API to full coroutines.
### Changed
- Add support for injecting view models scoped to the navigation component's graph in Jetpack Compose.

## [2.0.0] - 2021-05-01
### Changed
- Changed repo ownership to [hyperdevs-team](https://github.com/hyperdevs-team). Thanks [bq](https://github.com/bq) for all the work!
- Changed package names from `com.bq.*` to `com.hyperdevs.*`

## [1.4.0] - 2020-12-15
### Added
- Upgrade Kotlin to 1.4.21 and Kodein to 7.1.0, apart from other Android dependencies.

## [1.3.3] - 2020-08-13
### Added
- Add proguard rules for most modules that need them.

## [1.3.2] - 2020-05-27
### Added
- Add `allTerminal`, `onAllTerminal` and `firstExceptionOrNull` functions to lists of `Resource`s.

## [1.3.1] - 2020-04-22
### Added
- Upgrade Kotlin to 1.3.72 and Kodein to 6.5.5, apart from other Android dependencies.

## [1.3.0] - 2020-03-13
### Added
- Add support for incremental annotation processing.
### Fixed
- Fix sources not getting attached to some packages, now they should be visible from Android Studio.

## [1.2.0] - 2020-02-19
### Added
- Add `sharedActivityViewModel` to Kodein extensions to support shared Activity view models.
### Fixed
- Fix `ConcurrentModificationException`s in store subscriptions' iteration by adding safe iteration over them.
### Changed
- Rename `toggleAbility` to `toggleEnabled` as the name was confusing.
- Upgraded project dependencies.

IMPORTANT: as a result of upgrading dependencies, you may need to target Java 8.

## [1.1.2] - 2020-02-07
### Added
- Add new utilities in `KodeinAndroidUtils` to inject `ViewModelProvider.Factory` instances and retrieve `ViewModel`s with `by viewModel(params)`.

## [1.1.1] - 2020-02-03
### Added
- Add new `View` extensions to change view and view lists visibility status and enabled/disabled status.

## [1.1.0] - 2020-01-28
### Added
- Add `TypedTask` class in order to store simple metadata inside a `Task`.

## [1.0.9] - 2020-01-09
### Added
- Add support for custom backpressure strategies when calling `Store.flowable`.
The default strategy is `BackpressureStrategy.BUFFER`.

### Fixed
- Fix `Store.flowable` not unsubscribing correctly when disposed.

## [1.0.8] - 2019-12-19
### Fixed
- Fix logger not rendering correctly in logcat. Thanks @danielceinos!

## [1.0.7] - 2019-12-04
### Changed
- Make `Resource.empty` an object instead of a class.

## [1.0.6] - 2019-11-20
### Fixed
- Fix `Resource` and `Task` `toString` functions.

## [1.0.5] - 2019-11-20
### Fixed
- Fix `Task` `toString` function.

## [1.0.4] - 2019-11-14
### Added
- Add `equals` and `hashCode` to `Resource` class to ease comparisons.

## [1.0.3] - 2019-11-04
### Fixed
- Fix `getOrNull` not returning a value if said value is set-up in
`Resource.loading`

## [1.0.2] - 2019-10-28
### Added
- `Resource.isTerminal` method to check if a Resource is in a terminal
state (`success` or `failure`)

## [1.0.1] - 2019-10-23
### Added
- `mini-testing` package adds testing utilities for the library.

## [1.0.0] - 2019-10-07
### Added
- Initial architecture release.

[Unreleased]: https://github.com/hyperdevs-team/mini-kotlin/compare/3.0.0...HEAD
[3.0.0]: https://github.com/hyperdevs-team/mini-kotlin/compare/2.0.0...3.0.0
[2.0.0]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.4.0...2.0.0
[1.4.0]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.3.3...1.4.0
[1.3.3]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.3.2...1.3.3
[1.3.2]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.3.1...1.3.2
[1.3.1]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.3.0...1.3.1
[1.3.0]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.2.0...1.3.0
[1.2.0]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.1.2...1.2.0
[1.1.2]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.1.1...1.1.2
[1.1.1]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.1.0...1.1.1
[1.1.0]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.0.9...1.1.0
[1.0.9]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.0.8...1.0.9
[1.0.8]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.0.7...1.0.8
[1.0.7]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.0.6...1.0.7
[1.0.6]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.0.5...1.0.6
[1.0.5]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.0.4...1.0.5
[1.0.4]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.0.3...1.0.4
[1.0.3]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.0.2...1.0.3
[1.0.2]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.0.1...1.0.2
[1.0.1]: https://github.com/hyperdevs-team/mini-kotlin/compare/1.0.0...1.0.1
[1.0.0]: https://github.com/hyperdevs-team/mini-kotlin/releases/tag/1.0.0
