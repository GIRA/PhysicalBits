as yet unclassified
formatSrc: src
	 ^ String streamContents: [:stream || index |
		index := 0.
		src linesDo: [:line |
			index = 0 ifFalse: [stream crlf.
				stream nextPutAll: '  	'].
			stream nextPutAll: (line copyReplaceAll: '"' with: '\"').
			index := index + 1]].