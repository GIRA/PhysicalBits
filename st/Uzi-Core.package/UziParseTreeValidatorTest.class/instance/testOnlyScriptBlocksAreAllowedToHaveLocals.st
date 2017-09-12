tests
testOnlyScriptBlocksAreAllowedToHaveLocals
	self assertInvalid: 'task foo() stopped {
		if read(D13) { var a; toggle(D13); }
	}'.
	self assertInvalid: 'task foo() stopped {
		if read(D13) {} else { var a; toggle(D13); }
	}'.
	self assertInvalid: 'task foo() stopped {
		while read(D13) { var a; toggle(D13); }
	}'.
	self assertInvalid: 'task foo() stopped {
		do { var a; toggle(D13); } while read(D13);
	}'.