tests
testLoopBodyShouldContainOnlyStatements
	self assertInvalid: 'task foo() { while read(D13) { read(D13); }}'.
	self assertInvalid: 'task foo() { do { read(D13); } while read(D13); }'.