tests
testBlockOnlyContainsStatements
	self assertInvalid: 'task foo() {4;}'.	
	self assertInvalid: 'var a; task foo() {a;}'.
	self assertInvalid: 'task foo() { read(4); }'.
	self assertInvalid: 'task foo() { 3 > 4; }'.
	self assertInvalid: 'task foo() {D13;}'.
	self assertInvalid: 'task foo() {sin(5);}'.
	self assertInvalid: 'task foo() { if 3 > 4 {4;}}'.
	self assertInvalid: 'task foo() { if 3 > 4 {} else{4;}}'.
	self assertInvalid: 'task foo() { if 3 > 4 {3;} else {turnOn(3);}}'.
	self assertInvalid: 'task foo() { if 3 > 4 { turnOff(3);} else { 3;}}'.