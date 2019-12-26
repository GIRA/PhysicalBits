tests
test001EmptyScript
	| program script |
	program := self compile: 'task main() running 1/s {}'.
	script := program scripts detect: [:s | s name = #main].
	self assert: script isTicking.
	self assert: script delay value = 1000.
	self assert: script instructions isEmpty 