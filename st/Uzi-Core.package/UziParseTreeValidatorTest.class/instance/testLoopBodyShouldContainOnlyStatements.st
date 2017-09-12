tests
testLoopBodyShouldContainOnlyStatements
	self assertInvalid: 'task foo() stopped { while read(D13) { read(D13); }}'.
	self assertInvalid: 'task foo() stopped { do { read(D13); } while read(D13); }'.