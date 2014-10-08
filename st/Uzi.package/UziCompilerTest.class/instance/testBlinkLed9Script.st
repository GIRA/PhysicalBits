tests
testBlinkLed9Script
	| cc program script1 |
	cc := UziCompiler new.
	program := cc compile: '#blinkLed9 ticking 1/s [
			(at: 9) = 0
				ifTrue: [at: 9 put: 1]
				ifFalse: [at: 9 put: 0]
		]'.
	self assert: program scripts size = 1.
	script1 := program scripts first.
	self assert: script1 name = #blinkLed9.
	self assert: script1 isTicking.
	self assert: script1 delay = 1000.
	self assert: script1 literals = #(9 0 1).
	self assert: script1 variables isEmpty.
	self assert: script1 bytecodes = {
		pushLit arg: 0.
		primCall arg: #getValue.
		pushLit arg: 1.
		self flag: #Richo. "ACAACA fix this!"
	}
