tests
test008VariableDefinitionsScript
	| program |
	program := self compile: '
	var a;
	var b = 4;
	
	task main() running { write(D13, a + b); }'.	
	"We have 4 globals because the task interval (0) is also a global"
	self assert: 4 equals: program globals size.
	self assert: #(0 0 4 13) equals: (program sortedGlobals collect: #value).
	self assert: #(nil a b nil) equals: (program sortedGlobals collect: #name)