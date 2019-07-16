tests
test049ChannelShouldDeadlockIfConsumingFromTheSameTaskAsProducer
	| program |
	program := Uzi compileString: '
	import c from ''Channel.uzi'';

	task blink() running 1/s { toggle(D13); }

	task cp() running {
		turnOn(D11);
		c.send(c.receive());
		toggle(D11);
	}'.
	self writeFile: 'Test049ChannelShouldDeadlockIfConsumingFromTheSameTaskAsProducer'
		program: program