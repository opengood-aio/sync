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

### Installations

* Java JDK (17+)

## Setup

**IMPORTANT!** All [Pre-Requisites](#pre-requisites) must be met before
syncing a project!

### Sync File

Each project directory in one's workspace must have a `sync.yml` file in order
to participate in the sync process.

**Example:**

```yaml
version: 1.0

git:
  branch: dev
```

#### Configuration Properties

##### `Root`

| Name      | Description                            | Required | Default |
|-----------|----------------------------------------|----------|---------|
| `enabled` | Value indicating if syncing is enabled | No       | `true`  |

##### `Git`

| Name     | Description                                    | Required | Default |
|----------|------------------------------------------------|----------|---------|
| `branch` | Name of Git branch in which to perform syncing | No       | `dev`   |

### Sync Project

To sync a specific project in one's workspace directory, run:

```bash
cd <workspace-dir>/sync
./gradlew sync -PselectedProject=<project-dir>
```

### Sync All Projects

To sync all projects in one's workspace directory, run:

```bash
cd <workspace-dir>/sync
./gradlew sync
```
