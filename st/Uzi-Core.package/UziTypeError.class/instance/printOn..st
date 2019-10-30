printing
printOn: stream
	stream nextPutAll: self messageText.
	self token ifNotNil: [:tkn |
		stream cr; tab.
		self flag: #Richo. "TODO: Show more source than just the token"
		stream 
			nextPutAll: '... ';
			nextPutAll: tkn inputValue;
			nextPutAll: ' ...']