#pragma once

#include "Reader.h"

class Script
{

public:
	Script(Reader*);
	Script(void);
	~Script(void);

	long literalAt(int);
	long localAt(int);
	unsigned char bytecodeAt(int);
	bool shouldStepNow(long);
	void rememberLastStepTime(long);
	bool isStepping(void);
	void setStepping(bool);
	long getStepTime(void);
	void setNext(Script*);
	Script* getNext(void);

private:

	bool stepping;
	long stepTime;
	long lastStepTime;
	long * literals;
	long * locals;
	unsigned char * bytecodes;
	Script * nextScript;

	long * parseSection(Reader*);
	unsigned char * parseBytecodes(Reader*);
};

