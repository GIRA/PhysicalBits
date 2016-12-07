#include "Coroutine.h"

Coroutine::Coroutine(Script* script)
{
	this->script = script;
	pc = script->getInstructionStart();
	stack = new StackArray();
	next = 0;
}

Coroutine::Coroutine()
{
	pc = 0;
	stack = 0;
	script = 0;
	next = 0;
}

Coroutine::~Coroutine(void)
{
	delete stack;
	//delete script;
	delete next;
}

Script* Coroutine::getScript(void)
{
	return script;
}

int16 Coroutine::getPC(void)
{
	return pc;
}

StackArray* Coroutine::getStack(void)
{
	return stack;
}

Coroutine* Coroutine::getNext(void)
{
	return next;
}

void Coroutine::setNext(Coroutine* value)
{
	next = value;
}