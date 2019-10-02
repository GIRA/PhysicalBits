#include "Program.h"

Error readGlobals(Reader* rs, Program* program);
Error readScripts(Reader* rs, Program* program);

Error readProgram(Reader* rs, Program* program)
{
	program->globalCount = 0;
	program->globals = 0;
	program->globalsReport = 0;
	program->scripts = 0;

	bool timeout;
	program->scriptCount = rs->next(timeout);
	if (timeout)
	{
		program->scriptCount = 0;
		return READER_TIMEOUT;
	}

	Error result;
	result = readGlobals(rs, program);
	if (result != NO_ERROR) return result;

	if (program->scriptCount > 0)
	{
		result = readScripts(rs, program);
		if (result != NO_ERROR) return result;
	}
	return NO_ERROR;
}

Error readGlobals(Reader* rs, Program* program)
{
	const int defaultGlobalsCount = 3;
	float defaultGlobals[defaultGlobalsCount] = { 0, 1, -1 };
	
	// Initialize globalCount
	{
		bool timeout;
		program->globalCount = rs->next(timeout) + defaultGlobalsCount;
		if (timeout) return READER_TIMEOUT;
	}
	
	// Initialize globalsReport
	{
		int globalsReportCount = (int)ceil((double)program->globalCount / 8);

		program->globalsReport = uzi_createArray(uint8, globalsReportCount);
		if (program->globalsReport == 0) return OUT_OF_MEMORY;

		for (int i = 0; i < globalsReportCount; i++)
		{
			program->globalsReport[i] = 0;
		}
	}

	program->globals = uzi_createArray(float, program->globalCount);
	if (program->globals == 0) return OUT_OF_MEMORY;

	for (int i = 0; i < defaultGlobalsCount; i++)
	{
		program->globals[i] = defaultGlobals[i];
	}

	uint8 i = defaultGlobalsCount;
	while (i < program->globalCount)
	{
		bool timeout;
		uint8 sec = rs->next(timeout);
		if (timeout) return READER_TIMEOUT;

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
			if (timeout) return READER_TIMEOUT;

			count--;
			i++;
		}
	}
	return NO_ERROR;
}

Error readScripts(Reader * rs, Program* program)
{
	program->scripts = uzi_createArray(Script, program->scriptCount);
	if (program->scripts == 0) return OUT_OF_MEMORY;

	uint8 instructionCount = 0;
	for (int16 i = 0; i < program->scriptCount; i++)
	{
		Script* script = &program->scripts[i];

		Error result = readScript(rs, script, instructionCount, i, program->globals);
		instructionCount += script->getInstructionCount();

		if (result != NO_ERROR) return result;
	}
	return NO_ERROR;
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
	if (index < 0 || index >= globalCount) return 0;
	return globals[index];
}

void Program::setGlobal(int16 index, float value)
{
	if (index < 0 || index >= globalCount) return;
	globals[index] = value;
}

bool Program::getReport(uint8 index)
{
	if (index < 0 || index >= globalCount) return 0;
	uint8 actualIndex = (int)floor((double)index / 8);
	uint8 byteValue = globalsReport[actualIndex];
	return byteValue & (1 << (index % 8));
}

void Program::setReport(uint8 index, bool report)
{
	if (index < 0 || index >= globalCount) return;
	uint8 actualIndex = (int)floor((double)index / 8);
	uint8 mask = 1 << (index % 8);
	if (report) {
		globalsReport[actualIndex] |= mask;
	} else {
		globalsReport[actualIndex] &= ~(mask);
	}
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