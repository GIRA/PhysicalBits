initialization
delete
	super delete.
	debugger close.
	window ifNotNil: [:w | 
		window := nil.
		w delete]