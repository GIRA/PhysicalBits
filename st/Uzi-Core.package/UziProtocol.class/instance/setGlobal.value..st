accessing-globals
setGlobal: globalName value: value
	| globalNumber |
	globalNumber := self getGlobalNumber: globalName.
	^ self send: (ByteArray streamContents: [:stream | | actualValue |
		stream nextPut: self class msgOutSetGlobal.
		stream nextPut: globalNumber.
		actualValue := (FloatArray with: value asFloat) basicAt: 1.
		(3 to: 0 by: -1) do: [:n |
			stream nextPut: ((actualValue bitShift: (8 * n * -1)) bitAnd: 16rFF)]])
	