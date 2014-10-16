#include "StackArray.h"


void StackArray::push(long element) {
	_elements[_pointer] = element;
	_pointer++;
}

long StackArray::pop(void) {
	_pointer--;
	long result = _elements[_pointer];	
	return result;
}

long StackArray::top(void) {
	return _elements[_pointer - 1];
}

void StackArray::reset(void) {
	_pointer = 0;
}

bool StackArray::overflow(void) {
	return _pointer >= MAX_SIZE;
}
