# AGENTS.md - AI Development Guide for AICommitMessages

## Project Overview
A JetBrains IDE plugin that generates Git commit messages using AI CLIs (Cursor CLI and GitHub Copilot CLI). The plugin integrates into the commit workflow, extracts diffs from selected files, and uses AI to generate concise commit messages.

**Key Tech Stack:**
- Java 21, Kotlin 2.1.20
- IntelliJ Platform Gradle Plugin 2.10.2
- Targets IntelliJ IDEA 2025.2.4+
- No external AI library dependencies (uses system CLI binary execution)

---

## Architecture Overview

### Core Data Flow
```
User clicks "Generate Commit Message"
  ↓
GenerateCommitMessageAction (entry point)
  ├→ FilesProvider.getSelectedFiles() → extracts selected files from commit UI
  ├→ AiToolProvider.getSelectedAiTool() → prompts user to choose CLI (Cursor or Copilot) if no default
  ├→ CLIExecutor.isCliAvailable() → validates CLI availability
  └→ Runs async task (background thread):
      ├→ GitDiffProvider.getDiff(selectedFiles) → extracts diff from selected files
      ├→ CLIExecutor.generateCommitMessage() → orchestrates CLI execution via AiCliClient
      └→ Updates commit message field via UI thread
```

### Component Responsibilities

**GenerateCommitMessageAction** (`src/main/java/com/homebattles/aicommitmessages/GenerateCommitMessageAction.java`)
- AnAction that integrates into VCS commit UI
- Uses `FilesProvider.getSelectedFiles()` to get selected files from `VcsDataKeys.COMMIT_WORKFLOW_UI`
- Uses `AiToolProvider.getSelectedAiTool()` to prompt user for CLI choice if no default set
- Validates CLI availability via `CLIExecutor.isCliAvailable()`
- Runs async task (background thread) to avoid UI blocking
- Extracts message between `$$$` delimiters (CLI output parsing)
- Updates commit message field via UI thread

**FilesProvider** (`src/main/java/com/homebattles/aicommitmessages/providers/FilesProvider.java`)
- Extracts selected files from commit workflow UI
- Handles both versioned changes (`COMMIT_WORKFLOW_UI.getIncludedChanges()`) and unversioned files (`getIncludedUnversionedFiles()`)
- Returns `Set<VirtualFile>` of selected files

**AiToolProvider** (`src/main/java/com/homebattles/aicommitmessages/providers/AiToolProvider.java`)
- Manages AI tool selection logic
- Returns default tool from settings if configured, otherwise prompts user with dialog
- Uses `AiTool` enum values for available options

**GitDiffProvider** (`src/main/java/com/homebattles/aicommitmessages/providers/GitDiffProvider.java`)
- Reads VCS changes from `ChangeListManager` (default changelist only)
- Filters changes by selected files passed as parameter
- Handles both versioned and unversioned files
- Returns simplified diff format with status markers: "Added"/"Modified"/"Deleted"
- For unversioned files, includes full file content

**CLIExecutor** (`src/main/java/com/homebattles/aicommitmessages/CLIExecutor.java`)
- Takes `AiTool` and `AiCliClient` for execution
- Builds prompt with delimiter markers (`$$$ MESSAGE $$$`)
- Delegates command building and execution to `AiCliClient`
- Runs CLI from project root directory (fallback: user home)
- Checks availability by running `--version` command
- Combines stdout/stderr via `redirectErrorStream(true)`

**AiTool** (`src/main/java/com/homebattles/aicommitmessages/aitools/AiTool.java`)
- Enum defining available AI tools: CURSOR, COPILOT
- Each tool has display name and associated `AiCliClient` instance
- Factory method `fromString()` for parsing from settings

**AiCliClient** (`src/main/java/com/homebattles/aicommitmessages/aitools/cliclients/AiCliClient.java`)
- Abstract base class for CLI client implementations
- Methods: `getExecutablePath()`, `buildCommand()`, `setSettings()`
- Cursor uses "instructions" command with optional "--model"
- Copilot uses "-p" prompt parameter

**AICommitMessagesSettings** (`src/main/java/com/homebattles/aicommitmessages/settings/AICommitMessagesSettings.java`)
- IntelliJ IDE **@Service** (application-scoped singleton)
- Persisted to `AICommitMessages.xml` via **@State/@Storage**
- Stores: cursor CLI path, VSCode CLI path, cursor model, default AiTool type
- Defaults: `agent`, `copilot`, "", "ASK_EVERY_TIME"
- Uses `AiTool.fromString()` for default provider parsing

**AICommitMessagesConfigurable** (Settings UI)
- Implements `SearchableConfigurable` and `Configurable.NoScroll`
- UI path: Settings > Tools > AI Commit Messages
- Manages 4 settings fields; syncs with Settings on apply/reset
- Uses `AiTool` enum for default CLI dropdown

---

## Key Integration Points

### IntelliJ IDE Integration
- **Action Registration** (`plugin.xml`): added to `Vcs.MessageActionGroup` and `VcsGlobalGroup`
- **VCS Data Access**: `VcsDataKeys.COMMIT_WORKFLOW_UI` for selected files and changes
- **Change Detection**: `ChangeListManager.getDefaultChangeList()` for versioned changes, `getUnversionedFilesPaths()` for unversioned
- **Application Service**: `ApplicationManager.getApplication().getService()` for settings
- **Background Tasks**: `ProgressManager.getInstance().run(Task.Backgroundable)` for async execution
- **Logger**: `Logger.getInstance()` for plugin logging

### CLI Execution Pattern
Platform-specific binary execution:
```java
ProcessBuilder pb = new ProcessBuilder(aiCliClient.buildCommand(prompt));
pb.directory(new File(project.getBasePath() != null ? project.getBasePath() : System.getProperty("user.dir")));
pb.redirectErrorStream(true);
Process p = pb.start();
// Read output via BufferedReader from process.getInputStream()
```

---

## Critical Conventions & Patterns

### 1. **Service Instantiation**
Always use:
```java
AICommitMessagesSettings.getInstance()  // NOT: new AICommitMessagesSettings()
```
Settings are IntelliJ IDE services, not POJOs.

### 2. **Message Extraction**
CLI responses must be wrapped with delimiters. Parser looks for first and last `$$$`:
```
Input: "$$$ Fix: improve performance $$$"
Output: "Fix: improve performance"
```
If delimiters missing, entire response is used.

### 3. **Async Execution Safety**
UI updates require `ApplicationManager.getApplication().invokeLater()`:
```java
// From background task:
ApplicationManager.getApplication().invokeLater(() -> {
    commitMessageControl.setCommitMessage(generatedMessage);
});
```

### 4. **Provider Selection Strategy**
- If default AiTool set: use it directly
- Otherwise: prompt user with dialog (options from `AiTool` enum)
- Always validate provider availability before execution

### 5. **Diff Content Limitations**
Current implementation provides simplified diff (not unified format):
- Shows file status (Added/Modified/Deleted)
- Includes full before/after content for changes
- Handles unversioned files by reading from disk
- Filters by selected files in UI

---

## Developer Workflows

### Build & Run
```bash
# Build plugin
./gradlew build

# Run in IDE sandbox
./gradlew runIde
# Opens IntelliJ with plugin loaded: Settings > Tools > AI Commit Messages

# Package for distribution
./gradlew buildPlugin
# Output: build/distributions/AICommitMessages-1.0.0.zip
```

### Testing During Development
1. Open IDE sandbox → Create test repo with changes
2. Go to Commit tool window
3. Select files → Click "Generate Commit Message"
4. Check plugin logs: IDE Logs tab or `build/idea-sandbox/IU-*/log/idea.log`

### Add New CLI Provider
1. Create new `AiCliClient` subclass (e.g., `NewCliClient.java`) in `aitools/cliclients/`
2. Add new enum value to `AiTool` with factory instantiation
3. Add settings fields in `AICommitMessagesSettings` (path + any model params)
4. Add UI fields in `AICommitMessagesConfigurable`
5. Test via sandbox IDE

### Plugin Configuration
- Entry point: `src/main/resources/META-INF/plugin.xml`
- Define: plugin ID, actions, extension points, dependencies
- Minimum build: `sinceBuild = "252.25557"`

---

## Common Gotchas & Edge Cases

1. **Empty Diff**: If selected files have no actual changes, `getDiff()` returns empty string → error shown to user
2. **CLI Not in PATH**: Must configure full path in Settings if CLI not in system PATH
3. **Process Blocking**: Always use `Task.Backgroundable` to avoid UI freeze during `waitFor()`
4. **Unversioned Files**: Only file content is captured (no diff context)
5. **Default Changelist Only**: Plugin only works with default changelist, not other shelves/stashes

---

