tests
test061ForLoopWithExplicitStep
	| program script body for |
	program := parser parse: '
		task main() running {
			for i = 3 to 13 by 2 {
				toggle(i);
				delayMs(100);
			}
		}'.
	script := program scriptNamed: 'main'.
	self assert: 1 equals: script body statements size.
	for := script body statements first.
	self assert: #i equals: for counter name.
	self assert: 3 equals: for start value.
	self assert: 13 equals: for stop value.
	self assert: 2 equals: for step value.
	body := for body.
	self assert: 2 equals: body statements size.
	self assert: #toggle equals: body statements first selector.
	self assert: #delayMs equals: body statements second selector.