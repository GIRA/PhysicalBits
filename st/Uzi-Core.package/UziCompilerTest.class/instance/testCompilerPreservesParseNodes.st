tests
testCompilerPreservesParseNodes
	| cc program instr1 instr2 script |
	cc := UziCompiler new.
	program := cc compile: '@main 1 / s [ D13 toggle ]'.
	self assert: program node variableDefinitions isEmpty.
	self assert: 1 equals: program node scripts size.
	script := program scripts first.
	self assert: #main equals: script node name.
	self assert: 2 equals: script instructions size.
	instr1 := script instructions first.
	self assert: 13 equals: instr1 node value.
	instr2 := script instructions second.
	self assert: instr1 node equals: instr2 node arguments first.
	self assert: #toggle equals: instr2 node selector.
	self assert: instr2 node arguments size = 1