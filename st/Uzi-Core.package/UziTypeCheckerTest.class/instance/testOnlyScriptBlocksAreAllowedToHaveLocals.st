tests
testOnlyScriptBlocksAreAllowedToHaveLocals
	self assertInvalid: 'task foo() {
		if read(D13) { var a; toggle(D13); }
	}'.
	self assertInvalid: 'task foo() {
		if read(D13) {} else { var a; toggle(D13); }
	}'.
	self assertInvalid: 'task foo() {
		while read(D13) { var a; toggle(D13); }
	}'.
	self assertInvalid: 'task foo() {
		do { var a; toggle(D13); } while read(D13);
	}'.