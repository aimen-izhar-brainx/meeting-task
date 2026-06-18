# Android Code Standards and Best Practices

All Android pull requests must follow these standards to ensure modern, production-safe, crash-free, maintainable, scalable, and reusable Kotlin code (Jetpack Compose and/or XML Views).

Compliance reviews use `pr_compliance_checklist.yaml` as the primary section structure. Each applicable section is evaluated in **strict compliance mode**: every deviation from these standards must be reported as a violation with actionable fixes.

---

## 1. Proper Title and Description

- Title should be self-explanatory.
- Description must include what changed, why it changed, how it changed, UI evidence if applicable, and testing notes.

---

## 2. Single Responsibility

- One feature, bug fix, or improvement per PR.
- Avoid unrelated file changes.
- Avoid combining formatting-only changes with feature work.
- Avoid changing multiple independent flows in one PR unless clearly justified.

---

## 3. Clean and Scalable Architecture

Analyze the project scope, existing code structure, and architecture before reviewing the PR. The PR should not break or bypass the existing architecture.

### Requirements

- Follow the architecture already used in the project.
- Prefer clean, scalable architecture patterns such as:
  - MVVM
  - Clean Architecture (data / domain / presentation layers)
  - MVC
  - MVP
  - MVI
  - Singleton where appropriate (via DI)
- Keep UI (Activities, Fragments, Composables), ViewModels, UseCases/Interactors, Repositories, DataSources (remote/local), Models/Entities, Managers, Services, and Utilities properly separated.
- Do not introduce direct dependencies that make features/modules tightly coupled.
- Do not bypass existing ViewModels, Repositories, UseCases, Managers, or Services by calling data sources directly from the UI.
- Keep feature boundaries clear and maintainable.
- Avoid placing business logic directly inside Activities, Fragments, Composables, or Adapters.
- Avoid adding files to random/common packages when they belong to a feature or layer.
- New code should match the existing project package structure, dependency flow (DI via Hilt/Koin/Dagger), and naming conventions.

---

## 4. Production-Safe and Crash-Free Code

- Avoid unsafe non-null assertions using `!!`.
- Avoid unsafe casts using `as`; prefer `as?` with explicit null handling unless the type is fully guaranteed.
- Use Kotlin null-safety (`?`, `?:`, `let`, sealed `Result` types) instead of assuming non-null.
- Guard against empty lists, missing map keys, invalid indexes (`getOrNull`, `firstOrNull`), and invalid states.
- Avoid assumptions about API responses, Intent/Bundle extras, `SavedStateHandle`, navigation arguments, DataStore/SharedPreferences values, or remote config values.
- Handle lifecycle edge cases: check `isAdded` / `view != null` / use `viewLifecycleOwner` in Fragments before touching UI after async work.
- Use `lifecycleScope` / `viewModelScope` with `repeatOnLifecycle(Lifecycle.State.STARTED)` for Flow collection instead of raw coroutine launches that can outlive the UI.
- Cancel coroutines, Jobs, and Disposables properly; close Cursors, Streams, and other `Closeable` resources.
- Clear ViewBinding references (`_binding = null` in `onDestroyView`), listeners, observers, animation, `MediaPlayer`/`Camera` resources, and registered receivers.

---

## 5. Managers and Services

Properly use managers/services and repositories to manage separate dependent code and reusable application operations.

### Requirements

- Use dedicated manager/repository/service classes for reusable dependencies and system operations, such as:
  - `PreferencesManager` / `DataStoreManager`
  - `ApiService` / `NetworkClient` (Retrofit/OkHttp)
  - `DatabaseManager` / Room DAO + Database
  - `CacheManager`
  - `NotificationManager` (wrapper around `NotificationManagerCompat`)
  - `PermissionManager`
  - `LocationManager`
  - `FileManager`
  - WorkManager wrapper for background jobs
- Do not duplicate API, database, SharedPreferences/DataStore, cache, permission, or file logic inside Activities/Fragments/Composables/ViewModels.
- Do not call low-level platform/storage/network APIs directly from UI if a manager/repository already exists.
- Keep managers/services focused and reusable.
- Avoid creating God classes that manage unrelated responsibilities.
- Inject or access managers/services via the project's DI framework (Hilt, Koin, or manual DI) consistently.
- Reuse existing managers/services instead of creating duplicate implementations.

---

## 6. Graceful Error Handling

- Use try/catch (or `runCatching`/`Result`) around risky operations (network, parsing, I/O, database).
- Show user-friendly error messages (Snackbar, Toast, error state UI) instead of raw exceptions.
- Log technical details only via safe logging tools (`Log`, Timber, Crashlytics) — never via `println`.
- Never expose stack traces, tokens, internal exception messages, or sensitive data in the UI.
- Handle network (`IOException`, `HttpException`), parsing, permission (`SecurityException`), platform, database (`SQLiteException`), and storage errors explicitly.
- Provide fallback/empty/error UI for failed states.
- Avoid silent failures (empty catch blocks).
- Avoid infinite loaders when an operation fails or times out.

---

## 7. Model Mapping with Default Values

- Avoid using raw `JSONObject`, `Map<String, Any?>`, or `Bundle` directly in UI.
- Use strongly typed Kotlin `data class` models.
- Add safe default values during JSON parsing (Moshi/Gson/kotlinx.serialization defaults, nullable fields with sensible fallbacks).
- Handle nullable fields carefully; avoid force-unwrapping parsed fields.
- Avoid unchecked/force casts in mapper functions (`toDomain()`, `fromJson()`).
- Keep API-to-domain mapping logic inside the data/mapper layer, not in ViewModels or UI.
- UI should consume typed domain models, not raw API DTOs or maps.
- Use safe parsing for lists, nested objects, enums, booleans, numbers, and dates (with a fallback for unknown enum values).

---

## 8. Immutability and Efficient State (Compose & Views)

- Prefer `val` over `var`; use immutable `data class` models and read-only `List`/`Map` (not `MutableList`/`MutableMap`) for state exposed to the UI.
- In Compose: use `remember`/`rememberSaveable` for state that should survive recomposition, and `derivedStateOf` for computed values to avoid unnecessary recomposition.
- Avoid creating new lambdas, objects, or modifiers on every recomposition where they could be hoisted or remembered instead.
- In XML/Views: avoid allocating new objects (`Paint`, `Rect`, formatters, listeners) inside `onDraw`, `onBindViewHolder`, or other frequently-called methods — allocate once and reuse.
- Avoid unnecessary object recreation during `RecyclerView` rebinds or Compose recompositions.
- Prefer immutable widget/UI state inputs where possible.

---

## 9. Enums / Sealed Classes

Properly use enums and sealed classes instead of hardcoded conditions.

### Requirements

- Use `enum class` for fixed statuses, roles, filters, tabs, types, and actions.
- Use `sealed class`/`sealed interface` for complex UI/API/state cases (e.g. `UiState.Loading`, `UiState.Success`, `UiState.Error`).
- Avoid hardcoded string comparisons in UI/business logic.
- Avoid repeated condition strings such as `"active"`, `"pending"`, `"completed"`, `"error"`, etc.
- Map backend status strings into typed enums/sealed classes safely (with an `UNKNOWN` fallback).
- Handle unknown enum values safely instead of throwing.
- Avoid spreading raw backend status strings directly across Composables/Fragments.

---

## 10. Utils and Helper Classes

Avoid hardcoded and duplicated utility logic by creating reusable utility/helper classes with const identifiers.

### Requirements

- Avoid writing hardcoded sizes, paddings, margins, date formatters, UI strings, and color codes directly in Composables, layouts, or Kotlin code.
- Create separate reusable objects/classes for:
  - Dimensions (`Dimens` / `dimens.xml`)
  - Spacing/paddings/margins
  - Corner radius values
  - Durations (animation/debounce)
  - UI strings (`strings.xml`, never hardcoded in Composables/layouts)
  - Colors/theme tokens (`Color.kt`, `colors.xml`)
  - Date/time parsing and formatting
  - Validators (email, phone, password)
  - Formatters (currency, number)
  - Kotlin extension functions
- Use `const val` / `object` identifiers where possible instead of magic numbers/strings.
- Avoid duplicated date/time formatting logic across files.
- Avoid unsafe date parsing (e.g. `LocalDate.parse`/`SimpleDateFormat.parse`) without fallback handling.
- Keep helper methods reusable, focused, and easy to unit test.
- Avoid placing business logic inside generic utility classes.

---

## 11. Theme Values and Design System Usage

- Use `MaterialTheme.colorScheme` / `MaterialTheme.typography` in Compose, or `?attr/colorPrimary` and app theme styles in XML, instead of literal values.
- Avoid hardcoded colors (`Color(0xFF...)`, `Color.Red`, `#FFFFFF` in XML), font sizes, radius, shadows, and spacing.
- Avoid direct usage of raw framework colors unless explicitly justified.
- Support dark mode (`isSystemInDarkTheme()`, `values-night/`) where the project supports it.
- Keep UI styling consistent with the app's shared `Theme.kt` / `themes.xml` design system.

---

## 12. State Management

- Use the project-approved state management approach (ViewModel + `StateFlow`/`LiveData`, MVI reducer, etc.) consistently.
- Avoid unnecessary mutable state scattered across Composables/Fragments when it belongs in a ViewModel.
- Do not store business logic inside Activities, Fragments, or Composables.
- Handle loading, success, empty, and error states explicitly.
- Avoid duplicated state flags (`isLoading`, `hasError`, `isEmpty`) where one typed state would be better.
- Collect Flows with `repeatOnLifecycle`/`collectAsStateWithLifecycle`; remove `Observer`s and cancel jobs properly.
- Do not mix multiple state-management approaches (e.g. LiveData and StateFlow for the same screen) unless the project architecture requires it.

---

## 13. Naming Conventions

- Classes, enums, interfaces, and objects: `PascalCase`.
- Functions, variables, and parameters: `camelCase`.
- Kotlin files: `PascalCase.kt` matching the primary class/object; resource files (layouts, drawables) and package names: `snake_case`/lowercase.
- Names should be meaningful and self-explanatory.
- Follow the existing naming style of the project.

---

## 14. Unused Code, Debug Logs, and Imports

- Remove unused imports.
- Remove commented-out code.
- Remove unused variables, functions, and classes.
- Remove `println`/`System.out.print` statements. If logging is necessary, use `Log.d`/Timber instead; otherwise remove the statement.
- `Log.*` and `Timber.*` calls are allowed and must not be treated as violations (unless they log sensitive data — see App Security).
- Remove test-only code before merging.
- Do not leave TODOs without context or a tracking reference.
- Remove duplicated comments that do not add value.

---

## 15. Reusable Code

Components used frequently in the app should be made separately to reduce code duplication and dependency.

### Requirements

- Create reusable components for frequently used UI such as:
  - Buttons
  - Text styles/typography wrappers
  - App bars / TopAppBar
  - Text fields
  - Dialogs / BottomSheets
  - Empty/error/loading state Composables or Views
  - Cards / RecyclerView list items / Compose list items
- Extract repeated business logic into UseCases, Repositories, Managers, helpers, or extension functions.
- Avoid copy-pasted UI blocks.
- Avoid copy-pasted validation, formatting, navigation, and API handling logic.
- Keep reusable components configurable but not over-engineered.
- Prefer composition (small composables/custom Views) over large condition-heavy screens.
- Reuse existing shared components before creating new ones.

---

## 16. Folder Structure

- Follow the existing project package/module structure.
- Use feature-based, layered (data/domain/presentation), MVVM, clean architecture, MVC, MVP, or MVI structure consistently as applicable.
- Avoid placing repositories, models, managers, use cases, or business logic inside `ui`/`view` packages.
- Avoid dumping unrelated files into `common`/`util`/`shared` packages.
- Keep file locations predictable and maintainable.

---

## 17. UI Responsiveness and Layout Safety

- Avoid overflow/clipping; test long text, empty values, and small screens.
- In Compose: use `weight`, proper `Box`/`Column`/`Row` alignment, `LazyColumn`/`LazyRow` for lists, and scrollable containers for long content.
- In XML: use `ConstraintLayout`/`LinearLayout` weights, `wrap_content`/`match_parent` appropriately, and `RecyclerView` for lists (never manually inflated lists in a loop).
- Avoid fixed `dp` widths/heights unless required; support different screen sizes and orientations.
- Support dynamic content (long strings, large text settings) safely.
- Respect window insets / safe areas (notches, navigation/status bars).
- Provide `contentDescription` / accessibility labels for meaningful interactive elements.

---

## 18. Dependency Injection Consistency

Ensure all dependencies are wired through the project's DI framework rather than constructed manually inside UI or business logic classes.

### Requirements

- All new dependencies should be injected through the project's DI framework (Hilt/Koin/Dagger).
- Avoid manual dependency creation (`Repository()`, `ApiService()`, `Retrofit.Builder()`) inside Activities, Fragments, Composables, ViewModels, or Managers.
- Use constructor injection whenever possible.
- Avoid Service Locator patterns unless already established in the project.
- Scope dependencies appropriately (`@Singleton`, `@ActivityRetainedScoped`, `@ViewModelScoped`, etc.).
- Avoid injecting unnecessary dependencies.

### Common Violations

```kotlin
private val repository = UserRepository()
```

Instead:

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
)
```

---

## 19. Coroutine and Flow Best Practices

### Requirements

- Avoid `GlobalScope`.
- Avoid launching coroutines without lifecycle awareness.
- Prefer structured concurrency.
- Use appropriate dispatchers (`IO`, `Default`, `Main`).
- Avoid nested coroutine launches where unnecessary.
- Handle Flow exceptions using `catch`.
- Use `stateIn` / `shareIn` when appropriate.
- Avoid collecting the same Flow multiple times unnecessarily.
- Use `collectLatest` when older emissions should be cancelled.

### Common Violations

```kotlin
GlobalScope.launch {
    repository.loadData()
}
```

```kotlin
viewModelScope.launch {
    flow.collect { }
}
```

without exception handling.

---

## 20. Localization / Internationalization

### Requirements

- No user-facing hardcoded strings.
- Use `strings.xml`.
- Support string formatting placeholders.
- Avoid concatenating strings.

### Bad

```kotlin
Text("Hello " + name)
```

### Good

```xml
<string name="welcome_user">Hello %1$s</string>
```

```kotlin
stringResource(R.string.welcome_user, name)
```

---

## 21. Navigation Safety

### Requirements

- Use typed navigation arguments where possible.
- Validate incoming arguments.
- Avoid passing large objects through navigation.
- Avoid duplicate navigation events.
- Prevent multiple rapid clicks from triggering repeated navigation.
- Handle deep links safely.

### Common Violation

```kotlin
navController.navigate("details/$user")
```

where `user` is serialized manually.

---

## 22. Memory Leak Prevention

### Requirements

- Avoid holding Activity/Fragment Context in singleton objects.
- Avoid storing Views inside ViewModels.
- Remove listeners when no longer needed.
- Unregister BroadcastReceivers.
- Release Camera, MediaPlayer, ExoPlayer resources.
- Avoid leaking coroutine scopes.

### Example Violation

```kotlin
object SessionManager {
    lateinit var context: Context
}
```

---

## 23. Performance and Recomposition Review (Compose)

### Requirements

- Avoid expensive calculations directly inside Composables.
- Use stable state objects.
- Use `remember` appropriately.
- Avoid creating collections during recomposition.

### Common Violation

```kotlin
LazyColumn {
    items(users.filter { it.active }) { }
}
```

Prefer:

```kotlin
val activeUsers by remember(users) {
    derivedStateOf { users.filter { it.active } }
}
```

---

# General Product Engineering Practices

These practices are important for secure, scalable, high-quality Android products. Map violations from these guidelines into the most relevant checklist section when they apply to changed code.

## A. Modern App Architecture

- Prefer feature-first or clean architecture (data/domain/presentation) for medium/large apps.
- Keep presentation, domain, and data layers separate.
- Use repositories to abstract API/local data sources (Room, DataStore, network).
- Keep network clients (Retrofit/OkHttp) isolated from UI and ViewModels.
- Keep business rules in UseCases/Interactors, Repositories, Managers, or ViewModels — not in Composables, Activities, or Fragments.
- Avoid direct dependency between unrelated features/modules.
- Use dependency injection (Hilt, Koin, or Dagger) for services, repositories, clients, and shared utilities.
- Avoid global mutable state.
- Keep shared/common modules minimal and intentional.

## B. App Security

- Never hardcode API keys, tokens, credentials, secrets, or private URLs — use `local.properties`, Gradle secrets, or a secrets manager, exposed via `BuildConfig`.
- Use `EncryptedSharedPreferences` or encrypted DataStore for sensitive tokens.
- Avoid logging tokens, user data, headers, OTPs, passwords, or payment details (including via `Log`/Timber/Crashlytics breadcrumbs).
- Do not expose raw backend errors or stack traces to users.
- Validate and sanitize user input where required.
- Use HTTPS-only API communication; avoid disabling SSL/TLS validation.
- Handle authorization failures (401/403) safely.
- Clear sensitive data (tokens, cached user data) on logout.
- Avoid storing sensitive data in plain SharedPreferences.
- Keep environment-specific configuration outside source code (build flavors / `BuildConfig`, not hardcoded strings).
- Avoid committing keystore/`.jks` files, `google-services.json` secrets, signing configs, or `local.properties`.
- Review deep links, intent filters, and exported `Activity`/`Service`/`Receiver` components carefully.
- Enable code shrinking/obfuscation (R8/ProGuard) for release builds.

## C. Scalability and Maintainability

- Keep features modular and independently maintainable (multi-module Gradle setup for large apps).
- Avoid large Activities, Fragments, ViewModels, Composables, and Manager/Service classes.
- Avoid duplicated business logic across features.
- Keep navigation centralized and typed (Navigation Component / Compose Navigation).
- Keep error handling centralized.
- Keep manager/service/repository usage centralized and consistent.
- Keep date, currency, number, and string formatting centralized.
- Add documentation for complex flows and architecture decisions.

## D. High-Level Performance Practices

- Avoid expensive work inside Composable function bodies or `onBindViewHolder`.
- Avoid unnecessary recompositions or `notifyDataSetChanged()` calls (prefer `DiffUtil`/stable item keys).
- Use efficient list rendering with `LazyColumn`/`LazyRow`, `RecyclerView` with `DiffUtil`, or Paging 3.
- Avoid loading large data sets at once.
- Cancel coroutines/Jobs, unregister listeners/BroadcastReceivers, and clear ViewBinding/Compose state appropriately.
- Avoid memory leaks from retained `Context`/`Activity` references in singletons, listeners, or static fields.
- Optimize images using proper dimensions, caching, placeholders, and compression (Coil/Glide with proper sizing).
- Avoid blocking the main thread with heavy parsing/computation or `runBlocking` calls in UI-facing code.
- Move heavy computation to background dispatchers (`Dispatchers.Default`/`Dispatchers.IO`) or WorkManager.
- Use debounce/throttle for search and high-frequency UI events.
- Avoid repeated API calls during recomposition/configuration changes.

## E. Optimized Code Structure

- Keep Composables, Activities, Fragments, and ViewModels small and focused.
- Extract repeated UI into reusable Composables/custom Views.
- Extract repeated logic into UseCases, extension functions, services, managers, or helpers.
- Prefer composition over inheritance for UI.
- Avoid deeply nested layout hierarchies when readability/performance suffers.
- Avoid complex conditions directly inside Composable/layout trees.
- Keep files easy to scan and review.
- Avoid mixing UI, API, mapping, state, and business logic in one file.
- Keep imports clean and organized.
