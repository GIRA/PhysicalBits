tests
test053ChannelWithMultipleConsumersAndOneProducer
	| program |
	program := Uzi compileString: '
	import c from ''Channel.uzi'';

	task consumer1() running { write(D13, c.receive()); }
	task consumer2() running { write(D10, c.receive()); }
	task consumer3() running { write(D11, c.receive()); }
	task consumer4() running { write(D12, c.receive()); }


	task consumer() running { 
		for a = 0 to 1 by 0.25 {
			c.send(a);
			delayMs(1000);
		}
	}'.
	self writeFile: 'Test053ChannelWithMultipleConsumersAndOneProducer'
		program: program