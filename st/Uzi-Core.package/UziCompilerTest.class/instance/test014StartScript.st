tests
test014StartScript
	| program script |
	program := self compile: 'task main() running { start main; }'.
	script := program scripts last.
	self assert: script instructions = {
		start arg: #main
	}
