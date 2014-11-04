#pragma once

#include "ReadStream.h"

class Script {

public:
	Script(ReadStream*);
	Script(void);
	~Script(void);

	long literalAt(int);
	long localAt(int);
	unsigned char bytecodeAt(int);
	bool shouldStepNow(long);
	void rememberLastStepTime(long);
	bool isStepping(void);
	void setStepping(bool);
	long stepTime(void);
	void setNext(Script*);
	Script* getNext(void);

private:
	
	bool _stepping;
	long _stepTime;
	long _lastStepTime;
	long * _literals;
	long * _locals;
	unsigned char * _bytecodes;
	Script * _nextScript;

	long * parseSection(ReadStream*);
	unsigned char * parseBytecodes(ReadStream*);
};

