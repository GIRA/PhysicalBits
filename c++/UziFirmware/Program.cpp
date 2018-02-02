#include "Program.h"

void readGlobals(Reader* rs, Program* program, bool& timeout);
void readScripts(Reader* rs, Program* program, bool& timeout);

void readProgram(Reader* rs, Program* program, bool& timeout)
{
	program->globalCount = 0;
	program->globals = 0;
	program->globalsReport = 0;
	program->scripts = 0;

	program->scriptCount = rs->next(timeout);
	if (!timeout) { readGlobals(rs, program, timeout); }
	if (!timeout) { readScripts(rs, program, timeout); }

	if (timeout)
	{
		program->scriptCount = 0;
	}
}

void readGlobals(Reader* rs, Program* program, bool& timeout)
{
	const int defaultGlobalsCount = 3;
	float defaultGlobals[defaultGlobalsCount] = { 0, 1, -1 };

	program->globalCount = rs->next(timeout) + 3;
	if (timeout) return;

	// Initialize globals report
	{
		int globalsReportCount = 1 + (int)floor((double)program->globalCount / 8);
		program->globalsReport = new uint8[globalsReportCount];
		for (int i = 0; i < globalsReportCount; i++)
		{
			program->globalsReport[i] = 0;
		}
	}

	program->globals = new float[program->globalCount];
	for (int i = 0; i < defaultGlobalsCount; i++)
	{
		program->globals[i] = defaultGlobals[i];
	}

	uint8 i = defaultGlobalsCount;
	while (i < program->globalCount)
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
				program->globals[i] = rs->nextFloat(timeout);
			}
			else
			{
				program->globals[i] = (float)rs->nextLong(size, timeout);
			}
			if (timeout) return;

			count--;
			i++;
		}
	}
}

void readScripts(Reader * rs, Program* program, bool& timeout)
{
	program->scripts = new Script[program->scriptCount];
	uint8 instructionCount = 0;
	for (int16 i = 0; i < program->scriptCount; i++)
	{
		Script* script = new Script();
		readScript(rs, script, instructionCount, i, program->globals, timeout);
		instructionCount += script->getInstructionCount();
		program->scripts[i] = *script;

		if (timeout) return;
	}
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