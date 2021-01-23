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
void* uzi_pointer(uint32 value, Error& error);


/******************************STACK******************************/

void stack_push(float element, Error& error);
void stack_restoreFrom(float* source, uint16 size, Error& error);
void stack_saveTo(float* dest);
float stack_pop(Error& error);
void stack_discard(uint16 amount, Error& error);
void stack_reset();
uint16 stack_size();
uint16 stack_getPointer();
float stack_getElementAt(uint16 index, Error& error);
void stack_setElementAt(uint16 index, float value, Error& error);

void stack_pushPointer(void* pointer, Error& error);
void* stack_popPointer(Error& error);