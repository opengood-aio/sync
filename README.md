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

## Features

Sync tool is invoked from the CLI. It is written in Kotlin as Gradle tasks and
invoked with a Gradle wrapper embedded in the sync tool's project.


