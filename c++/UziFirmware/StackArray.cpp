#include "StackArray.h"


void StackArray::push(float element) {
	_elements[_pointer] = element;
	_pointer++;
}

float StackArray::pop(void) {
	_pointer--;
	float result = _elements[_pointer];	
	return result;
}

float StackArray::top(void) {
	return _elements[_pointer - 1];
}

void StackArray::reset(void) {
	_pointer = 0;
}

bool StackArray::overflow(void) {
	return _pointer >= MAX_SIZE;
}
