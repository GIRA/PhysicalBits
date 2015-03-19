accessing
use
	^ (writes inject: 0 into: [:subTotal :next | subTotal + next])
		/ (1.0 * self size * self writeLimit)