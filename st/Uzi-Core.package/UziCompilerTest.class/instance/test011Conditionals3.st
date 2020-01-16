tests
test011Conditionals3
	| program script |
	program := self compile: 'task main() running { if read(D13) { turnOff(D13); }}'.
	script := program scripts last.
	self assert: script instructions = {
		push arg: (program globals detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #read).
		jz arg: 2.
		push arg: (program globals detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #turnOff).
	}
