tests
testButton10Led9Script
	| cc program script1 bytecodes |
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
	bytecodes := script1 bytecodes.
	self assert: bytecodes size = 5.
	self assert: bytecodes = {
		pushLit arg: program variables first.
		pushLit arg: program variables second.
		primCall arg: (prim named: #getValue).
		primCall arg: (prim named: #setValue).
		end.
	}
