PPMemento is an internal class used by PPMemoizedParser to cache results and detect left-recursive calls.

Instance Variables:
	result	<Object>	The cached result.
	count	<Integer>	The number of recursive cycles followed.
	position	<Integer>	The position of the cached result in the input stream.