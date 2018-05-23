tests
test055VariablesWithTheSameNameInDifferentScopesShouldNotInterfereWithEachOther
	| program |
	program := Uzi compileString: '
	"Pin 7 should blink once per second.
	Pin 6 should blink once per second."
	task main() running 1/s {
		if 1 { 
			var pin = 7; 
			toggle(pin);
		}
		var pin = 6;
		toggle(pin);
	}'.
	self writeFile: 'Test055VariablesWithTheSameNameInDifferentScopesShouldNotInterfereWithEachOther'
		contents: (UziProtocol new run: program)