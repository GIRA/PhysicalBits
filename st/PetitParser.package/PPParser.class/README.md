An abstract parser for all parsers in PetitParser. Subclasses implement #parseOn: to perform the actual recursive-descent parsing. All parsers support a variety of methods to perform an actual parse, see the methods in the #parsing protocol. Parsers are combined with a series of operators that can be found in the #operations protocol.

Instance Variables:
	properties	<Dictionary>	Stores additional state in the parser object.