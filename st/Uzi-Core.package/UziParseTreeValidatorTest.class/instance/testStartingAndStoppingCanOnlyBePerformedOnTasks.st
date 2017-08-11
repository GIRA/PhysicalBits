tests
testStartingAndStoppingCanOnlyBePerformedOnTasks
	self assertInvalid: '
	func foo() { return 42; }
	task main() { start foo; }'.
	self assertInvalid: '
	func foo() { return 42; }
	task main() { stop foo; }'.
	self assertInvalid: '
	proc foo() { turnOn(D13); }
	task main() { start foo; }'.
	self assertInvalid: '
	proc foo() { turnOn(D13); }
	task main() { stop foo; }'