#include "Script.h"

Script::Script(Reader * rs)
{
	long n = rs->nextLong(4);
	stepping = (n >> 31) & 1;
	stepTime = n & 0x7FFFFFFF;
	lastStepTime = 0;

	bytecodes = parseBytecodes(rs);
	nextScript = 0;
}

Script::Script()
{
	// Returns a NOOP program.
	stepping = false;
	stepTime = lastStepTime = 0;
	bytecodes = new unsigned char[1];
	bytecodes[0] = 0xFF;
	nextScript = 0;
}

Script::~Script(void)
{
	delete[] bytecodes;
}

unsigned char Script::bytecodeAt(int index)
{
	return bytecodes[index];
}

void Script::rememberLastStepTime(long now)
{
	lastStepTime = now;
}

bool Script::shouldStepNow(long now)
{
	return (now - lastStepTime) > stepTime;
}

bool Script::isStepping(void)
{
	return stepping;
}

void Script::setStepping(bool val)
{
	stepping = val;
}

Script* Script::getNext(void)
{
	return nextScript;
}

void Script::setNext(Script* next)
{
	nextScript = next;
}

long Script::getStepTime(void)
{
	return stepTime;
}

unsigned char * Script::parseBytecodes(Reader * rs)
{
	return rs->upTo(0xFF, true);
}
