tests
test051RepeatLoop
	| program script body repeat times |
	program := parser parse: '
		task main() running {
			repeat 5 {
				toggle(D13);
				delayMs(1000);
			}
		}'.
	script := program scriptNamed: 'main'.
	self assert: 1 equals: script body statements size.
	repeat := script body statements first.
	times := repeat times.
	body := repeat body.
	self assert: 5 equals: times value.
	self assert: 2 equals: body statements size.
	self assert: #toggle equals: body statements first selector.
	self assert: #delayMs equals: body statements second selector.