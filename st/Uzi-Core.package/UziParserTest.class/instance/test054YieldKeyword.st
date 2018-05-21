tests
test054YieldKeyword
	| program script yield |
	program := parser parse: '
		task main() running { yield; }'.
	script := program scriptNamed: 'main'.
	self assert: script isTask.
	self assert: 1 equals: script body statements size.
	yield := script body statements first.
	self assert: (yield isKindOf: UziYieldNode).
	self assert: yield isStatement 