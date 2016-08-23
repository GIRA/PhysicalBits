connection
setGlobal: name value: value
	| index msg |
	index := currentProgram variables indexOf: (currentProgram variables detect: [:var | var name = name]).
	msg := ByteArray streamContents: [:stream | | actualValue |
		stream nextPut: 10.
		stream nextPut: index - 1.
		actualValue := value asFloat asIEEE32BitWord.
		(3 to: 0 by: -1) do: [:n |
			stream nextPut: ((actualValue bitShift: (8 * n * -1)) bitAnd: 16rFF)]].
	^ self send: msg
	