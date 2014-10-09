tests
testBlinkLed9Script
	| cc program script1 |
	cc := UziCompiler new.
	program := cc compile: '#blinkLed9 ticking 1/s [
			(at: 9)
				ifTrue: [at: 9 put: 1]
				ifFalse: [at: 9 put: 0]
		]'.
	self assert: program scripts size = 1.
	script1 := program scripts first.
	self assert: script1 name = #blinkLed9.
	self assert: script1 isTicking.
	self assert: script1 delay = 1000.
	self assert: (script1 literals collect: #value) = #(9 1 0).
	self assert: script1 variables isEmpty.
	self assert: script1 bytecodes = {
		pushLit arg: script1 literals first.
		primCall arg: #getValue.
		pushLit arg: 1.
		self flag: #Richo. "ACAACA fix this!"
	}
