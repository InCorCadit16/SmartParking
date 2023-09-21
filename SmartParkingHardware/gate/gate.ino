#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <WiFiClient.h>
#include <OV7670.h>
#include <Arduino.h>
#include <FreeRTOS.h>
#include <task.h>

#define PIR_PIN D2
#define FILTER_SIZE 5

// WiFi credentials
const char* ssid = "MOLDTELECOM_F58";
const char* password = "12345678";

// Server details
const char* serverAddress = "https://localhost:5001/check_licence"; 

// OV7670 pins
const int ov7670_reset = D5;
const int ov7670_pwdn = D6;

// Global variables
portMUX_TYPE mux = portMUX_INITIALIZER_UNLOCKED;
volatile uint8_t motionState;
uint8_t motionBuffer[FILTER_SIZE];
uint8_t motionBufferIndex;

// Task handles
TaskHandle_t pirTaskHandle;
TaskHandle_t filterTaskHandle;

// PIR sensor task
void pirTask(void* pvParameters) {
  (void)pvParameters;  // Unused parameter

  while (1) {
    uint8_t motion = digitalRead(PIR_PIN);

    // Update motion state
    portENTER_CRITICAL(&mux);
    motionState = motion;
    portEXIT_CRITICAL(&mux);

    // Delay before reading again
    vTaskDelay(pdMS_TO_TICKS(100));
  }
}

// Median filter task
void filterTask(void* pvParameters) {
  (void)pvParameters;  // Unused parameter

  while (1) {
    portENTER_CRITICAL(&mux);

    // Copy motion state into the buffer
    motionBuffer[motionBufferIndex++] = motionState;
    if (motionBufferIndex >= FILTER_SIZE) {
      motionBufferIndex = 0;
    }

    // Compute median
    uint8_t sortedBuffer[FILTER_SIZE];
    memcpy(sortedBuffer, motionBuffer, sizeof(motionBuffer));
    for (uint8_t i = 0; i < FILTER_SIZE - 1; i++) {
      for (uint8_t j = i + 1; j < FILTER_SIZE; j++) {
        if (sortedBuffer[i] > sortedBuffer[j]) {
          uint8_t temp = sortedBuffer[i];
          sortedBuffer[i] = sortedBuffer[j];
          sortedBuffer[j] = temp;
        }
      }
    }
    uint8_t median = sortedBuffer[FILTER_SIZE / 2];

    portEXIT_CRITICAL(&mux);

    // Use the filtered motion state as needed
    if (median == HIGH) {
      // Motion detected
      capture();
    }

    // Delay before filtering again
    vTaskDelay(pdMS_TO_TICKS(100));
  }
}

void capture() {
  camera.startCapture();
  while (!camera.captureReady()) {
    delay(1);
  }
  camera.readFrameBuffer();

  // Connect to the server
  if (http.begin(client, serverAddress)) {
    // Set the Content-Type header to multipart/form-data
    http.addHeader("Content-Type", "multipart/form-data");
    
    // Create a filename for the photo
    String filename = "/photo.jpg";
    
    // Begin the multipart/form-data request
    http.beginRequest();
    http.write("--");
    http.print(http.boundary());
    http.println();
    http.print("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"");
    http.println();
    http.println("Content-Type: image/jpeg");
    http.println();
    http.sendRequestHeader();
    
    // Send the photo data
    http.write(camera.getFrameBuffer(), camera.getFrameBufferSize());
    
    // End the multipart/form-data request
    http.println();
    http.write("--");
    http.print(http.boundary());
    http.write("--");
    http.println();
    http.endRequest();
    
    // Get the HTTP response code
    int httpResponseCode = http.responseStatusCode();
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    
    // Check if the photo was successfully uploaded
    if (httpResponseCode == HTTP_CODE_OK) {
      Serial.println("Photo uploaded successfully");
    } else {
      Serial.println("Failed to upload photo");
    }
  } else {
    Serial.println("Failed to connect to server");
  }
  
  // Disconnect from the server
  http.end();
}

void setup() {
  Serial.begin(115200);

  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }

  // Configure PIR sensor pin
  pinMode(PIR_PIN, INPUT);

  // Initialize the OV7670 camera
  camera.begin(Wire);
  camera.reset(ov7670_reset, ov7670_pwdn);
  camera.setResolution(OV7670_RESOLUTION_VGA);
  camera.setBitWindow(OV7670_WINDOW_VGA);
  
  delay(1000); // Wait for camera initialization

  // Create tasks
  xTaskCreate(pirTask, "PIRTask", 2048, NULL, 1, &pirTaskHandle);
  xTaskCreate(filterTask, "FilterTask", 2048, NULL, 1, &filterTaskHandle);
}

void loop() {
  // Empty loop as tasks handle the work
}