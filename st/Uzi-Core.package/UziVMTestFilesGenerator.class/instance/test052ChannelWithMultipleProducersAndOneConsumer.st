tests
test052ChannelWithMultipleProducersAndOneConsumer
	| program |
	program := '
	import c from ''Channel.uzi'';

	task blink() running 1/s { toggle(D13); }

	task producer1() running { c.send(0); }
	task producer2() running { c.send(0.25); }
	task producer3() running { c.send(0.5); }
	task producer4() running { c.send(0.75); }
	task producer5() running { c.send(1); }

	task consumer() running 1/s { write(D11, c.receive()); }'.
	self writeFile: 'Test052ChannelWithMultipleProducersAndOneConsumer'
		program: program