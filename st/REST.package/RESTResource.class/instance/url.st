accessing
url
	^ String streamContents: [:stream | 
		self path 
			do: [:each | stream 
				nextPut: $/;
				nextPutAll: each asString]]