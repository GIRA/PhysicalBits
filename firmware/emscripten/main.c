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


int main() {
  printf("Simulator loaded successfully!\n");
  EM_ASM( Simulator.init() );
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
