accessing
children
	^ Array streamContents: [:stream |
		stream nextPutAll: self arguments.
		self tickingRate ifNotNil: [:tr | stream nextPut: tr].
		stream nextPut: self body]