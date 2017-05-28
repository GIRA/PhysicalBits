tests
testVariableDefinitionsScript
	| cc program |
	cc := UziCompiler new.
	program := cc compile: '
	| a = 3,  b = 4 |
	
	@main ticking [ D13 write: a + b]'.
	self assert: 3 equals: program globals size.
	self assert: #(3 4 13) equals: (program sortedGlobals collect: #value).
	self assert: #(a b nil) equals: (program sortedGlobals collect: #name)