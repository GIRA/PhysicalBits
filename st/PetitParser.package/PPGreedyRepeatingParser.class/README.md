A greedy repeating parser, commonly seen in regular expression implementations. It aggressively consumes as much input as possible and then backtracks to meet the 'limit' condition.

This class essentially implements the iterative version of the following recursive parser composition:

	| parser |
	parser := PPChoiceParser new.
	parser setParsers: (Array
		with: (self , parser map: [ :each :rest | rest addFirst: each; yourself ])
		with: (limit and ==> [ :each | OrderedCollection new ])).
	^ parser ==> [ :rest | rest asArray ]