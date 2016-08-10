#include "Program.h"

Program::Program(Reader * rs)
{
	scriptCount = rs->next();
	globals = parseVariables(rs);
	parseScripts(rs);
}

Program::Program()
{
	scriptCount = 0;
	script = new Script();
}

Program::~Program(void)
{
	Script * current = script;
	Script * next;
	for (int i = 0; i < scriptCount; i++)
	{
		next = current->getNext();
		delete current;
		current = next;
	}
}

unsigned char Program::getScriptCount(void)
{
	return scriptCount;
}

Script * Program::getScript(void)
{
	return script;
}

long * Program::parseVariables(Reader * rs)
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

void Program::parseScripts(Reader * rs)
{
	Script * scriptTemp;
	for (int i = 0; i < scriptCount; i++)
	{
		scriptTemp = new Script(rs);
		scriptTemp->setNext(script);
		script = scriptTemp;
	}
}

void Program::configurePins(GPIO * io)
{
	io->reset();
}

long Program::getGlobal(int index)
{
	return globals[index];
}

void Program::setGlobal(int index, long value)
{
	globals[index] = value;
}