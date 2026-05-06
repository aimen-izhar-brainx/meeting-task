# Android project structure for AI ticket generator
_Exported on 5/6/2026 at 14:16:26 GMT+5 from Cursor (3.2.21)_

---

**User**

You are an expert Android architect.
Set up a clean Kotlin Android project structure for an AI-powered “Client Brief → Jira Ticket Generator” app.

Requirements:

No authentication
No database
No notifications
Clean Architecture preferred (presentation, domain, data layers)

Create:

MainActivity
A single screen UI 
Modules/packages structure:
presentation
domain
data
ai

Output only project structure + base files.

---

**Cursor**

I’ll scaffold a clean Kotlin Android project layout with the requested layers (`presentation`, `domain`, `data`, `ai`) and add base files for `MainActivity` plus a single-screen UI. I’m checking the current workspace first, then I’ll create the structure directly.

I found an existing Android app skeleton, so I’ll now replace it with a clean-architecture package layout and minimal base classes for the single screen flow (client brief input -> generate Jira ticket).

I’m creating the base Kotlin classes and wiring now (domain/data/ai/presentation + updated `MainActivity` and layout), keeping everything minimal and compile-ready without auth/database/notifications.

```text
MeetingTask/
├── build.gradle.kts
├── settings.gradle.kts
└── app/
    ├── build.gradle.kts
    └── src/
        └── main/
            ├── AndroidManifest.xml
            ├── java/com/example/meetingtask/
            │   ├── MainActivity.kt
            │   ├── presentation/
            │   │   └── brief/
            │   │       ├── BriefGeneratorUiState.kt
            │   │       ├── BriefGeneratorViewModel.kt
            │   │       └── BriefGeneratorViewModelFactory.kt
            │   ├── domain/
            │   │   ├── model/
            │   │   │   └── JiraTicketDraft.kt
            │   │   ├── repository/
            │   │   │   └── ClientBriefRepository.kt
            │   │   └── usecase/
            │   │       └── GenerateJiraTicketUseCase.kt
            │   ├── data/
            │   │   └── repository/
            │   │       └── ClientBriefRepositoryImpl.kt
            │   └── ai/
            │       ├── mapper/
            │       │   └── ClientBriefPromptBuilder.kt
            │       └── service/
            │           ├── AiTicketGenerator.kt
            │           └── FakeAiTicketGenerator.kt
            └── res/
                ├── layout/
                │   └── activity_main.xml
                └── values/
                    └── strings.xml
```

```kotlin
// app/src/main/java/com/example/meetingtask/MainActivity.kt
package com.example.meetingtask

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.meetingtask.ai.mapper.ClientBriefPromptBuilder
import com.example.meetingtask.ai.service.FakeAiTicketGenerator
import com.example.meetingtask.data.repository.ClientBriefRepositoryImpl
import com.example.meetingtask.databinding.ActivityMainBinding
import com.example.meetingtask.domain.usecase.GenerateJiraTicketUseCase
import com.example.meetingtask.presentation.brief.BriefGeneratorViewModel
import com.example.meetingtask.presentation.brief.BriefGeneratorViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: BriefGeneratorViewModel by viewModels {
        val promptBuilder = ClientBriefPromptBuilder()
        val aiGenerator = FakeAiTicketGenerator()
        val repository = ClientBriefRepositoryImpl(promptBuilder, aiGenerator)
        val useCase = GenerateJiraTicketUseCase(repository)
        BriefGeneratorViewModelFactory(useCase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.generateButton.setOnClickListener {
            viewModel.onGenerateClicked(binding.clientBriefInput.text.toString())
        }

        viewModel.uiState.observe(this) { state ->
            binding.generateButton.isEnabled = !state.isLoading
            binding.generatedTicketOutput.text = state.ticketPreview
            binding.errorText.text = state.errorMessage.orEmpty()
        }
    }
}
```

```kotlin
// app/src/main/java/com/example/meetingtask/presentation/brief/BriefGeneratorUiState.kt
package com.example.meetingtask.presentation.brief

data class BriefGeneratorUiState(
    val isLoading: Boolean = false,
    val ticketPreview: String = "",
    val errorMessage: String? = null
)
```

```kotlin
// app/src/main/java/com/example/meetingtask/presentation/brief/BriefGeneratorViewModel.kt
package com.example.meetingtask.presentation.brief

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.meetingtask.domain.usecase.GenerateJiraTicketUseCase

class BriefGeneratorViewModel(
    private val generateJiraTicketUseCase: GenerateJiraTicketUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData(BriefGeneratorUiState())
    val uiState: LiveData<BriefGeneratorUiState> = _uiState

    fun onGenerateClicked(clientBrief: String) {
        if (clientBrief.isBlank()) {
            _uiState.value = _uiState.value?.copy(
                errorMessage = "Client brief cannot be empty.",
                ticketPreview = ""
            )
            return
        }

        _uiState.value = _uiState.value?.copy(isLoading = true, errorMessage = null)

        runCatching {
            generateJiraTicketUseCase(clientBrief)
        }.onSuccess { ticket ->
            _uiState.value = _uiState.value?.copy(
                isLoading = false,
                ticketPreview = "Title: ${ticket.title}\n\nDescription:\n${ticket.description}",
                errorMessage = null
            )
        }.onFailure { throwable ->
            _uiState.value = _uiState.value?.copy(
                isLoading = false,
                errorMessage = throwable.message ?: "Unknown error",
                ticketPreview = ""
            )
        }
    }
}
```

```kotlin
// app/src/main/java/com/example/meetingtask/presentation/brief/BriefGeneratorViewModelFactory.kt
package com.example.meetingtask.presentation.brief

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.meetingtask.domain.usecase.GenerateJiraTicketUseCase

class BriefGeneratorViewModelFactory(
    private val generateJiraTicketUseCase: GenerateJiraTicketUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BriefGeneratorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BriefGeneratorViewModel(generateJiraTicketUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
```

```kotlin
// app/src/main/java/com/example/meetingtask/domain/model/JiraTicketDraft.kt
package com.example.meetingtask.domain.model

data class JiraTicketDraft(
    val title: String,
    val description: String
)
```

```kotlin
// app/src/main/java/com/example/meetingtask/domain/repository/ClientBriefRepository.kt
package com.example.meetingtask.domain.repository

import com.example.meetingtask.domain.model.JiraTicketDraft

interface ClientBriefRepository {
    fun generateTicketFromBrief(clientBrief: String): JiraTicketDraft
}
```

```kotlin
// app/src/main/java/com/example/meetingtask/domain/usecase/GenerateJiraTicketUseCase.kt
package com.example.meetingtask.domain.usecase

import com.example.meetingtask.domain.model.JiraTicketDraft
import com.example.meetingtask.domain.repository.ClientBriefRepository

class GenerateJiraTicketUseCase(
    private val repository: ClientBriefRepository
) {
    operator fun invoke(clientBrief: String): JiraTicketDraft {
        return repository.generateTicketFromBrief(clientBrief)
    }
}
```

```kotlin
// app/src/main/java/com/example/meetingtask/data/repository/ClientBriefRepositoryImpl.kt
package com.example.meetingtask.data.repository

import com.example.meetingtask.ai.mapper.ClientBriefPromptBuilder
import com.example.meetingtask.ai.service.AiTicketGenerator
import com.example.meetingtask.domain.model.JiraTicketDraft
import com.example.meetingtask.domain.repository.ClientBriefRepository

class ClientBriefRepositoryImpl(
    private val promptBuilder: ClientBriefPromptBuilder,
    private val aiTicketGenerator: AiTicketGenerator
) : ClientBriefRepository {

    override fun generateTicketFromBrief(clientBrief: String): JiraTicketDraft {
        val prompt = promptBuilder.build(clientBrief)
        return aiTicketGenerator.generateTicket(prompt)
    }
}
```

```kotlin
// app/src/main/java/com/example/meetingtask/ai/mapper/ClientBriefPromptBuilder.kt
package com.example.meetingtask.ai.mapper

class ClientBriefPromptBuilder {
    fun build(clientBrief: String): String {
        return """
            Convert this client brief into a Jira ticket.
            Return a concise title and clear technical description.

            Client brief:
            $clientBrief
        """.trimIndent()
    }
}
```

```kotlin
// app/src/main/java/com/example/meetingtask/ai/service/AiTicketGenerator.kt
package com.example.meetingtask.ai.service

import com.example.meetingtask.domain.model.JiraTicketDraft

interface AiTicketGenerator {
    fun generateTicket(prompt: String): JiraTicketDraft
}
```

```kotlin
// app/src/main/java/com/example/meetingtask/ai/service/FakeAiTicketGenerator.kt
package com.example.meetingtask.ai.service

import com.example.meetingtask.domain.model.JiraTicketDraft

class FakeAiTicketGenerator : AiTicketGenerator {
    override fun generateTicket(prompt: String): JiraTicketDraft {
        return JiraTicketDraft(
            title = "Client Brief: Initial Jira Ticket",
            description = "Generated from prompt:\n$prompt"
        )
    }
}
```

```xml
<!-- app/src/main/res/layout/activity_main.xml -->
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:id="@+id/screenTitle"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Client Brief to Jira Ticket"
            android:textAppearance="@style/TextAppearance.MaterialComponents.Headline6" />

        <EditText
            android:id="@+id/clientBriefInput"
            android:layout_width="match_parent"
            android:layout_height="160dp"
            android:layout_marginTop="12dp"
            android:gravity="top|start"
            android:hint="Paste or type client brief"
            android:inputType="textMultiLine"
            android:padding="12dp" />

        <Button
            android:id="@+id/generateButton"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:text="Generate Jira Ticket" />

        <TextView
            android:id="@+id/errorText"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:textColor="@android:color/holo_red_dark" />

        <TextView
            android:id="@+id/generatedTicketOutput"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:text="Generated ticket will appear here" />
    </LinearLayout>
</ScrollView>
```

```xml
<!-- app/src/main/res/values/strings.xml -->
<resources>
    <string name="app_name">Client Brief Jira Generator</string>
</resources>
```

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.meetingtask"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.meetingtask"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

---

**User**

Build a xml screen where user can:

Paste a raw client brief (multi-line text field)
Click a “Generate Tickets” button
View loading state
Show output in a structured list

UI requirements:

Input TextField (large)
Button: "Generate Dev Tasks"
Result section:
Epic
User Stories
Tasks (bullet list)

Keep UI minimal and clean.
For AI make a folder i will paste my keys there to perform this task

---

**Cursor**

I’ll implement a cleaner XML screen with a large brief input, `Generate Dev Tasks` button, loading indicator, and structured Epic/User Stories/Tasks output, then add a dedicated folder for your AI keys. I’m updating layout and wiring `MainActivity` + ViewModel/model next.

I’m now switching state/model to structured output (`epic`, `userStories`, `tasks`) and updating `activity_main.xml` plus `MainActivity` to render sections and loading cleanly.

Implemented the screen exactly as requested with a minimal XML UI and structured output rendering.

### What’s now in place

- **Large multiline input** for raw client brief (`EditText`)
- **Button** labeled **`Generate Dev Tasks`**
- **Loading state** via `ProgressBar` (shown while generating)
- **Structured result section**:
  - Epic
  - User Stories (numbered)
  - Tasks (bullet list)

### Updated files

- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/java/com/example/meetingtask/MainActivity.kt`
- `app/src/main/java/com/example/meetingtask/presentation/brief/BriefGeneratorUiState.kt`
- `app/src/main/java/com/example/meetingtask/presentation/brief/BriefGeneratorViewModel.kt`
- `app/src/main/java/com/example/meetingtask/domain/model/JiraTicketDraft.kt`
- `app/src/main/java/com/example/meetingtask/ai/service/FakeAiTicketGenerator.kt`
- `app/src/main/res/values/strings.xml`

### AI keys folder created

- `ai/keys/README.md`

You can paste your API keys/config files into `ai/keys/` as requested.

---

**User**

Create an AI service layer in Kotlin that takes a raw client brief string and returns structured JSON output.

Do NOT assume backend exists.
Instead:

Create an interface: AiTicketGenerator
Create a mock implementation first

Output format MUST be:
{
"epic": "",
"user_stories": [],
"tasks": []
}

Ensure strict parsing-ready structure.

---

**Cursor**

I’ll add a dedicated AI service contract for raw brief → strict JSON string, then provide a mock implementation that always returns parsing-ready JSON with exactly `epic`, `user_stories`, and `tasks`. I’m updating the `ai/service` layer now.

I’m wiring the data layer to consume the JSON output and parse it strictly into domain fields so the app remains functional with the new AI service contract.

Implemented a proper AI service layer with strict JSON output and a mock-first implementation.

### Added/updated AI service contract

- `app/src/main/java/com/example/meetingtask/ai/service/AiTicketGenerator.kt`

```kotlin
interface AiTicketGenerator {
    fun generateTicketJson(rawClientBrief: String): String
}
```

### Mock implementation (strict JSON)

- `app/src/main/java/com/example/meetingtask/ai/service/FakeAiTicketGenerator.kt`

It now returns parsing-ready JSON via `JSONObject`/`JSONArray` with exact required keys:

```json
{
  "epic": "",
  "user_stories": [],
  "tasks": []
}
```

### Wired into current app flow

To keep the app functional with this new contract, I also updated:

- `app/src/main/java/com/example/meetingtask/data/repository/ClientBriefRepositoryImpl.kt`
  - Calls `generateTicketJson(rawClientBrief)`
  - Strictly parses `epic`, `user_stories`, `tasks` from JSON
- `app/src/main/java/com/example/meetingtask/MainActivity.kt`
  - Updated repository construction to match new AI service signature

No backend assumptions were introduced; this is fully local/mock-driven.

---

**User**

Now generate task click use the OpenAi Api to generate epic task and user stories

---

**Cursor**

I’ll wire a real OpenAI-backed generator for the button click, reading the API key from `local.properties` into `BuildConfig`, then call the API and parse strict JSON (`epic`, `user_stories`, `tasks`). I’m updating manifest, Gradle config, AI service implementation, and ViewModel threading now.

I’ve added the OpenAI service class; next I’m wiring it into dependency creation with fallback behavior and moving generation off the main thread so network calls won’t block/crash UI.

Done — clicking **Generate Dev Tasks** now uses OpenAI (when key is present) to generate **epic + user stories + tasks**.

### What I changed

- Added real OpenAI service:
  - `app/src/main/java/com/example/meetingtask/ai/service/OpenAiTicketGenerator.kt`
  - Sends `POST https://api.openai.com/v1/chat/completions`
  - Uses `response_format: { "type": "json_object" }`
  - Normalizes output to strict JSON:
    - `epic` (string)
    - `user_stories` (array of strings)
    - `tasks` (array of strings)

- Kept/mock layer intact:
  - `AiTicketGenerator` still interface-based
  - `FakeAiTicketGenerator` remains for fallback

- Wired runtime selection in UI layer:
  - `MainActivity` now chooses:
    - `OpenAiTicketGenerator(BuildConfig.OPENAI_API_KEY)` if key exists
    - otherwise `FakeAiTicketGenerator()`

- Made network-safe generation:
  - `BriefGeneratorViewModel` now runs generation in a background thread (so no main-thread network crash)

- Added internet permission:
  - `app/src/main/AndroidManifest.xml` includes `android.permission.INTERNET`

- Added API key config from `local.properties`:
  - `app/build.gradle.kts` now reads `OPENAI_API_KEY` from `local.properties`
  - Exposes it as `BuildConfig.OPENAI_API_KEY`

---

### What you need to add now

In `local.properties`, add:

```properties
OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxx
```

Then run the app and click **Generate Dev Tasks**.

Build wasn’t runnable in this environment because Java runtime is missing here, but lint checks are clean and code wiring is complete.

---

**User**

OPENAI_API_KEY is already added in local.propeties file and model used should be 4.1 mini

---

**Cursor**

I’ll switch the OpenAI model to `gpt-4.1-mini` in the service call and keep everything else unchanged. I’ll also quickly re-check lint after the edit.

Updated successfully: the OpenAI request now uses **`gpt-4.1-mini`** in `OpenAiTicketGenerator`.

Also verified — no lint errors in that file.

Small note: you wrote `local.propeties`; Android reads from `local.properties` (with `r`). If your key is in `local.properties`, you’re good.

---

**User**

e: file:///Users/brainxtechnologies/StudioProjects/MeetingTask/app/build.gradle.kts:6:23: Unresolved reference: util
Resolve this issue 

---

**Cursor**

I’ll fix the Gradle Kotlin DSL reference error in `app/build.gradle.kts` by correcting how `Properties` is referenced, then re-check the file for lint errors.

Fixed. The Gradle Kotlin DSL error came from `java.util.Properties()` resolution in script context.

### Change made in `app/build.gradle.kts`

- Added:
  - `import java.util.Properties`
- Replaced:
  - `java.util.Properties()` → `Properties()`

No lint errors now in that file.

---

**User**

Create a Kotlin class called PromptBuilder.

It should convert raw client brief into a strict AI prompt:

Rules:

Never assume missing requirements
If info is missing, mark as "NEEDS CLARIFICATION"
Output must be structured Jira-ready format

Example prompt output:
"Convert this client brief into Jira tickets. Do not assume missing details..."

Implement reusable function:
fun buildPrompt(input: String): String

---

**Cursor**

I’ll add a reusable `PromptBuilder` class with `buildPrompt(input: String)` and wire `OpenAiTicketGenerator` to use it, so prompt logic is centralized and strict per your rules.

Added a reusable `PromptBuilder` and integrated it into OpenAI generation.

### Created

- `app/src/main/java/com/example/meetingtask/ai/mapper/PromptBuilder.kt`

It includes:

- `fun buildPrompt(input: String): String`
- Strict instructions:
  - never assume missing requirements
  - mark missing info as `"NEEDS CLARIFICATION"`
  - enforce Jira-ready strict JSON shape:
    - `{"epic":"","user_stories":[],"tasks":[]}`

### Integrated

- Updated `app/src/main/java/com/example/meetingtask/ai/service/OpenAiTicketGenerator.kt`
  - Injects `PromptBuilder` (default instance)
  - Uses `promptBuilder.buildPrompt(rawClientBrief)` for the user message

No lint issues after the changes.

---

**User**

instead of text field add option to add pdf file and read data from pdf 

---

**Cursor**

I’ll replace manual text input with a PDF upload flow: add a **Select PDF** button, extract text from the chosen PDF, and pass that extracted brief into generation. I’m updating layout, activity wiring, and adding a PDF parsing dependency now.

I’m finishing the PDF integration in `MainActivity`: document picker, PDF text extraction, and feeding extracted brief text into your existing generate flow.

Done. The input flow is now PDF-based instead of a text field.

### What changed

- Replaced text input UI with:
  - `Select Client Brief PDF` button
  - status text showing selected/read result
- Kept `Generate Dev Tasks` button
- On PDF select:
  - app opens document picker
  - reads text from PDF
  - stores extracted brief internally
- On Generate click:
  - uses extracted PDF text as input for OpenAI flow

### Files updated

- `app/src/main/res/layout/activity_main.xml`
  - removed `clientBriefInput`
  - added `selectPdfButton`
  - added `selectedPdfText`

- `app/src/main/java/com/example/meetingtask/MainActivity.kt`
  - added PDF picker via `ActivityResultContracts.OpenDocument`
  - added PDF text extraction logic
  - wired extracted text to `viewModel.onGenerateClicked(...)`

- `app/build.gradle.kts`
  - added PDF parsing dependency:
    - `implementation("com.tom-roush:pdfbox-android:2.0.27.0")`

### Notes

- This supports text-based PDFs (not scanned image PDFs with OCR).
- If needed, next I can add:
  - PDF file name display
  - disabled Generate button until valid PDF text is loaded
  - OCR fallback for scanned PDFs.

---

**User**

instead of epic stories and task in text format generate developers tickets that can be import directly in jira 

---

**Cursor**

I’ll switch generation from display-only sections to **Jira import-ready tickets** by returning structured ticket rows from AI and generating a proper CSV (with Jira-compatible columns) that you can export/import directly. I’m updating model, parser, prompt, and UI output/export flow now.

I’m converting the AI/output pipeline to a **Jira CSV ticket format** (Epic/Story/Task rows) and adding an **Export CSV** action so you can import directly into Jira. I’ll now update model, parser, prompt contract, AI normalizer, and UI bindings together.

Implemented. The app now generates **Jira import-ready developer tickets** instead of plain Epic/Stories/Tasks text.

### What changed

- AI output contract switched to strict ticket rows:
  - JSON shape:
    - `{"tickets":[{"summary":"","issue_type":"","description":"","epic_name":"","parent_summary":""}]}`
- OpenAI prompt and normalizer now enforce Jira-import fields and valid issue types (`Epic`, `Story`, `Task`).
- Repository now converts AI ticket rows into **CSV** with Jira-compatible columns:
  - `Summary, Issue Type, Description, Epic Name, Parent Summary`
- UI now shows:
  - generated ticket count
  - CSV preview
  - **Export CSV for Jira Import** button
- Export flow added:
  - saves `jira-import-tickets.csv` via Android document picker

### Updated files

- `app/src/main/java/com/example/meetingtask/ai/mapper/PromptBuilder.kt`
- `app/src/main/java/com/example/meetingtask/ai/service/OpenAiTicketGenerator.kt`
- `app/src/main/java/com/example/meetingtask/ai/service/FakeAiTicketGenerator.kt`
- `app/src/main/java/com/example/meetingtask/data/repository/ClientBriefRepositoryImpl.kt`
- `app/src/main/java/com/example/meetingtask/domain/model/JiraImportTicket.kt` (new)
- `app/src/main/java/com/example/meetingtask/domain/model/JiraTicketDraft.kt`
- `app/src/main/java/com/example/meetingtask/presentation/brief/BriefGeneratorUiState.kt`
- `app/src/main/java/com/example/meetingtask/presentation/brief/BriefGeneratorViewModel.kt`
- `app/src/main/java/com/example/meetingtask/MainActivity.kt`
- `app/src/main/res/layout/activity_main.xml`

### How to use

1. Select client brief PDF.
2. Click **Generate Jira CSV**.
3. Click **Export CSV for Jira Import**.
4. Import that CSV into Jira (CSV importer mapping to the 5 columns above).

If you want, I can next align the CSV columns exactly to your Jira cloud project’s importer template (field names sometimes vary by Jira setup).

---

**User**

Also give option to download pdf for  human-readable reports

---

**Cursor**

I’ll add a second export action for a **human-readable PDF report** (alongside CSV), wire it to document save picker, and generate a clean report directly on-device from the generated tickets. I’m updating layout, UI state, and `MainActivity` now.

I’m adding PDF report export wiring in `MainActivity` (new button, save dialog, and multi-line PDF generation) and then I’ll run lint checks.

Added. You now have a **download PDF option** for a human-readable report.

### What’s new

- New button in UI:
  - `Export Human-Readable PDF Report`
- PDF export flow:
  - Opens save picker
  - Writes a readable report PDF on device
  - Includes generated ticket content (issue type, summary, epic, parent, description)
- Button enabled only when generated report content exists.

### Files updated

- `app/src/main/res/layout/activity_main.xml`
  - added `exportPdfButton`
- `app/src/main/java/com/example/meetingtask/presentation/brief/BriefGeneratorUiState.kt`
  - added `reportContent`
- `app/src/main/java/com/example/meetingtask/presentation/brief/BriefGeneratorViewModel.kt`
  - builds human-readable report text from generated tickets
- `app/src/main/java/com/example/meetingtask/MainActivity.kt`
  - added PDF save launcher
  - added `savePdfReport(...)` using `PdfDocument`
  - wired new export button and state enablement

Lint checks are clean for all touched files.

---

**User**

add build folder in gitignore file

---

**Cursor**

I’ll update `.gitignore` to ignore build folders. I’m checking existing entries first to avoid duplicates.

Added build-folder ignore rule in `.gitignore`.

### Updated

- `/.gitignore`
  - already had `/build`
  - added `**/build/` to ignore build directories in all modules (e.g. `app/build/`, future module builds)



https://chatgpt.com/share/69fb07b8-71a8-8323-9498-bc3ec9152dfb

