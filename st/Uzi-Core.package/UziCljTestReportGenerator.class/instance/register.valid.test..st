as yet unclassified
register: aString valid: aBoolean test: aSymbol 
	| list |
	list := tests at: aSymbol ifAbsentPut: [OrderedCollection new].
	list add: aString -> aBoolean.