#include "Program.h"

Program::Program(Reader * rs, bool& timeout)
{
	scriptCount = rs->next(timeout);
	if (!timeout) { parseGlobals(rs, timeout); }
	if (!timeout) { parseScripts(rs, timeout); }
}

Program::Program()
{
	scriptCount = 0;
}

Program::~Program(void)
{
	delete[] globals;
	delete[] scripts;
}

unsigned char Program::getScriptCount(void)
{
	return scriptCount;
}

Script * Program::getScriptAt(int index)
{
	return &scripts[index];
}

void Program::parseGlobals(Reader * rs, bool& timeout)
{
	unsigned char varCount = rs->next(timeout);
	if (timeout) return;

	globals = new float[varCount];
	int i = 0;
	while (i < varCount)
	{
		unsigned char sec = rs->next(timeout);
		if (timeout) return;

		int count = (sec >> 2) & 0x3F;
		int size = (sec & 0x03) + 1;
		while (count > 0)
		{
			if (size == 4)
			{
				// Special case: float
				globals[i] = rs->nextFloat(timeout);
			}
			else
			{
				globals[i] = (float)rs->nextLong(size, timeout);
			}
			if (timeout) return;

			count--;
			i++;
		}
	}
}

void Program::parseScripts(Reader * rs, bool& timeout)
{
	scripts = new Script[scriptCount];
	for (int i = 0; i < scriptCount; i++)
	{		
		scripts[i] = Script(rs, timeout);
		if (timeout) return;
	}
}

float Program::getGlobal(int index)
{
	return globals[index];
}

void Program::setGlobal(int index, float value)
{
	globals[index] = value;
}