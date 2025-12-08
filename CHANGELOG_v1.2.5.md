# IonAPI v1.2.5

🚀 **Release v1.2.5 implements the missing platform logic for Paper and Folia support.**

### ✨ Features
- **Full Platform Support**:
  - ✅ **Paper**: Native support via `IonPaperPlugin`.
  - ✅ **Folia**: Native support via `IonFoliaPlugin` and threaded region scheduler.
- **Unified Scheduler**:
  - `IonScheduler` now automatically delegates to best underlying scheduler (Bukkit vs. Folia).
  - Added `runAt(Entity, Runnable)` and `runAt(Location, Runnable)` for context-aware scheduling.
- **Core Improvements**:
  - Implemented base `IonPlugin` logic.
  - Added default service implementations for CommandRegistry, EventBus, and ConfigProvider.

### 📦 Artifacts
- **`IonAPI-1.2.5.jar`** - The main all-in-one shading artifact. (Recommended)

### 🛠️ Shading
```kotlin
implementation("com.github.mattbaconz:IonAPI:1.2.5")
```
### 📜 Full Changelog
- Implemented `PaperScheduler` and `FoliaScheduler`.
- Created platform-specific plugin entry points.
- Bumped version to 1.2.5.
