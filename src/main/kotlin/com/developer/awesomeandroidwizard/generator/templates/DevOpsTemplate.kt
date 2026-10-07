package com.developer.awesomeandroidwizard.generator.templates

import com.developer.awesomeandroidwizard.model.WizardModel

object DevOpsTemplate {

    fun generateGitHubActionsWorkflow(model: WizardModel): String = """
name: Android CI

on:
  push:
    branches: [ main, master, develop ]
  pull_request:
    branches: [ main, master, develop ]

jobs:
  build:
    name: Build & Test (${model.projectName})
    runs-on: ubuntu-latest
    timeout-minutes: 30

    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Validate Gradle Wrapper
        uses: gradle/actions/wrapper-validation@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'corretto'
          java-version: '17'

      - name: Setup Gradle Cache
        uses: gradle/actions/setup-gradle@v3

      - name: Run Unit Tests & Lint
        run: ./gradlew lint test --continue

      - name: Assemble Debug APK
        run: ./gradlew assembleDebug

      - name: Upload Test Reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: '**/build/reports/'
""".trimIndent()

    fun generateDependabot(): String = """
version: 2
updates:
  # Maintain Gradle dependencies in libs.versions.toml
  - package-ecosystem: "gradle"
    directory: "/"
    schedule:
      interval: "weekly"
    open-pull-requests-limit: 10
    commit-message:
      prefix: "deps"
""".trimIndent()

    fun generateDetektConfig(): String = """
build:
  maxIssues: 0
  excludeCorrectable: false

complexity:
  LongMethod:
    threshold: 60
  TooManyFunctions:
    thresholdInFiles: 25

naming:
  FunctionNaming:
    functionPattern: '^([a-z][a-zA-Z0-9]*|[A-Z][a-zA-Z0-9]*)$' # Allows PascalCase for @Composable functions

style:
  MagicNumber:
    active: false
  UnusedPrivateMember:
    active: true
""".trimIndent()
}
