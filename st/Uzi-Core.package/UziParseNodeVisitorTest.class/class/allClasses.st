as yet unclassified
allClasses
	| fn |
	fn := nil. "To avoid compiler complaint"
	fn := [:class :stream | 
		stream nextPut: class.
		(class subclasses sorted: [:a :b | a name < b name] )
			do: [:child | fn value: child value: stream]].
	^ Array streamContents: [:stream | 
		fn value: self root value: stream]