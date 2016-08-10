#include "Program.h"

Program::Program(Reader * rs)
{
	scriptCount = rs->next();
	parseScripts(rs);
}

Program::Program()
{
	scriptCount = 0;
	script = new Script();
}

Program::~Program(void)
{
	//delete _script;
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