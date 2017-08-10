tests
testScriptWithOptionalTickingState
	| script |
	"Case: No state, no rate"
	script := (cc compile: 'var a; var b; task main() { write(a, b);}') scripts first.
	self deny: script isTicking.
	self assert: 0 equals: script delay.
	"Case: No state"
	script := (cc compile: 'var a; var b; task main() 1/s { write(a, b); }') scripts first.
	self assert: script isTicking.
	self assert: 1000 equals: script delay.
	"Case: No rate"
	script := (cc compile: 'var a; var b; task main() running { write(a, b);}') scripts first.
	self assert: script isTicking.
	self assert: 0 equals: script delay.
	"Case: Both"
	script := (cc compile: 'var a; var b; task main() running 1/s { write(a,b);}') scripts first.
	self assert: script isTicking.
	self assert: 1000 equals: script delay.