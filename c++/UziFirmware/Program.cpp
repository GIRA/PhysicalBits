#include "Program.h"

Program::Program(Reader * rs, bool& timeout)
{
	globalCount = 0;
	globals = 0;
	globalsReport = 0;
	scripts = 0;

	scriptCount = rs->next(timeout);
	if (!timeout) { parseGlobals(rs, timeout); }
	if (!timeout) { parseScripts(rs, timeout); }

	if (timeout)
	{
		scriptCount = 0;
	}
}

Program::Program()
{
	scriptCount = 0;
	scripts = 0;
	globalCount = 0;
	globals = 0;
	globalsReport = 0;
}

Program::~Program(void)
{
	delete[] globalsReport;
	delete[] globals;
	delete[] scripts;
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

void Program::parseGlobals(Reader * rs, bool& timeout)
{
	const int defaultGlobalsCount = 3;
	float defaultGlobals[defaultGlobalsCount] = { 0, 1, -1 };

	globalCount = rs->next(timeout) + 3;
	if (timeout) return;

	// Initialize globals report
	{
		int globalsReportCount = 1 + (int)floor((double)globalCount / 8);
		globalsReport = new uint8[globalsReportCount];
		for (int i = 0; i < globalsReportCount; i++)
		{
			globalsReport[i] = 0;
		}
	}
		
	globals = new float[globalCount];
	for (int i = 0; i < defaultGlobalsCount; i++)
	{
		globals[i] = defaultGlobals[i];
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
	uint8 actualIndex = (int)floor((double)index / 8);
	uint8 byteValue = globalsReport[actualIndex];
	return byteValue & (1 << (index % 8));
}

void Program::setReport(uint8 index, bool report)
{
	uint8 actualIndex = (int)floor((double)index / 8);
	globalsReport[actualIndex] |= report ? 1 << (index % 8) : 0;
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