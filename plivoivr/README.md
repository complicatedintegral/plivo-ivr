# InspireWorks — Multi-Level IVR System with OTP Authentication

This is a robust Spring Boot application that integrates with the Plivo Voice API to deliver a multi-level Interactive Voice Response (IVR) flow. It features secure caller authentication via an OTP challenge, contextual language routing, and final-action handling (audio streaming and live call forwarding).

## 🚀 Architectural Design Flow
1. **Outbound Trigger (`/ivr/make-call`):** Uses Java's native asynchronous `HttpClient` to cleanly trigger an outbound request to Plivo's API without external library naming clashes.
2. **Level 0 Authentication (`/ivr/answer`):** Captures 4-digit input via `<GetDigits>`. If validation fails at `/ivr/verify-otp`, it cycles back gracefully. 
3. **Level 1 Language Menu (`/ivr/language-menu`):** Branching path supporting English (1) or Spanish (2).
4. **Level 2 Action Menu (`/ivr/level-2`):** Delivers custom-localized prompts asking the user to choose between an audio message (1) or associate routing (2).
5. **Final Execution (`/ivr/final-action`):** Executes high-fidelity media rendering via `<Play>` or bridges standard carrier connections natively via `<Dial>`.

## 🛠️ Required Configuration Credentials
To review or run this application, the following values are hardcoded inside `src/main/java/com/example/plivoivr/IvrController.java`:
- **Auth ID:** `MAYMYZMWEYNMM1YTA2MW`
- **Auth Token:** `ZjMwYTI5NmEtMWY2Zi00ZGZkLWEyZGUtZjM5MzZh`
- **Plivo Source Number:** `+918035736861`
- **Receiver (Target) Number:** `+919445456541`
- **Live Associate Forwarding Number:** `+912264236412`
- **Hardcoded OTP Verification Code:** `2905` (Birthdate in DDMM format)

## 💻 Setup & Local Execution Instructions

### Prerequisites
- Java 21 or higher
- Maven (or bundled wrapper)
- ngrok agent installed

### 1. Boot up the Spring Boot Engine
Navigate to the root directory of your project and run:
```bash
mvn spring-boot:run