#pragma once

const int MAX_SIZE = 100;

class StackArray {

public:
	StackArray(void) {
		_pointer = 0;
	}
	~StackArray(void) {}

	void push(long);
	long pop(void);
	long top(void);
	void reset(void);
	bool overflow(void);

private:
	
	long _elements[MAX_SIZE];
	unsigned int _pointer;

};

