tests
testInvalidSelector
	| cc |
	cc := UziCompiler new.
	self should: [cc compile: '@main ticking 1/s [1 primitiveThatDoesNotExist]'] 
		raise: UziCompileError
	