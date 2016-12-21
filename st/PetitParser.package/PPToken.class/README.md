PPToken represents a parsed part of the input stream. Contrary to a simple String it remembers where it came from, the original collection, its start and stop position and its parse value.

Instance Variables:
	collection	<SequenceableCollection>	The collection this token comes from.
	start	<Integer>	The start position in the collection.
	stop	<Integer>	The stop position in the collection.
	value <Object>	The parse result.