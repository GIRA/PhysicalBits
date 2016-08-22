tests
testButton10Led9Script
	| cc program script1 instructions |
	cc := UziCompiler new.
	program := cc compile: '#button10Led9 ticking 100/s [
			write: 9 value: (read: 10)
		]'.
	self assert: program scripts size = 1.	
	self assert:  #(9 10) equals: (program variables collect: #value).
	script1 := program scripts first.
	self assert: script1 name = #button10Led9.
	self assert: script1 isTicking.
	self assert: script1 delay = 10.
	instructions := script1 instructions.
	self assert: instructions size = 4.
	self assert: instructions = {
		pushLit arg: program variables first.
		pushLit arg: program variables second.
		primCall arg: (prim named: #read).
		primCall arg: (prim named: #write)
	}
