tests
test010Conditionals1
	| program script |
	program := self compile: 'task main() running { if read(D13) { turnOff(D13); } else { turnOn(D13); }}'.
	script := program scripts first.
	self assert: script instructions = {
		push arg: (program globals detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #read).
		jz arg: 3.
		push arg: (program globals detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #turnOff).
		jmp arg: 2.
		push arg: (program globals detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #turnOn).
	}
