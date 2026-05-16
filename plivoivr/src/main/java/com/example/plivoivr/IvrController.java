package com.example.plivoivr;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/ivr")
public class IvrController {


    private final String AUTH_ID = "MAYMYZMWEYNMM1YTA2MW";
    private final String AUTH_TOKEN = "ZjMwYTI5NmEtMWY2Zi00ZGZkLWEyZGUtZjM5MzZh";
    private final String PLIVO_NUMBER = "+918035736861"; 
    private final String YOUR_NUMBER = "+919445456541";
    
    private final String BASE_URL = "https://strive-gladiator-curliness.ngrok-free.dev"; 

    private final String CORRECT_OTP = "2905"; // My birthdate in DDMM format

    @GetMapping("/make-call")
        public String makeCall() {
            try {
                String url = "https://api.plivo.com/v1/Account/" + AUTH_ID + "/Call/";
                
                // Build the JSON payload manually to avoid library clashes
                String jsonBody = String.format(
                    "{\"from\":\"%s\",\"to\":\"%s\",\"answer_url\":\"%s/ivr/answer\",\"answer_method\":\"POST\"}",
                    PLIVO_NUMBER, YOUR_NUMBER, BASE_URL
                );

                // Create Basic Authentication header
                String auth = AUTH_ID + ":" + AUTH_TOKEN;
                String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes());

                // Send the request natively using Java's built-in HTTP Client
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Basic " + base64Auth)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return "Call initiated successfully to " + YOUR_NUMBER;
                } else {
                    return "Failed to initiate call. Status: " + response.statusCode() + " - " + response.body();
                }
            } catch (Exception e) {
                return "Error initiating call: " + e.getMessage();
            }
        }

    // 1. Initial Call Answer - Prompt for OTP
    @PostMapping(value = "/answer", produces = "application/xml")
    public String answerCall() {
        return "<Response>" +
               "  <GetDigits action=\"" + BASE_URL + "/ivr/verify-otp\" method=\"POST\" numDigits=\"4\" timeout=\"10\">" +
               "    <Speak>Welcome. Please enter your 4-digit birthdate O T P to authenticate.</Speak>" +
               "  </GetDigits>" +
               "  <Speak>We did not receive any input. Hanging up.</Speak>" +
               "  <Hangup/>" +
               "</Response>";
    }

    // 2. Verify OTP Layer
    @PostMapping(value = "/verify-otp", produces = "application/xml")
    public String verifyOtp(@RequestParam(value = "Digits", required = false) String digits) {
        if (digits != null && digits.equals(CORRECT_OTP)) {
            // Success -> Move to Level 1 (Language Selection)
            return "<Response>" +
                   "  <Redirect method=\"POST\">" + BASE_URL + "/ivr/language-menu</Redirect>" +
                   "</Response>";
        } else {
            // Failure -> Reprompt by redirecting back to answer call
            return "<Response>" +
                   "  <Speak>Incorrect O T P. Please try again.</Speak>" +
                   "  <Redirect method=\"POST\">" + BASE_URL + "/ivr/answer</Redirect>" +
                   "</Response>";
        }
    }

    // 3. Level 1: Language Selection Menu
    @PostMapping(value = "/language-menu", produces = "application/xml")
    public String languageMenu() {
        return "<Response>" +
               "  <GetDigits action=\"" + BASE_URL + "/ivr/level-2\" method=\"POST\" numDigits=\"1\" timeout=\"10\">" +
               "    <Speak>For English, press 1. Para Español, presione 2.</Speak>" +
               "  </GetDigits>" +
               "  <Speak>Input timeout. Hanging up.</Speak>" +
               "  <Hangup/>" +
               "</Response>";
    }

    // 4. Level 2: Action Selection (Branching based on language choice)
    @PostMapping(value = "/level-2", produces = "application/xml")
    public String level2(@RequestParam(value = "Digits", required = false) String langChoice) {
        if ("1".equals(langChoice)) { // English selected
            return "<Response>" +
                   "  <GetDigits action=\"" + BASE_URL + "/ivr/final-action?lang=en\" method=\"POST\" numDigits=\"1\" timeout=\"10\">" +
                   "    <Speak>Press 1 to play a short audio message. Press 2 to connect to a live associate.</Speak>" +
                   "  </GetDigits>" +
                   "  <Hangup/>" +
                   "</Response>";
        } else if ("2".equals(langChoice)) { // Spanish selected
            return "<Response>" +
                   "  <GetDigits action=\"" + BASE_URL + "/ivr/final-action?lang=es\" method=\"POST\" numDigits=\"1\" timeout=\"10\">" +
                   "    <Speak>Presione 1 para escuchar un mensaje de audio corto. Presione 2 para comunicarse con un asociado.</Speak>" +
                   "  </GetDigits>" +
                   "  <Hangup/>" +
                   "</Response>";
        } else {
            // Invalid input at Level 1 -> Repeat Language Selection
            return "<Response>" +
                   "  <Speak>Invalid option.</Speak>" +
                   "  <Redirect method=\"POST\">" + BASE_URL + "/ivr/language-menu</Redirect>" +
                   "</Response>";
        }
    }

    // 5. Final Action Execution
    @PostMapping(value = "/final-action", produces = "application/xml")
    public String finalAction(
            @RequestParam(value = "Digits", required = false) String actionChoice,
            @RequestParam(value = "lang", defaultValue = "en") String lang) {
        
        boolean isEn = "en".equals(lang);

        if ("1".equals(actionChoice)) {
            // Action 1: Play audio message
            String audioUrl = "https://s3.amazonaws.com/plivocloud/music.mp3"; // Plivo standard sample audio
            return "<Response>" +
                   "  <Speak>" + (isEn ? "Playing your message now." : "Reproduciendo su mensaje ahora.") + "</Speak>" +
                   "  <Play>" + audioUrl + "</Play>" +
                   "  <Hangup/>" +
                   "</Response>";
                   
        } else if ("2".equals(actionChoice)) {
            // Action 2: Forward to live associate
            String placeholderNumber = "+912264236412"; 
            return "<Response>" +
                   "  <Speak>" + (isEn ? "Connecting you to an associate." : "Conectando con un asociado.") + "</Speak>" +
                   "  <Dial>" +
                   "    <Number>" + placeholderNumber + "</Number>" +
                   "  </Dial>" +
                   "</Response>";
        } else {
            // Invalid input at Level 2 -> Redirect back to Level 2 with the correct context language
            String errorMsg = isEn ? "Invalid option." : "Opción inválida.";
            String langDigit = isEn ? "1" : "2";
            return "<Response>" +
                   "  <Speak>" + errorMsg + "</Speak>" +
                   "  <Redirect method=\"POST\">" + BASE_URL + "/ivr/level-2?Digits=" + langDigit + "</Redirect>" +
                   "</Response>";
        }
    }
}