tests
test050ChannelWithMultipleProducersAndNoConsumerShouldBlockAllProducers
	| program |
	program := Uzi compileString: '
	import c from ''Channel.uzi'';
	var counter = 0.5;

	task producer1() running {
		c.send(1);
		counter = 1;
	}

	task producer0() running {
		c.send(0);
		counter = 2;
	}

	task producer05() running {
		c.send(0.5);
		counter = 3;
	}

	task blink() running 1/s { toggle(D13); }

	task test() running { write(D11, counter); }'.
	self writeFile: 'Test050ChannelWithMultipleProducersAndNoConsumerShouldBlockAllProducers'
		contents: (UziProtocol new run: program)