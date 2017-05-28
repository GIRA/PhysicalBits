tests
testButton10Led9Script
	| cc program script1 instructions |
	cc := UziCompiler new.
	program := cc compile: '@button10Led9 ticking 100/s [
			9 write: 10 read
		]'.
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
