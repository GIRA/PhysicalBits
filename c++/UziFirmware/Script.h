#pragma once

#include "Reader.h"

class Script
{

public:
	Script(Reader*);
	Script(void);
	~Script(void);

	unsigned char getBytecodeCount(void);
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

	unsigned char * bytecodes;
	unsigned char bytecodeCount;

	Script * nextScript;
};

