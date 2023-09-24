# Sync Tool

Software project artifact sync tool

## Abstract

Software is constantly changing. New versions of software: libraries,
frameworks, tools, etc. are released regularly (sometimes even daily). Companies
make use of many open source libraries and frameworks as dependencies in
its projects. Having to manually update dependencies regularly is time-consuming
and next to impossible to always stay current.

Most importantly, with more security vulnerabilities being identified and
reported in open source software, dependencies need to be updated regularly to
ensure secure usage. Companies also strives to maintain up-to-date versions of
its dependencies for compatibility.

In order to remain current with the latest versions of software dependencies,
OpenGood has developed an open source tool that performs bulk synchronization
of dependency versions. The tool is dubbed "Sync Tool". In addition, the sync
tool has other features that have been added.

Sync tool is invoked from the CLI. It is written in Kotlin as Gradle tasks and
invoked with a Gradle wrapper embedded in the sync tool's project.

## Pre-Requisites

* Java 17+
* Workspace directory setup:
  * Sync tool works from the design of a **workspace** directory where all
  local Git repos are in the same root directory or subdirectories off the
  root directory where the sync tool is stored

    Example:
      * `/workspace`
        * `/sync`
        * `/project-1`
        * `/project-2`
        * `/project-3`
          * `/sub-project-3a`

## Setup

**IMPORTANT!** All [Pre-Requisites](#pre-requisites) must be met before
syncing a project!

### Sync File

Each project directory in one's workspace must have a `sync.yml` file in order
to participate in the sync process.

**Example:**

```yaml
version: 2.0

config:
  enabled: true

git:
  remote: origin
  branch: main
  commit-message: Perform automatic project sync changes

ci:
  provider: GitHub Actions
  template: gradle/lib

versions:
  exclusions:
    - description: spring-boot-3
      type: Maven Dependency
      group: org.springframework.boot
      name: spring-boot-starter-parent
      versions:
        - 3.*
```

#### Main Properties

| Name       | Description                 | Required | Default |
|------------|-----------------------------|----------|---------|
| `config`   | Configuration properties    | No       | `N/A`   |
| `git`      | Git task(s) properties      | No       | `N/A`   |
| `ci`       | CI task(s) properties       | No       | `N/A`   |
| `versions` | Versions task(s) properties | No       | `N/A`   |

#### Configuration Properties

| Name      | Description                            | Required | Default |
|-----------|----------------------------------------|----------|---------|
| `enabled` | Value indicating if syncing is enabled | No       | `true`  |

#### Git Task(s) Properties

| Name            | Description                          | Required | Default                                  |
|-----------------|--------------------------------------|----------|------------------------------------------|
| `remote`        | Git remote name                      | No       | `origin`                                 |
| `branch`        | Git branch name                      | No       | `main`                                   |
| `commitMessage` | Git commit message for project syncs | No       | `Perform automatic project sync changes` |

#### CI Task(s) Properties

| Name       | Description                   | Required | Default |
|------------|-------------------------------|----------|---------|
| `provider` | Name of CI platform provider  | No       | `N/A`   |
| `template` | Relative path to CI templates | No       | `N/A`   |

##### Supported CI Platform Providers

* `GitHub Actions`

#### Versions Task(s) Properties

| Name         | Description                   | Required | Default |
|--------------|-------------------------------|----------|---------|
| `exclusions` | Object for version exclusions | No       | `N/A`   |

##### Version Exclusions Properties

| Name          | Description                       | Required | Default |
|---------------|-----------------------------------|----------|---------|
| `description` | Description of version exclusion  | Yes      | `N/A`   |
| `type`        | Type for version exclusion        | Yes      | `N/A`   |
| `group`       | Group name/ID of artifact         | Yes      | `N/A`   |
| `name`        | Name/ID of artifact               | Yes      | `N/A`   |
| `versions`    | Version(s) of artifact to exclude | Yes      | `N/A`   |

###### Supported Version Exclusion Types

* `Gradle Dependency`
* `Gradle Nexus Dependency`
* `Gradle Plugin`
* `Gradle Wrapper`
* `Maven Dependency`
* `Maven Nexus Dependency`
* `Maven Plugin`
* `Maven Wrapper`

**Notes:**

* Exclusions are optional. The properties are only required if an exclusion is
defined.
* Exclusion versions can be explicit or wildcard. The minor or patch version
of a version number can be a wildcard via an asterisk `*`.

### Sync Project

To sync a specific project in one's workspace directory, run:

```bash
cd <workspace-dir>/sync
./gradlew sync -PselectedProject=<project-path>
```

**Notes:**

* `<project-path>` should be relative to the workspace directory
* Gradle `sync` task performs a full sync of all available tasks:
  * CI
  * Versions
  * Git Commit
    * This task performs a Git commit of changes from the prior tasks and pushes
    the commit to the remote origin

### Sync All Projects

To sync all projects in one's workspace directory, run:

```bash
cd <workspace-dir>/sync
./gradlew sync
```

**Notes:**

* Only project directories with a configured `sync.yml` file will be synced
* Same full sync is performed as individual project

### Sync Versions Only

To sync only versions for a specific project or all projects in one's workspace
directory, run:

#### Specific Project

```bash
cd <workspace-dir>/sync
./gradlew syncVersions -PselectedProject=<project-path>
```

#### All Projects

```bash
cd <workspace-dir>/sync
./gradlew syncVersions
```

**Notes:**

* Same rules apply as described above
* Git Commit task will not be performed
