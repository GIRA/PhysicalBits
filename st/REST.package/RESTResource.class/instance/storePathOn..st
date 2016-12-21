private
storePathOn: stream
	parent ifNotNil: [:p |
		p storePathOn: stream.
		stream nextPut: (p pathTo: self)]