tests
test006ScriptWithOptionalTickingState
	| script |
	"Case: No rate"
	script := (self compile: 'var a; var b; task main() running { write(a, b);}') scripts first.
	self assert: script isTicking.
	self assert: 0 equals: script delay value.
	"Case: Both"
	script := (self compile: 'var a; var b; task main() running 1/s { write(a,b);}') scripts first.
	self assert: script isTicking.
	self assert: 1000 equals: script delay value.