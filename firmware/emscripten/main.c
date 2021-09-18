/*
 * Copyright 2011 The Emscripten Authors.  All rights reserved.
 * Emscripten is available under two separate licenses, the MIT license and the
 * University of Illinois/NCSA Open Source License.  Both these licenses can be
 * found in the LICENSE file.
 */

#include <stdio.h>
#include "Arduino.h"
#include "Servo.h"
#include "Simulator.h"

void test() {
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

  printf("_____________");
  printf("Testing EEPROM\n");
  {
    unsigned char value = EEPROM.read(127);
    printf("%d\n", value);
    value = 42;
    EEPROM.write(127, value);
    value = EEPROM.read(127);
    printf("%d\n", value);
  }


  printf("_____________");
  printf("Testing Servo\n");
  {
    Servo servos[5];
    for (int i = 0; i < 5; i++) {
      int pin = 3 + i;
      servos[i].attach(pin);
      servos[i].write(90);
      printf("%d -> %d\n", i, analogRead(pin));
      servos[i].detach();
    }
  }
}

int main() {
  printf("hello, world!\n");
  test();
  setup();
  loop();
  printf("bye!\n");
  return 0;
}
