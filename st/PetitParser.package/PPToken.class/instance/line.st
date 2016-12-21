querying
line
	"Answer the line number of this token in the underlying collection."
	
	| line |
	line := 1.
	(NewLineParser , [ :stream |
		start <= stream position
			ifTrue: [ ^ line ].
		line := line + 1 ] asParser
		/ #any asParser) star
			parse: collection.
	^ line