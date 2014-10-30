#pragma once

#include "PE.h"
#include "StackArray.h"
#include "Script.h"

class Interpreter {

public:
	Interpreter(void) {
		_stack = new StackArray();
    }
	~Interpreter(void) {
		delete _stack;
	}

	void executeScript(Script*, PE*);


private:

	PE * _pe;

	int _ip;
	StackArray * _stack;
	Script * _currentScript;
	
	unsigned char nextBytecode(void);
	void executeBytecode(unsigned char);
	void executePrimitive(unsigned char);

};

