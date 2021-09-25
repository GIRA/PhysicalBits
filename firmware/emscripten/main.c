/*
 * Copyright 2011 The Emscripten Authors.  All rights reserved.
 * Emscripten is available under two separate licenses, the MIT license and the
 * University of Illinois/NCSA Open Source License.  Both these licenses can be
 * found in the LICENSE file.
 */

#include <emscripten.h>
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

void handshake() {
  Sketch_setMillis(0);
  Sketch_setup();

  unsigned char connection_req[3] = {255, 0, 8};
  Serial_write((char*)connection_req, sizeof(connection_req));
  Sketch_loop();

  unsigned char hshake;
  {
    char buf[10];
    int read_bytes = Serial_readInto(buf, sizeof(buf));
    if (read_bytes <= 0) { printf("HANDSHAKE NOT RECEIVED\n"); return; }
    hshake = buf[0];
  }

  unsigned char send = (8+hshake) % 256;
  unsigned char out_buf[1] = {send};
  Serial_write((char*)out_buf, sizeof(out_buf));
  Sketch_loop();

  unsigned char ack;
  {
    char buf[10];
    int read_bytes = Serial_readInto(buf, sizeof(buf));
    if (read_bytes <= 0) { printf("ACK NOT RECEIVED\n"); return; }
    ack = buf[0];
  }

  if (send != ack) { printf("CONNECTION FAILED!\n"); return; }
}


int main() {
  printf("Simulator loaded successfully!\n");
  EM_ASM( Simulator.start() );
}

/*
int main() {
  printf("hello, world!\n");
  handshake();

  //unsigned char program[] = {0, 0, 5, 1, 0, 128, 1, 13};
  unsigned char program[] = {0, 0, 12, 1, 2, 4, 13, 5, 3, 232, 160, 4, 2, 131, 162};
  Serial_write((char*)program, sizeof(program));
  printf("D13: %d\n", GPIO_getPinValue(13));
  for (int i = 0; i < 100; i++) {
    Sketch_setMillis(i * 1000);
    Sketch_loop();
    printf("%ld -> D13: %d\n", Sketch_getMillis(), GPIO_getPinValue(13));
  }
  printf("bye!\n");
  return 0;
}
*/
