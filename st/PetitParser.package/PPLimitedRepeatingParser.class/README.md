An abstract parser that repeatedly parses between 'min' and 'max' instances of my delegate and that requires the input to be completed with a specified parser 'limit'. Subclasses provide repeating behavior as typically seen in regular expression implementations (non-blind).

Instance Variables:
	limit	<PPParser>	The parser to complete the input with.