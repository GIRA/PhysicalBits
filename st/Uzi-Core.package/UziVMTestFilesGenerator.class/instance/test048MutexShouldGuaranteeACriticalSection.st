tests
test048MutexShouldGuaranteeACriticalSection
	| program |
	program := '
	import m from ''Mutex.uzi'';

	task blink() running 1/s { toggle(D13); }

	task test1() running {
		m.acquire();
		for a = 0 to 1 by 0.25 {
			write(D11, a);
			delayMs(1000);
		}
		m.release();
	}

	task test2() running {
		m.acquire();
		for a = 1 to 0 by -0.5 {
			write(D11, a);
			delayMs(2000);
		}
		m.release();
	}'.
	self writeFile: 'Test048MutexShouldGuaranteeACriticalSection'
		program: program