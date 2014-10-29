#pragma once

const int MAX_SIZE = 100;

class StackArray {

public:
	StackArray(void) {
		_pointer = 0;
	}
	~StackArray(void) {}

	void push(float);
	float pop(void);
	float top(void);
	void reset(void);
	bool overflow(void);

private:
	
	float _elements[MAX_SIZE];
	unsigned int _pointer;

};

