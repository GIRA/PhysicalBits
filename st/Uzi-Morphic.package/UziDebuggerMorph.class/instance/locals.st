accessing
locals
	^ stackFrame 
		ifNil: [locals]
		ifNotNil: [:sf | locals := String streamContents: [:stream |
			sf script variables do: [:var |
				stream nextPutAll: var displayName;
					nextPutAll: ': ';
					print: (sf localAt: var);
					cr]]]