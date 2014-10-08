tests
testButton10Led9Script
	| cc program script1 bytecodes |
	cc := UziCompiler new.
	program := cc compile: '#button10Led9 ticking 100/s [
			at: 9 put: (at: 10)
		]'.
	self assert: program scripts size = 1.
	script1 := program scripts first.
	self assert: script1 name = #button10Led9.
	self assert: script1 isTicking.
	self assert: script1 delay = 10.
	self assert: (script1 literals collect: #value) = #(9 10).
	self assert: script1 variables isEmpty.
	bytecodes := script1 bytecodes.
	self assert: bytecodes size = 5.
	self assert: bytecodes = {
		pushLit arg: script1 literals first.
		pushLit arg: script1 literals second.
		primCall arg: 0.
		primCall arg: 1.
		end.
	}
