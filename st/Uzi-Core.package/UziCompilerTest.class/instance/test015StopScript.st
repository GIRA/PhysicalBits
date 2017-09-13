tests
test015StopScript
	| program script |
	program := self compile: 'task main() running {stop main;}'.
	script := program scripts first.
	self assert: script instructions = {
		stop arg: #main
	}
