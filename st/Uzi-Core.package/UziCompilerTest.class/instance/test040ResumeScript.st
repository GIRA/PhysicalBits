tests
test040ResumeScript
	| program script |
	program := self compile: 'task main() running { resume main; }'.
	script := program scripts first.
	self assert: script instructions = {
		resume arg: #main
	}
