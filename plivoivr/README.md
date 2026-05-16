# InspireWorks — Multi-Level IVR System with OTP Authentication

This is a robust Spring Boot application that integrates with the Plivo Voice API to deliver a multi-level Interactive Voice Response (IVR) flow. It features secure caller authentication via an OTP challenge, contextual language routing, and final-action handling including audio streaming and live call forwarding.

---

## 🚀 Architectural Design Flow

1. **Outbound Trigger (`/ivr/make-call`):** Uses Java's native asynchronous `HttpClient` to cleanly trigger an outbound request to Plivo's API, bypassing external library dependency naming clashes.
2. **Level 0 Authentication (`/ivr/answer`):** Captures 4-digit DTMF input via `<GetDigits>`. If validation fails at `/ivr/verify-otp`, it cycles back gracefully to the prompt.
3. **Level 1 Language Menu (`/ivr/language-menu`):** Provides a branching pipeline supporting English (1) or Spanish (2).
4. **Level 2 Action Menu (`/ivr/level-2`):** Delivers custom-localized prompts asking the user to choose between an audio message (1) or associate routing (2).
5. **Final Execution (`/ivr/final-action`):** Executes high-fidelity media rendering via `<Play>` or bridges standard carrier connections natively to a live associate via `<Dial>`.

---

## 🛠️ Project Configuration & Credentials

Before compiling the source code, open `src/main/java/com/example/plivoivr/IvrController.java` and verify or update the following configuration properties:

| Constant Variable | Purpose / Description | Expected Value |
| :--- | :--- | :--- |
| `AUTH_ID` | Your official Plivo account unique identifier. | `"MAYMYZMWEYNMM1YTA2MW"` |
| `AUTH_TOKEN` | Your secure Plivo account authentication secret. | `"ZjMwYTI5NmEtMWY2Zi00ZGZkLWEyZGUtZjM5MzZh"` |
| `PLIVO_NUMBER` | The leased/source number routing the outbound call. | `"+918035736861"` |
| `YOUR_NUMBER` | The destination handset phone number (E.164 format). | `"+919445456541"` |
| `CORRECT_OTP` | The hardcoded 4-digit validation threshold (DDMM format). | `"2905"` |

---

## 💻 Setup, Running, and Testing Instructions

Follow these instructions chronologically to launch the application, establish the public proxy tunnel, and execute the end-to-end interactive IVR testing protocol.

### Phase 1: Environment Prerequisites

Ensure your local development machine has the following dependencies configured globally:
* **Java Development Kit (JDK):** Version 21 or higher.
* **Apache Maven:** Version 3.9+ (Alternatively, use the bundled Maven wrapper `.\mvnw`).
* **ngrok CLI:** Installed and authenticated via your personal ngrok auth token.

### Phase 2: Launching the Public Proxy Tunnel (ngrok)

Plivo utilizes webhooks to fetch call configuration instructions dynamically. Because Plivo's cloud platform cannot access a local host address directly, you must create a public secure tunnel.

1. Open a fresh command prompt or terminal window.
2. Spin up a public HTTPS tunnel mapping directly to your local Spring Boot port (`8080`) by running:
   ```bash
   ngrok http 8080
   ```
3. Locate the **Forwarding** line in your active ngrok terminal screen (it will look similar to `https://strive-gladiator-curliness.ngrok-free.app`). 
4. Copy that complete URL string, return to `IvrController.java`, and update the configuration constant:
   ```java
   private final String BASE_URL = "YOUR_ACTIVE_NGROK_FORWARDING_URL";
   ```

> ⚠️ **Critical Note:** Keep this terminal window open. If you close or restart ngrok, a new random subdomain will be provisioned, and you must update the `BASE_URL` property accordingly.

### Phase 3: Compiling and Booting the Spring Boot App

With the web proxy configured, you can launch the application container.

#### Option A: Execution via VS Code (Recommended)
1. Navigate to the Explorer side-panel in VS Code.
2. Open `src/main/java/com/example/plivoivr/PlivoivrApplication.java`.
3. Locate the `main` method and click the inline **Run** text link superimposed directly above it.

#### Option B: Execution via Terminal / Command Line
1. Open a separate terminal positioned in the project's root folder (`plivoivr/`).
2. Run the application using the local Maven wrapper:
   ```powershell
   # On Windows PowerShell
   .\mvnw spring-boot:run
   
   # On macOS / Linux Terminal
   ./mvnw spring-boot:run
   ```
3. Confirm the application boots cleanly by checking the console trailing logs for this confirmation timestamp:  
   `Tomcat started on port 8080 (http) with context path '/'`

---

## 🧪 Phase 4: Comprehensive Testing Protocol (E2E)

### Step 1: Launch the Communication Portal

Open any desktop web browser and navigate to the application root home address:
```text
http://localhost:8080/
```
You will be greeted by the **InspireWorks — IVR Control Center** user interface. Click the blue **Launch IVR Verification Flow** button. The interface will communicate asynchronously with your controller, and your phone will ring within 5–10 seconds.

### Step 2: Validate the Multi-Level IVR Voice Flow

Once you receive the call on your target handset, execute the following precise menu steps to test all core branches and validation guardrails:

1. **Answer Call & Trigger Level 0 Challenge:** The automated text-to-speech engine will announce: *"Welcome. Please enter your 4-digit birthdate O T P to authenticate."*
2. **Execute Error-Handling Path Evaluation:** Deliberately enter an invalid 4-digit combination (e.g., `1111`) via your phone's keypad. The engine will instantly parse the digit webhook, respond with *"Incorrect O T P. Please try again."*, and gracefully cycle you back to the initial prompt.
3. **Execute Success-Handling Path Evaluation:** Enter your correct hardcoded birthdate OTP code: **`2905`**. The backend will validate the parameter and cleanly route the call state forward to Level 1.
4. **Navigate Level 1 (Language Tree Selection):** The menu will prompt: *"For English, press 1. Para Español, presione 2."* Press **`1`** on your keypad to proceed down the English architectural pipeline.
5. **Navigate Level 2 (Action Choice Selection):** The application logic detects your language choice and responds contextually: *"Press 1 to play a short audio message. Press 2 to connect to a live associate."*
6. **Trigger Final Action Branches:** * **Branch A (Media Streaming):** Press **`1`**. The platform will say *"Playing your message now."* and cleanly stream high-fidelity sample music directly through your phone line speaker via the `<Play>` XML directive.
   * **Branch B (Live Call Transfer):** Alternatively, re-trigger the call sequence and press **`2`** at this menu layer. The platform will state *"Connecting you to an associate."* and automatically route, execute, and bridge a secondary connection to the designated live proxy number (`+912264236412`) using the `<Dial>` XML element.