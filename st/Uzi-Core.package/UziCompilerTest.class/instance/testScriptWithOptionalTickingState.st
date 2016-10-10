tests
testScriptWithOptionalTickingState
	| cc script |
	cc := UziCompiler new.
	"Case 1: No state, no rate"
	script := (cc compile: '#main [ a write: b ]') first.
	self deny: script isTicking.
	self assert: 0 equals: script delay.
	"Case 2: No state"
	script := (cc compile: '#main 1/s [ a write: b ]') first.
	self assert: script isTicking.
	self assert: 1000 equals: script delay.
	"Case 2: No rate"
	script := (cc compile: '#main ticking [ a write: b ]') first.
	self assert: script isTicking.
	self assert: 0 equals: script delay.
	"Case 2: Both"
	script := (cc compile: '#main ticking 1/s [ a write: b ]') first.
	self assert: script isTicking.
	self assert: 1000 equals: script delay.