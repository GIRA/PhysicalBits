tests
test040ResumeScript
	| program script |
	program := self compile: 'task main() running { resume main; }'.
	script := program scripts detect: [:s | s name = #main].
	self assert: script instructions = {
		resume arg: #main
	}
