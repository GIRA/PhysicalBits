accessing
locals
	^ String streamContents: [:stream |
		stackFrame ifNotNil: [:sf |
			sf script variables do: [:var |
				stream nextPutAll: var displayName;
					nextPutAll: ': ';
					print: (sf localAt: var);
					cr]]]