tests
testInvalidSelector
	self should: [cc compile: 'task main() running 1/s {primitiveThatDoesNotExist(1);}'] 
		raise: UziCompileError
	