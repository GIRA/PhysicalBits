tests
test039PauseScript
	| program script |
	program := self compile: 'task main() running { pause main; }'.
	script := program scripts first.
	self assert: script instructions = {
		pause arg: #main
	}
