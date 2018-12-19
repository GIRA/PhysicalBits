server
stopServer 
	server ifNotNil: [:s | s destroy].
	server := nil