tests
test001BlockOnlyContainsStatements
	self assertInvalid: 'task foo() stopped {4;}'.	
	self assertInvalid: 'var a; task foo() stopped {a;}'.
	self assertInvalid: 'task foo() stopped { read(4); }'.
	self assertInvalid: 'task foo() stopped { 3 > 4; }'.
	self assertInvalid: 'task foo() stopped {D13;}'.
	self assertInvalid: 'task foo() stopped {sin(5);}'.
	self assertInvalid: 'task foo() stopped { if 3 > 4 {4;}}'.
	self assertInvalid: 'task foo() stopped { if 3 > 4 {} else{4;}}'.
	self assertInvalid: 'task foo() stopped { if 3 > 4 {3;} else {turnOn(3);}}'.
	self assertInvalid: 'task foo() stopped { if 3 > 4 { turnOff(3);} else { 3;}}'.