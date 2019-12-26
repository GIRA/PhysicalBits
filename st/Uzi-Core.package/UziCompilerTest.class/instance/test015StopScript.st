tests
test015StopScript
	| program script |
	program := self compile: 'task main() running {stop main;}'.
	script := program scripts last.
	self assert: script instructions = {
		stop arg: #main
	}
