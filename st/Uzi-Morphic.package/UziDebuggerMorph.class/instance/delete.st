initialization
delete
	super delete.
	debugger controller removeActionsWithReceiver: self.
	debugger close.
	window ifNotNil: [:w | 
		window := nil.
		w delete]