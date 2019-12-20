accessing
corePrimitives
	| linker |
	linker := self defaultLinker
		parser: self defaultParser new
		librariesDirectory: self librariesDirectory.
	^ linker corePrimitives