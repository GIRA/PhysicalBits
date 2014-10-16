#pragma once

#include "PE.h"
#include "StackArray.h"
#include "StackProgram.h"

class StackVM {

public:
	StackVM(void) {
		_stack = new StackArray();
    }
	~StackVM(void) {
		delete _stack;
	}

	void executeProgram(StackProgram*, PE*);


private:

	PE * _pe;

	int _ip;
	StackArray * _stack;
	StackProgram * _currentProgram;
	
	unsigned char nextBytecode(void);
	void executeBytecode(unsigned char);
	void executePrimitive(unsigned char);

};

