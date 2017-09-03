tests
testAssignmentScript
	| program script1 |
	program := self compile: 'var c; task main() running { c = 1; }'.
	self assert: 2 equals: program globals size.
	self assert: program scripts size = 1.
	script1 := program scripts first.
	self assert: script1 instructions = {
		push arg: (program globals detect: [:var | var value = 1]).
		pop arg: (program globals detect: [:var | var name = 'c']).
	}
