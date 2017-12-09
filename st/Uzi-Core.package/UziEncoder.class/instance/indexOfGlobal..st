accessing
indexOfGlobal: anUziVariable 
	| index def |
	def := self defaultGlobals.
	index := (anUziVariable isConstant 
			and: [def includes: anUziVariable value])
		ifTrue: [def indexOf: anUziVariable value]
		ifFalse: [def size + (globals indexOf: anUziVariable)].
	"Zero-based"
	^ index - 1