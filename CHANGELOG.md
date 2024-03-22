# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
## [1.7.4] - 2024-03-22
### Fixed
- Make log4j2 binding a test dependency. Otherwise downstream projects complain at runtime.

## [1.7.3] - 2024-03-20
### Fixed
- Bugfix: stackoverflow in makeUnique 

## [1.7.2] - 2024-03-20
- First public maven central release
- Dependency upgrades
- Note this version contains a bug that causes a stack overflow

## [1.7.1] -  2023-09-18
- New UniProt xsd schema

## [1.7.0] -  2022-06-03
- Make sure additional property enums are generated with distinct namespace

## [1.6.2] -  2022-12-01
### Bug fix
- Fixed UniProt xsd schema URL

## [1.6.0] -  2022-06-03
### Upgraded
- Guava 31.1-jre
- Log4j 2.17.2
- jsonschema2pojo 1.1.2
- Swagger 2.2.0
- Jackson 2.13.3
- jaxb-api 2.4.0-b180830.0359
- nashorn-core 15.4

### Added
- Support for folder organization for generation jobs

## [1.5.1] -  2022-04-01
### Upgraded
- Jackson and Swagger libraries

## [1.5.0] -  2022-01-20
### Upgraded
- Now built java 11 compliant. It will not work anymore on jdk 8.

## [1.4.1] -  2021-09-23
### Fixed
- fix minor improvements
- update documentation

## [1.4.0] -  2021-09-22
### Added
- jsonschema2pojo: support for generating types with generic parameters
- jsonschema2pojo: ignore duplicated enums during generation process