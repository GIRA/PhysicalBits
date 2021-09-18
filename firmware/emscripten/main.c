/*
 * Copyright 2011 The Emscripten Authors.  All rights reserved.
 * Emscripten is available under two separate licenses, the MIT license and the
 * University of Illinois/NCSA Open Source License.  Both these licenses can be
 * found in the LICENSE file.
 */

#include <stdio.h>
#include "Arduino.h"
#include "Simulator.h"

void setup() {
  printf("_____________");
  printf("Testing basic GPIO\n");
  {
    pinMode(13, OUTPUT);
    digitalWrite(13, HIGH);
    int val = digitalRead(13);
    printf("PIN 13: %d\n", val);
    digitalWrite(13, LOW);
    val = digitalRead(13);
    printf("PIN 13: %d\n", val);
  }

  printf("_____________");
  printf("Testing serial\n");
  {
    Serial.begin(9600);

    Serial_write((char*)"RICHO", 5);

    printf("AVAILABLE: %d\n", Serial.available());
    while (Serial.available()) {
      char val = Serial.read();
      printf("READ: %d\n", val);
    }

    Serial.write(42);
    Serial.write(43);
    Serial.write(44);
    Serial.write(45);

    char buf[10];
    int count = Serial_readInto(buf, sizeof(buf));
    for (int i = 0; i < count; i++) {
      printf("WRITE: %d\n", buf[i]);
    }
  }
}

void loop() {

}

int main() {
  printf("hello, world!\n");
  setup();
  return 0;
}
