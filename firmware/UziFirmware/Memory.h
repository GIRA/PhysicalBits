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

void stack_push(float element, Error& error);
void stack_copyFrom(float* source, uint16 size, Error& error);
void stack_copyTo(float* dest);
float stack_pop(Error& error);
void stack_discard(uint16 amount, Error& error);
float stack_top(void);
void stack_reset(void);
uint16 stack_getPointer(void);
void stack_setPointer(uint16 value);
float stack_getElementAt(uint16 index, Error& error);
void stack_setElementAt(uint16 index, float value, Error& error);