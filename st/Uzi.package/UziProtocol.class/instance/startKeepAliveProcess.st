keep alive
startKeepAliveProcess
	keepAlive ifNotNil: [^ self].
	keepAlive := [[self keepAlive.
			self keepAliveTime milliSeconds asDelay wait] repeat]
		forkNamed: ('Uzi - Keep alive ({1})' format: { self hash asString })