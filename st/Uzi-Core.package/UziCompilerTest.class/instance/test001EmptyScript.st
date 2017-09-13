tests
test001EmptyScript
	| program script1 |
	program := self compile: 'task main() running 1/s {}'.
	self assert: program scripts size = 1.
	script1 := program scripts first.
	self assert: script1 name = #main.
	self assert: script1 isTicking.
	self assert: script1 delay = 1000.
	self assert: script1 instructions isEmpty 