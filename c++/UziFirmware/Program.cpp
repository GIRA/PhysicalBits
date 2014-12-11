#include "Program.h"

Program::Program(ReadStream * rs) {
	_scriptCount = rs->nextChar();
	parsePinModes(rs);
	parseScripts(rs);
}

Program::Program() {
	_scriptCount = 0;
	for (int i = 0; i < 3; i++) {
		_inputs[i] = 0;
		_outputs[i] = 0;
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
	int pinNumber = 0;
	for (int index = 0; index < 3; index++) {
		for (int shift = 7; shift >= 0; shift--) {
			unsigned char in = _inputs[index] >> shift & 0x01;
			unsigned char out = _outputs[index] >> shift & 0x01;
			unsigned char pinMode = INPUT;
			bool report = false;			
			if (in && out) {
				pinMode = INPUT_PULLUP;
				report = true;
			} else if (in) {
				pinMode = INPUT;
				report = true;
			} else if (out) {
				pinMode = OUTPUT;
			}
			pe->setReport(pinNumber, report);
			pe->setMode(pinNumber, pinMode);
			pinNumber++;
		}
	}
}

void Program::parsePinModes(ReadStream * rs) {
	for (int i = 0; i < 3; i++) {
		_inputs[i] = rs->nextChar();
	}
	for (int i = 0; i < 3; i++) {
		_outputs[i] = rs->nextChar();
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