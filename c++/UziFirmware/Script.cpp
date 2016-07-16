#include "Script.h"

Script::Script(Reader * rs)
{
	long n = rs->nextLong(4);
	stepping = (n >> 31) & 1;
	stepTime = n & 0x7FFFFFFF;
	lastStepTime = 0;

	literals = parseSection(rs);
	locals = parseSection(rs);
	bytecodes = parseBytecodes(rs);
	nextScript = 0;
}

Script::Script()
{
	// Returns a NOOP program.
	stepping = false;
	stepTime = lastStepTime = 0;
	literals = new long[0];
	locals = new long[0];
	bytecodes = new unsigned char[1];
	bytecodes[0] = 0xFF;
	nextScript = 0;
}

Script::~Script(void)
{
	delete[] literals;
	delete[] locals;
	delete[] bytecodes;
}

long Script::literalAt(int index)
{
	return literals[index];
}

long Script::localAt(int index)
{
	return locals[index];
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

long * Script::parseSection(Reader * rs)
{
	unsigned char size = rs->next();
	long * result = new long[size];
	int i = 0;
	while (i < size)
	{
		unsigned char sec = rs->next();
		int count = (sec >> 2) & 0x3F;
		int size = (sec & 0x03) + 1; // ACAACA Richo: This variable is shadowing the outer size!! FIX THIS!!
		while (count > 0)
		{
			result[i] = rs->nextLong(size);
			count--;
			i++;
		}
	}
	return result;
}

unsigned char * Script::parseBytecodes(Reader * rs)
{
	return rs->upTo(0xFF, true);
}
