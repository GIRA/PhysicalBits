tests
test018RepeatTimesShouldBeAnExpression
	self assertInvalid: 'task foo() running { 
		repeat turnOn(13) {
			toggle(D13);
		}
	}'.