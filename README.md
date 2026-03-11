# AI Commit Messages

JetBrains plugin for generating Git commit messages from the selected changes in the Commit tool window using AI CLI tools.

Download from [Jetbrains Marketplace](https://plugins.jetbrains.com/plugin/30544-aicommitmessages)

## What it does

- Adds a `Generate Commit Message` action to the commit workflow.
- Uses the diff from the files currently selected for commit.
- Supports choosing between Cursor CLI and GitHub Copilot CLI.
- Lets you configure CLI paths and a default provider in `Settings > Tools > AI Commit Messages`.

## Install

Install from [Jetbrains Marketplace](https://plugins.jetbrains.com/plugin/30544-aicommitmessages):

Or in your JetBrains IDE:

1. Open `Settings` / `Preferences`.
2. Go to `Plugins`.
3. Search for `AI Commit Messages`.
4. Install the plugin and restart the IDE.

## Setup

Before using the plugin, make sure at least one supported CLI is installed and accessible from your machine:

- Cursor CLI
- GitHub Copilot CLI

Then open `Settings > Tools > AI Commit Messages` and configure:

- `Cursor CLI path`
- `VSCode CLI path`
- `Default CLI`

If the CLI binary is already available in `PATH`, the defaults may work. Otherwise, set the full executable path manually.

## Usage

1. Open the Commit tool window in a supported JetBrains IDE.
2. Select the files you want to commit.
3. Click `Generate Commit Message`.
4. Choose the AI CLI, unless you already set a default.
5. The generated message is inserted into the commit message field.

## Platform Support

This plugin is not macOS-only.

It does not contain macOS-specific logic and should work on macOS, Linux, and Windows as long as:

- the JetBrains IDE version is compatible with the plugin
- the configured AI CLI is installed on that OS
- the CLI path is set correctly in plugin settings

If a CLI command name differs on your system, use the full executable path in the plugin settings.

## Credits
- [@shivamag00](https://github.com/shivamag00)