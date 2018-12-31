initializing
close
	self clearAllBreakpoints;
		sendBreakpoints;
		sendContinue.
	protocol removeActionsWithReceiver: self forEvent: self coroutineUpdateEvent.
	index := pc := fp := stack := nil