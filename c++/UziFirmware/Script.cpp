#include "Script.h"

Script::Script(ReadStream * rs) {
    long n = rs->nextLong(4);
	_stepping = (n >> 31) & 1;
	_stepTime = n & 0x7FFFFFFF;
	_lastStepTime = 0;
	
	_literals = parseSection(rs);
	_locals = parseSection(rs);
	_bytecodes = parseBytecodes(rs);
	_nextScript = 0;
}

Script::Script() {
	// Returns a NOOP program.
	_stepping = false;
	_stepTime = _lastStepTime = 0;
	_literals = new long[0];
	_locals = new long[0];
	_bytecodes = new unsigned char[1];
	_bytecodes[0] = 0xFF;
	_nextScript = 0;
}

Script::~Script(void) {
	delete[] _literals;
	delete[] _locals;
	delete[] _bytecodes;
}

long Script::literalAt(int index) {
	return _literals[index];
}

long Script::localAt(int index) {
	return _locals[index];
}

unsigned char Script::bytecodeAt(int index) {
	return _bytecodes[index];
}

void Script::rememberLastStepTime(long now) {
	_lastStepTime = now;
}

bool Script::shouldStepNow(long now) {
	return (now - _lastStepTime) > _stepTime;
}

bool Script::isStepping(void) {
	return _stepping;
}

void Script::setStepping(bool val) {
	_stepping = val;
}

Script* Script::getNext(void) {
	return _nextScript;
}

void Script::setNext(Script* next) {
	_nextScript = next;
}

long Script::stepTime(void) {
	return _stepTime;
}

long * Script::parseSection(ReadStream * rs) {
	unsigned char size = rs->nextChar();
	long * result = new long[size];
	int i = 0;
	while (i < size) {
		unsigned char sec = rs->nextChar();
		int count = (sec >> 2) & 0x3F;
		int size = (sec & 0x03) + 1; // ACAACA Richo: This variable is shadowing the outer size!! FIX THIS!!
		while (count > 0) {
			result[i] = rs->nextLong(size);
			count--;
			i++;
		}
	}
	return result;
}

unsigned char * Script::parseBytecodes(ReadStream * rs) {
	return rs->upTo(0xFF, true);
}
