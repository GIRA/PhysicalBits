tests
testScriptWithOptionalTickingState
	| cc script |
	cc := UziCompiler new.
	"Case: No state, no rate"
	script := (cc compile: '| a, b | @main [ a write: b ]') scripts first.
	self deny: script isTicking.
	self assert: 0 equals: script delay.
	"Case: No state"
	script := (cc compile: '| a, b | @main 1/s [ a write: b ]') scripts first.
	self assert: script isTicking.
	self assert: 1000 equals: script delay.
	"Case: No rate"
	script := (cc compile: '| a, b | @main ticking [ a write: b ]') scripts first.
	self assert: script isTicking.
	self assert: 0 equals: script delay.
	"Case: Both"
	script := (cc compile: '| a, b | @main ticking 1/s [ a write: b ]') scripts first.
	self assert: script isTicking.
	self assert: 1000 equals: script delay.