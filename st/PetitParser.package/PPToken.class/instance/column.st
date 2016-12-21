querying
column
	"Answer the column number of this token in the underlying collection."
	
	| position |
	position := 0.
	(NewLineParser , [ :stream |
		start <= stream position
			ifTrue: [ ^ start - position ].
		position := stream position ] asParser
		/ #any asParser) star
			parse: collection.
	 ^ start - position