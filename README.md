# Sync Tool

OpenGood project artifact sync tool

## Abstract

Software is constantly changing. New versions of software: libraries,
frameworks, tools, etc. are released regularly (sometimes even daily). OpenGood
makes use of many open source libraries and frameworks as dependencies in
its projects. Having to manually update dependencies regularly is time-consuming
and next to impossible to always stay current.

Most importantly, with more security vulnerabilities being identified and
reported in open source software, dependencies need to be updated regularly to
ensure secure usage. OpenGood also strives to maintain up-to-date versions of
its dependencies for compatibility.

In order to remain current with the latest versions of software dependencies,
OpenGood has developed an open source tool that performs bulk synchronization
of dependency versions. The tool is dubbed "Sync Tool". In addition, the sync
tool has other features that have been added.

Sync tool is invoked from the CLI. It is written in Kotlin as Gradle tasks and
invoked with a Gradle wrapper embedded in the sync tool's project.

## Pre-Requisites

* Java 17+

## Setup

**IMPORTANT!** All [Pre-Requisites](#pre-requisites) must be met before
syncing a project!

### Sync File

Each project directory in one's workspace must have a `sync.yml` file in order
to participate in the sync process.

**Example:**

```yaml
version: 1.0

config:
  enabled: true

git:
  remote: origin
  branch: main

ci:
  provider: GitHub Actions
  template: gradle/lib
```

#### Main Properties

| Name      | Description                            | Required | Default |
|-----------|----------------------------------------|----------|---------|
| `config`  | Configuration properties               | No       | `N/A`   |
| `ci`      | CI task(s) properties                  | No       | `N/A`   |
| `git`     | Git task(s) properties                 | No       | `N/A`   |

#### Configuration Properties

| Name      | Description                            | Required | Default |
|-----------|----------------------------------------|----------|---------|
| `enabled` | Value indicating if syncing is enabled | No       | `true`  |

#### Git Task(s) Properties

| Name     | Description     | Required | Default  |
|----------|-----------------|----------|----------|
| `remote` | Git remote name | No       | `origin` |
| `branch` | Git branch name | No       | `main`   |

#### CI Task(s) Properties

| Name       | Description                   | Required | Default |
|------------|-------------------------------|----------|---------|
| `provider` | Name of CI platform provider  | No       | `N/A`   |
| `template` | Relative path to CI templates | No       | `N/A`   |

##### Supported CI Providers

* `GitHub Actions`

### Sync Project

To sync a specific project in one's workspace directory, run:

```bash
cd <workspace-dir>/sync
./gradlew sync -Pproject=<project-path>
```

**Notes:**

* `<project-path>` should be relative to the workspace directory

### Sync All Projects

To sync all projects in one's workspace directory, run:

```bash
cd <workspace-dir>/sync
./gradlew sync
```

**Notes:**

* Only project directories with a configured `sync.yml` file will be synced
