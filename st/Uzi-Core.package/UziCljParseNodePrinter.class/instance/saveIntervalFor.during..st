private
saveIntervalFor: node during: aBlock
	| start stop |
	start := stream position.
	aBlock ensure: [
		stop := stream position.
		intervals at: node put: (start + 1 to: stop)]