#include "Program.h"

Program::Program(Reader * rs)
{
	_scriptCount = rs->nextChar();
	parseScripts(rs);
}

Program::Program()
{
	_scriptCount = 0;
	_script = new Script();
}

Program::~Program(void)
{
	//delete _script;
	Script * current = _script;
	Script * next = _script->getNext();
	for (int i = 0; i < _scriptCount; i++)
	{
		delete current;
		current = next;
		next = current->getNext();
	}
}

unsigned char Program::getScriptCount(void)
{
	return _scriptCount;
}

Script * Program::getScript(void)
{
	return _script;
}

void Program::parseScripts(Reader * rs)
{
	Script * scriptTemp;
	for (int i = 0; i < _scriptCount; i++)
	{
		scriptTemp = new Script(rs);
		scriptTemp->setNext(_script);
		_script = scriptTemp;
	}
}

void Program::configurePins(GPIO * io)
{
	io->reset();
}