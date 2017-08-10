tests
testStopScript
	| program script |
	program := cc compile: 'task main() running {stop main;}'.
	script := program scripts first.
	self assert: script instructions = {
		stop arg: #main
	}
