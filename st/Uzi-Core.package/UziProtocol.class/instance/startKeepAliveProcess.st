keep alive
startKeepAliveProcess
	keepAlive ifNotNil: [^ self].
	keepAlive := self fork: [self keepAliveLoop]
		at: Processor highestPriority 
		named: 'Keep alive'