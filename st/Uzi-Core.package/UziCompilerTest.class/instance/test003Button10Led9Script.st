tests
test003Button10Led9Script
	| program script1 instructions |
	program := self compile: '
	task button10Led9() running {
		write(9, read(10));
	}'.
	self assert:  #(0 9 10) equals: (program sortedGlobals collect: #value).
	script1 := program scripts detect: [:s | s name = #button10Led9].
	self assert: script1 isTicking.
	self assert: script1 delay value = 0.
	instructions := script1 instructions.
	self assert: instructions size = 4.
	self assert: instructions = {
		push arg: (program globals detect: [:var | var value = 9]).
		push arg: (program globals detect: [:var | var value = 10]).
		prim arg: (UziPrimitive named: #read).
		prim arg: (UziPrimitive named: #write)
	}
