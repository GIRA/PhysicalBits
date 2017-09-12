tests
test050StartKeywordAcceptsMultipleTasks
	| program script |
	program := parser parse: '
		task main() running {
			start t1, t2, t3;
		}'.
	script := program scriptNamed: 'main'.
	self assert: script isTask.
	self assert: 1 equals: script body statements size.
	self assert: #(t1 t2 t3) equals: script body statements first scriptNames