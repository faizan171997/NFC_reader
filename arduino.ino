#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ESP8266WebServer.h>
#include <WiFiClient.h>

#include <SPI.h>
#include <PN532_SPI.h>
#include "PN532.h"

#ifndef STASSID
#define STASSID "The 192 Network 2.4G"
#define STAPSK  "Mumbai254"
#endif

#define AP_SSID "tap-n-auth-ap"


#define SERVER_IP "10.0.0.215:3000"

PN532_SPI pn532spi(SPI, D2);
PN532 nfc(pn532spi);

ESP8266WebServer server(80);

// vars
boolean serverStop = false;
String codes = "";

void handlePlain() {
  serverStop = true;
  if (server.method() != HTTP_POST) {
    server.send(405, "text/plain", "Method Not Allowed");
  } else {
    String code = server.arg("plain");
    Serial.println("Got code: " + code);
    while (codes.length() != 0) {
      int index = codes.indexOf(',');
      String thisCode = "";
      if (index != -1) {
        thisCode = codes.substring(0, index);
        codes = codes.substring(index + 1);
      } else {
        thisCode = codes.substring(0);
        codes = "";
      }
      if (code == thisCode) {
        server.send(200, "text/plain", "");
        Serial.println("Found a code match!");
        Serial.println("Waiting for a hash from Android ...");
        server.on("/postHash", handleHash);
        server.begin();
//        Serial.println("HTTP server started again."); 
        serverStop = false;
        while(!serverStop) {
//          Serial.println("Waiting for hash ...");
          server.handleClient();
          delay(1000);
        }
        return;
      }
    }
    Serial.println("None of the codes match; could be an imposter request.");
    server.send(401, "text/plain", "");
  }
}

void handleHash() {
  serverStop = true;
  if (server.method() != HTTP_POST) {
    server.send(405, "text/plain", "Method Not Allowed");
  } else {
    String hashVal = server.arg("plain");
    Serial.println("Got hash: " + hashVal);
    connectToWifi();
    if (postUnlock(hashVal) == 1) {
      Serial.println("Unlocking door ... ");
      server.send(200, "text/plain");
    } else {
      Serial.println("Invalid user.");
      server.send(401, "text/plain");
    }
  }
}

void nfcInit() {
  nfc.begin();
  uint32_t versiondata = nfc.getFirmwareVersion();
  if (! versiondata) {
    Serial.print("Didn't find PN53x board");
    while (1); // halt
  }
  nfc.SAMConfig();
}

void connectToWifi() {
  WiFi.begin(STASSID, STAPSK);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.print("Connected! IP address: ");
  Serial.println(WiFi.localIP());
}

void createAP() {
  WiFi.softAP(AP_SSID, "", 1, false, 4);

  IPAddress myIP = WiFi.softAPIP();
  Serial.print("AP IP address: ");
  Serial.println(myIP);
  server.on("/postCode", handlePlain);
  server.begin();
//  Serial.println("HTTP server started.");
  serverStop = false;
  Serial.println("Waiting for code ...");
  while(!serverStop) {
//    Serial.println("Waiting for code ...");
    server.handleClient();
    delay(1000);
  }
//  Serial.println("HTTP server stopped.");
}

//void postAccessCode() {
//  // wait for WiFi connection
//  if ((WiFi.status() == WL_CONNECTED)) {
//
//    WiFiClient client;
//    HTTPClient http;
//
//    Serial.print("[HTTP] begin...\n");
//    // configure traged server and url
//    http.begin(client, "http://" SERVER_IP "/"); //HTTP
//    http.addHeader("Content-Type", "application/json");
//
//    Serial.print("[HTTP] POST...\n");
//    // start connection and send HTTP header and body
//    int httpCode = http.POST("{\"key\":\"" KEY "\"}");
////    int httpCode = http.POST("");
//
//    // httpCode will be negative on error
//    if (httpCode > 0) {
//      // HTTP header has been send and Server response header has been handled
//      Serial.printf("[HTTP] POST... code: %d\n", httpCode);
//
//      // file found at server
//      if (httpCode == HTTP_CODE_OK) {
//        const String& payload = http.getString();
//        Serial.println("received payload:\n<<");
//        int index = payload.indexOf(':');
//        String accessCode = payload.substring(0, index);
//        String pass = payload.substring(index + 1);
//        Serial.println(accessCode);
//        Serial.println(pass);
//        Serial.println(payload);
//        createAP(accessCode, pass);
//        Serial.println(">>");
//      }
//    } else {
//      Serial.printf("[HTTP] POST... failed, error: %s\n", http.errorToString(httpCode).c_str());
//    }
//
//    http.end();
//  }
//}

void getAccessCodes() {
  // wait for WiFi connection
  if ((WiFi.status() == WL_CONNECTED)) {

    WiFiClient client;
    HTTPClient http;

//    Serial.print("[HTTP] begin...\n");
    // configure traged server and url
    http.begin(client, "http://" SERVER_IP "/getCodes"); //HTTP
    http.addHeader("Content-Type", "application/json");

//    Serial.print("[HTTP] GET...\n");
    // start connection and send HTTP header and body
    int httpCode = http.GET();
//    int httpCode = http.POST("");

    // httpCode will be negative on error
    if (httpCode > 0) {
      // HTTP header has been send and Server response header has been handled
//      Serial.printf("[HTTP] GET... code: %d\n", httpCode);

      // file found at server
      if (httpCode == HTTP_CODE_OK) {
        codes = http.getString();
//        Serial.println("received payload:\n<<");

        createAP();
        
//        Serial.println(">>");
      }
    } else {
//      Serial.printf("[HTTP] GET... failed, error: %s\n", http.errorToString(httpCode).c_str());
    }

    http.end();
  }
}

int postUnlock(String hashVal) {
  // wait for WiFi connection
  if ((WiFi.status() == WL_CONNECTED)) {

    WiFiClient client;
    HTTPClient http;

//    Serial.print("[HTTP] begin...\n");
    // configure traged server and url
    http.begin(client, "http://" SERVER_IP "/popCode"); //HTTP
    http.addHeader("Content-Type", "application/json");

//    Serial.print("[HTTP] POST...\n");
    // start connection and send HTTP header and body
    int httpCode = http.POST("{\"hash\":\"" + hashVal + "\"}");
//    int httpCode = http.POST("");

    // httpCode will be negative on error
    if (httpCode > 0) {
      // HTTP header has been send and Server response header has been handled
//      Serial.printf("[HTTP] POST... code: %d\n", httpCode);

      // file found at server
      if (httpCode == HTTP_CODE_OK) {
        const String& payload = http.getString();
//        Serial.println("received payload:\n<<");
//        Serial.println(payload);
//        Serial.println(">>");
        return payload.toInt();
      }
    } else {
//      Serial.printf("[HTTP] POST... failed, error: %s\n", http.errorToString(httpCode).c_str());
    }
    return 0;
    http.end();
  }
}

void showStatus(String msg) {
  Serial.println(msg.length() == 0 ? "Ready for taps!" : msg);
}

void setup() {

  Serial.begin(115200);

  Serial.println();
  Serial.println();
  Serial.println();

  nfcInit();

  connectToWifi();

  showStatus("");
}

void loop() {
  boolean success;
  uint8_t uid[] = { 0, 0, 0, 0, 0, 0, 0 };  // Buffer to store the returned UID
  uint8_t uidLength;                        // Length of the UID (4 or 7 bytes depending on ISO14443A card type)

  success = nfc.readPassiveTargetID(PN532_MIFARE_ISO14443A, &uid[0], &uidLength);
  
  if (success) {
    Serial.println("Found a card!");
    Serial.print("UID Length: ");Serial.print(uidLength, DEC);Serial.println(" bytes");
    Serial.print("UID Value: ");
    for (uint8_t i=0; i < uidLength; i++) 
    {
      Serial.print(" 0x");Serial.print(uid[i], HEX); 
    }
    Serial.println("");
    
    Serial.println("Waiting for an access code from Android ...");
    getAccessCodes();

    delay(60*60*1000);

    showStatus("");
  }
  delay(1000);
}
