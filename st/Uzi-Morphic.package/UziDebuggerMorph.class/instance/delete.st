initialization
delete
	super delete.
	debugger protocol removeActionsWithReceiver: self.
	debugger close.
	window ifNotNil: [:w | 
		window := nil.
		w delete]