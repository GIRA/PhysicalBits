tests
test014StartingAndStoppingCanOnlyBePerformedOnTasks
	self assertInvalid: '
	func foo() { return 42; }
	task main() stopped { start foo; }'.
	self assertInvalid: '
	func foo() { return 42; }
	task main() stopped { stop foo; }'.
	self assertInvalid: '
	proc foo() { turnOn(D13); }
	task main() stopped { start foo; }'.
	self assertInvalid: '
	proc foo() { turnOn(D13); }
	task main() stopped { stop foo; }'