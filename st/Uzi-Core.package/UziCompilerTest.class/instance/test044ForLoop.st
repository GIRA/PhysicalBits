tests
test044ForLoop
	| program script counter |
	program := self compile: 'func c() { 
		for i = 3 to 13 by 2 { toggle(i); delayMs(1000); }
	}'.
	script := program scripts detect: [:s | s name = #c].
	self assert: 1 equals: script locals size.
	counter := script locals first.
	self assert: script instructions = {
		push arg: (program globals detect: [:var | var value = 3]).
		writeLocal arg: counter.
		readLocal arg: counter.
		push arg: (program globals detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #lessThanOrEquals).
		jz arg: 9.
		readLocal arg: counter.
		prim arg: (UziPrimitive named: #toggle).
		push arg: (program globals detect: [:var | var value = 1000]).
		prim arg: (UziPrimitive named: #delayMs).
		readLocal arg: counter.
		push arg: (program globals detect: [:var | var value = 2]).
		prim arg: (UziPrimitive named: #add).
		writeLocal arg: counter.
		jmp arg: -13
	}
