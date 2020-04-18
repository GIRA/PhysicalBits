private
saveInterval: aBlock
	| start stop |
	start := stream position.
	aBlock ensure: [
		stop := stream position.
		intervals add: (start + 1 to: stop)]