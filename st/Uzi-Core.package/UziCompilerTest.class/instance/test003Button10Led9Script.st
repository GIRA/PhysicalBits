tests
test003Button10Led9Script
	| program script1 instructions |
	program := self compile: '
	task button10Led9() running 100/s {
		write(9, read(10));
	}'.
	self assert: program scripts size = 1.	
	self assert:  #(9 10) equals: (program sortedGlobals collect: #value).
	script1 := program scripts first.
	self assert: script1 name = #button10Led9.
	self assert: script1 isTicking.
	self assert: script1 delay = 10.
	instructions := script1 instructions.
	self assert: instructions size = 4.
	self assert: instructions = {
		push arg: program sortedGlobals first.
		push arg: program sortedGlobals second.
		prim arg: (UziPrimitive named: #read).
		prim arg: (UziPrimitive named: #write)
	}
