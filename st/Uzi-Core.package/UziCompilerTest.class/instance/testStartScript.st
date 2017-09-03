tests
testStartScript
	| program script |
	program := self compile: 'task main() running { start main; }'.
	script := program scripts first.
	self assert: script instructions = {
		start arg: #main
	}
