# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)

## Unreleased

## [v1.4.0] - 2023-08-17

### Notable Changes

This version of the CMA java client works with cumulus-message-adapter
[v2.0.2](https://github.com/nasa/cumulus-message-adapter/releases/tag/v2.0.2) or later,
and it's built with JDK 1.8.  The package works on AWS Runtime Java 8 environment, for both Amazon Linux 1 and 2.

### Changed

- **CUMULUS-3386**
  - Updated CMA client to utilize pre-packaged AWS LINUX 2 binary for CMA when system python is unavailable or
  USE_CMA_BINARY environment is set to true. This is a breaking change if your task environment does not have
  python in the system path, but generally should be backward compatible with most use cases.
  - Updated ci configuration to test the package on jdk8 environment both with and without system python

## [v1.3.10] - 2023-08-07

### Notable Changes

This version of the CMA java client works with cumulus-message-adapter
[v2.0.2](https://github.com/nasa/cumulus-message-adapter/releases/tag/v2.0.2) or earlier,
and it's built with JDK 1.8.

### Changed

- **CUMULUS-3182**
  - Updated `AdapterLogger.InitializeLogger` method to be public
  - Updated example Cumulus task to initialize logger, build with Gradle 8
  - Updated com.amazonaws libraries to address security vulnerability

## [v1.3.9] - 2022-01-20

### Fixed

- Updated `aws-lambda-java-log4j2` to `1.5.1` to address security vulnerability

## [v1.3.8] - 2022-01-12

### Fixed

- Updated `log4j-api` to `2.17.1` to address security vulnerability
- Updated `log4j-core` to `2.17.1` to address security vulnerability

## [v1.3.7] - 2021-12-20

### Added

- Added `log4j-api` version `2.17.0`

### Fixed

- Updated `aws-lambda-java-log4j2` to `1.5.0` to address [security vulnerability](https://nvd.nist.gov/vuln/detail/CVE-2021-45105)

## [v1.3.6] - 2021-12-20

### Fixed

- Updated `log4j-core` to `2.17.0` to address [security vulnerability](https://nvd.nist.gov/vuln/detail/CVE-2021-45105)

## [v1.3.5] - 2021-12-15

### Fixed

- Updated `aws-lambda-java-log4j2` to `1.4.0` to address [security vulnerability](https://github.com/advisories/GHSA-jfh8-c2jp-5v3q)

## [v1.3.4] - 2021-12-15

### Fixed

- Updated `org.apache.logging.log4j` to `2.16.0` to address [security vulnerability](https://github.com/advisories/GHSA-jfh8-c2jp-5v3q)

## [v1.3.3] - 2021-12-13

### Fixed

- Updated `aws-lambda-java-log4j2` to `1.3.0` and `org.apache.logging.log4j` to `2.15.0` to address [critical security vulnerability](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2021-44228)
- Updated `com.google.code.gson` to `2.8.9` to address security vulnerabilities

## [v1.3.2] - 2020-11-19

### Fixed

- Fixed timeout on large messages

## [v1.3.1] - 2020-06-10

### Fixed

- **CUMULUS-2017** - fixed the `AdapterLogger` timestamp to be in ISO format

## [v1.3.0] - 2020-02-10

### BREAKING CHANGES

- Updated external CMA call to utilize python3 in anticipation of updates to the CMA.

This is a breaking change as it requires the existence of a `python3` runtime in the environment, similar to AWS's lambda runtime for Java8

## [v1.2.13] - 2020-01-02

### Added

- Added `JsonUtils` class to help with converting from a JSON string to a `Map` with proper type assignment
  - `JsonUtils.toMap()` converts JSON string to a `Map`

## [v1.2.12] - 2019-12-17

### Changed

- Updated `MessageParser.HandleMessage()` to properly handle errors where `e.getMessage()` is `null`, such as a `NullPointerException`

## [v1.2.11] - 2019-11-15

### Added

- CUMULUS-1634 - Adds three new keys `granules`, `parentArn`, and `stackName` to the AdapterLogger class

## [v1.2.10] - 2019-10-03

### Added

- CUMULUS-1488 - Update java client to allow use of CUMULUS_MESSAGE_ADAPTER_DIR environment variable/support layers

## [v1.2.9] - 2019-09-16

### Added

- Updated CMA client to handle parameterized configuration, set execution env variable regardless of message format
