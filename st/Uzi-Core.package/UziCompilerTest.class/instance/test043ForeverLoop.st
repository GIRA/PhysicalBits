tests
test043ForeverLoop
	| program script |
	program := self compile: 'func c() { 
		forever { toggle(D13); delayMs(1000); }
	}'.
	script := program scripts first.
	self assert: script instructions = {
		push arg: (program globals detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #toggle).
		push arg: (program globals detect: [:var | var value = 1000]).
		prim arg: (UziPrimitive named: #delayMs).
		jmp arg: -5
	}
