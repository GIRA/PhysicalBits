#include "Program.h"

Program::Program(ReadStream * rs) {
	_scriptCount = rs->nextChar();
	parsePinModes(rs);
	parseScripts(rs);
}

Program::Program() {
	_scriptCount = 0;
	for (int i = 0; i < 3; i++) {
		_pinModes[i] = 0;
	}
	_script = new Script();
}

Program::~Program(void) {
	//delete _script;
	Script * current = _script;
	Script * next = _script->getNext();
	for (int i = 0; i < _scriptCount; i++) {
		delete current;
		current = next;
		next = current->getNext();
	}
}

unsigned char Program::getScriptCount(void) {
	return _scriptCount;
}

Script * Program::getScript(void) {
	return _script;
}

void Program::configurePins(PE * pe) {
	int pinNumber = 1;
	int pinMode = INPUT;
	for (int index = 0; index < 3; index++) {
		for (int shift = 7; shift >= 0; shift--) {
			if ((_pinModes[index] >> shift) & 0x01) {
				pinMode = OUTPUT;
			} else {
				pinMode = INPUT;
			}
			pe->setMode(pinNumber, pinMode);
			pinNumber++;
		}
	}
}

void Program::parsePinModes(ReadStream * rs) {
	for (int i = 0; i < 3; i++) {
		_pinModes[i] = rs->nextChar();
	}
}

void Program::parseScripts(ReadStream * rs) {
	Script * scriptTemp;
	for (int i = 0; i < _scriptCount; i++) {
		scriptTemp = new Script(rs);
		scriptTemp->setNext(_script);
		_script = scriptTemp;
	}
}