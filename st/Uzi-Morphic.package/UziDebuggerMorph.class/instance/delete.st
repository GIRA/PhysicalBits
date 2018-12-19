initialization
delete
	super delete.
	debugger continue; close.
	window ifNotNil: [:w | 
		window := nil.
		w delete]