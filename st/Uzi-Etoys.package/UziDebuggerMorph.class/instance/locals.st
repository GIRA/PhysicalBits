accessing
locals
	^ String streamContents: [:stream |
		stackFrame ifNotNil: [
			stackFrame script variables do: [:var |
				stream print: var name;
					nextPutAll: ' -> ';
					print: (stackFrame localAt: var);
					cr]]]