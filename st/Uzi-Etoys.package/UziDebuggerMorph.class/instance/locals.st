accessing
locals
	^ String streamContents: [:stream |
		stackFrame ifNotNil: [:sf |
			sf script variables do: [:var |
				stream print: var name;
					nextPutAll: ' -> ';
					print: (sf localAt: var);
					cr]]]