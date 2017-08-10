tests
testVariableDefinitionsScript
	| program |
	program := cc compile: '
	var a;
	var b = 4;
	
	task main() running { write(D13, a + b); }'.
	self assert: 3 equals: program globals size.
	self assert: #(0 4 13) equals: (program sortedGlobals collect: #value).
	self assert: #(a b nil) equals: (program sortedGlobals collect: #name)