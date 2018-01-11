#include "Program.h"

Program::Program(Reader * rs, bool& timeout)
{
	globalCount = 0;
	globals = 0;
	globalsReport = 0;
	scripts = 0;
	coroutine = 0;

	scriptCount = rs->next(timeout);
	if (!timeout) { parseGlobals(rs, timeout); }
	if (!timeout) { parseScripts(rs, timeout); }

	if (timeout)
	{
		scriptCount = 0;
	}
	else
	{
		initializeCoroutines();
	}
}

Program::Program()
{
	scriptCount = 0;
	scripts = 0;
	coroutine = 0;
	globalCount = 0;
	globals = 0;
	globalsReport = 0;
}

Program::~Program(void)
{
	delete[] globalsReport;
	delete[] globals;
	delete[] scripts;
	delete coroutine;
}

uint8 Program::getScriptCount(void)
{
	return scriptCount;
}

Script * Program::getScript(int16 index)
{
	if (index >= scriptCount) return 0;
	return &scripts[index];
}

uint8 Program::getCoroutineCount(void)
{
	// INFO(Richo): For now, we'll use the same var
	return scriptCount;
}

Coroutine* Program::getCoroutine(void)
{
	return coroutine;
}

Coroutine* Program::getCoroutine(int16 index)
{
	if (index >= scriptCount) return 0;
	Coroutine* result = coroutine;
	for (int i = 0; i < index; i++)
	{
		result = result->getNext();
	}
	return result;
}

void Program::parseGlobals(Reader * rs, bool& timeout)
{
	const int defaultGlobalsCount = 3;
	float defaultGlobals[defaultGlobalsCount] = { 0, 1, -1 };

	globalCount = rs->next(timeout) + 3;
	if (timeout) return;
		
	globals = new float[globalCount];
	globalsReport = new bool[globalCount];
	for (int i = 0; i < defaultGlobalsCount; i++)
	{
		globals[i] = defaultGlobals[i];
		globalsReport[i] = false;
	}

	uint8 i = defaultGlobalsCount;
	while (i < globalCount)
	{
		uint8 sec = rs->next(timeout);
		if (timeout) return;

		int16 count = (sec >> 2) & 0x3F;
		int16 size = (sec & 0x03) + 1;
		while (count > 0)
		{
			globalsReport[i] = false;
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
	uint8 instructionCount = 0;
	for (int16 i = 0; i < scriptCount; i++)
	{
		Script* script = new Script(instructionCount, i, globals, rs, timeout);
		instructionCount += script->getInstructionCount();
		scripts[i] = *script;

		if (timeout) return;
	}
}

void Program::initializeCoroutines(void)
{
	Coroutine* last = 0;
	for (int i = 0; i < scriptCount; i++)
	{
		Coroutine* temp = new Coroutine(getScript(i));
		if (i == 0)
		{
			coroutine = last = temp;
		}
		else
		{
			last->setNext(temp);
			last = temp;
		}
	}
}

float Program::getGlobal(int16 index)
{
	return globals[index];
}

void Program::setGlobal(int16 index, float value)
{
	globals[index] = value;
}

bool Program::getReport(uint8 index)
{
	return globalsReport[index];
}

void Program::setReport(uint8 index, bool report)
{
	globalsReport[index] = report;
}

uint8 Program::getGlobalCount(void)
{
	return globalCount;
}

Script* Program::getScriptForPC(int16 pc)
{
	for (int i = 0; i < scriptCount; i++)
	{
		Script* current = getScript(i);
		if (pc >= current->getInstructionStart()
			&& pc <= current->getInstructionStop())
		{
			return current;
		}
	}
	return NULL;
}