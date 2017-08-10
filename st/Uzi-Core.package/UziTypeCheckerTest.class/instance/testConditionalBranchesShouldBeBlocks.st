tests
testConditionalBranchesShouldBeBlocks
	"self assertInvalid: 'task foo() { if read(D13) turnOff(D13); }'.
	self assertInvalid: 'task foo() { if read(D13) {} else turnOn(D13); }'."