tests
test054VariablesWithTheSameNameInDifferentScopesShouldNotInterfereWithEachOther
	| program |
	program := '
	"Pin 7 should blink once per second.
	Pin 6 should blink once per second."
	task main() running 1/s {
		if 1 { 
			var pin = 7; 
			toggle(pin);
		}
		var pin;
		pin = pin + 6;
		toggle(pin);
	}'.
	self writeFile: 'Test054VariablesWithTheSameNameInDifferentScopesShouldNotInterfereWithEachOther'
		program: program