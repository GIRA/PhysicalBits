accessing
estimatedSize
	^ 16 + 24 +
	(self locals size * 4) +
	(self instructions collect: #estimatedSize) sum
	