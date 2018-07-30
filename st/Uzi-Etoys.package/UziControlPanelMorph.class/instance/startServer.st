server
startServer 
	server ifNotNil: [^ self].
	server := UziServer protocol: protocol