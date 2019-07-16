tests
test013CaptureEveryPinWithDifferentTickrate
	| program |
	program := Uzi compileString: '
		task blink8() running 1 / s {
                toggle(D8);
            }
            task blink9() running 1.5 / s {
                toggle(D9);
            }
           task blink110() running 2 / s {
                toggle(D10);
            }
            task blink11() running 2.5 / s {
                toggle(D11);
            }
            task blink12() running 3 / s {
                toggle(D12);
            }
            task trigger() running  {
                toggle(D13);
            }'.
	self writeFile: 'Test013CaptureEveryPinWithDifferentTickrate'
		program: program