#pragma once

#include "types.h"
#include "Arduino.h"
#include "Errors.h"

#define uzi_create(T)			((T*)uzi_malloc(sizeof(T)))
#define uzi_createArray(T, L)	((T*)uzi_malloc(sizeof(T) * (L)))

void* uzi_malloc(size_t size);
void uzi_memreset();
uint16 uzi_available();
uint16 uzi_used();


/******************************STACK******************************/

void stack_push(float element);
void stack_copyFrom(float* source, uint16 size);
void stack_copyTo(float* dest);
float stack_pop(void);
void stack_discard(uint16 amount);
float stack_top(void);
void stack_reset(void);
bool stack_hasError(void);
Error stack_getError(void);
uint16 stack_getPointer(void);
void stack_setPointer(uint16 value);
float stack_getElementAt(uint16 index);
void stack_setElementAt(uint16 index, float value);