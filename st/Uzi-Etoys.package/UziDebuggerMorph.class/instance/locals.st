accessing
locals
	^ String streamContents: [:stream |
		debugger locals do: [:kvp |
			stream print: kvp key;
				nextPutAll: ' -> ';
				print: kvp value;
				cr]]