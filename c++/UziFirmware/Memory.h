#pragma once

#include "types.h"
#include "Arduino.h"

#define uzi_create(T)			((T*)uzi_malloc(sizeof(T)))
#define uzi_createArray(T, L)	((T*)uzi_malloc(sizeof(T) * (L)))

void* uzi_malloc(size_t size);
void uzi_memreset();
uint16 uzi_available();