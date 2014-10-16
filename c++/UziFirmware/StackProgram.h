#pragma once

#include "ReadStream.h"

class StackProgram {

public:
	StackProgram(ReadStream*);
	StackProgram(void);
	~StackProgram(void);

	long literalAt(int);
	long localAt(int);
	unsigned char bytecodeAt(int);
	bool shouldStepNow(long);
	void rememberLastStepTime(long);
	bool isStepping(void);
	void setStepping(bool);
	long stepTime(void);

private:
	
	bool _stepping;
	long _stepTime;
	long _lastStepTime;
	long * _literals;
	long * _locals;
	unsigned char * _bytecodes;

	long * parseSection(ReadStream*);
	unsigned char * parseBytecodes(ReadStream*);
};

