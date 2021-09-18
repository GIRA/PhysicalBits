/*
 * Copyright 2011 The Emscripten Authors.  All rights reserved.
 * Emscripten is available under two separate licenses, the MIT license and the
 * University of Illinois/NCSA Open Source License.  Both these licenses can be
 * found in the LICENSE file.
 */

#include <stdio.h>
#include "Arduino.h"

void setup() {
  pinMode(13, OUTPUT);
}

void loop() {

}

int main() {
  printf("hello, world!\n");
  setup();
  return 0;
}
