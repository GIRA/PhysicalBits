tests
test012TwoBlinksWithDifferentTickrate
	| program |
	program := Uzi compileString: '
		task blink8() running 1 / s {
                toggle(D8);
            }
            task blink9() running 0.5 / s {
                toggle(D9);
            }
            task trigger() running  {
                toggle(D13);
            }'.
	self writeFile: 'Test012TwoBlinksWithDifferentTickrate'
		contents: (UziProtocol new run: program)